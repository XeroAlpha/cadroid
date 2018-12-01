var fs = require("fs");
var process = require("process");
var zlib = require("zlib");
var crypto = require("crypto");
var crc32 = require("./crc32");

var cwd = process.cwd();
var gradle = cwd + "/app/build.gradle";
var scriptPath = cwd + "/app/src/main/assets/script.js";
var pageUrl = "https://projectxero.gitee.io/ca/";
var privateKey = cwd + "/app/signatures/privatekey.pem"

function updateBuild(corePath) {
	var versions = JSON.parse(fs.readFileSync(corePath + "/versions.json", 'utf-8'));
	var lv = versions[versions.length - 1];
	var s = fs.readFileSync(gradle, 'utf-8');
	s = s.replace(/versionCode \d+/, "versionCode " + Math.floor(lv.time / 86400000));
	s = s.replace(/versionName ".*"/, "versionName \"" + lv.version + " (" + lv.belongs + ")" + "\"");
	fs.writeFileSync(gradle, s);
}

function crc32wrap(buffer) {
	return crc32(0, buffer, buffer.length, 0);
}

function addDexVerification(content) {
	var crc = crc32wrap(fs.readFileSync(cwd + "/app/build/intermediates/transforms/dexMerger/release/0/classes.dex")).toString(16);
	console.log("Current dex crc32: " + crc.toUpperCase());
	return content.replace(/\$dexCrc\$/g, crc);
}

function sign(source, signPath, targetPath, dexVerify) {
	var srcbuf = zlib.gzipSync(Buffer.from(dexVerify ? addDexVerification(source) : source));
	var sgnbuf = fs.readFileSync(signPath);
	var i, srcsize = srcbuf.length, sgnsize = sgnbuf.length;
	for (i = 0; i < srcsize; i++) {
		srcbuf[i] ^= sgnbuf[i % sgnsize];
	}
	fs.writeFileSync(targetPath, srcbuf);
}

function makeUpdate(corePath, signPath) {
	var data = JSON.parse(fs.readFileSync(corePath + "/update.json"));
	sign(fs.readFileSync(corePath + "/build/min.js", 'utf-8').replace(/AndroidBridge\.HOTFIX/g, "true"), signPath, corePath + "/pages/hotfix.js");
	var signature = crypto.createSign("RSA-SHA256");
	signature.update(fs.readFileSync(corePath + "/pages/hotfix.js"));
	var signBytes = signature.sign(fs.readFileSync(privateKey).toString());
	var versionBytes = Buffer.alloc(4);
	var grad = fs.readFileSync(gradle, 'utf-8');
	versionBytes.writeInt32LE(getVersionCode(grad), 0);
	fs.writeFileSync(corePath + "/pages/hotfix.sign", Buffer.concat([versionBytes, signBytes]));
	data.hotfix = {
		"url": pageUrl + "hotfix.js",
		"sign": pageUrl + "hotfix.sign",
		"shell": getShellVersion(grad),
		"sha1": digestSHA1(fs.readFileSync(corePath + "/pages/hotfix.js"))
	};
	data.requirements = [{
		"type": "minsdk",
		"value": getMinSDKVersion(grad)
	}];
	fs.writeFileSync(corePath + "/pages/hotfix.json", JSON.stringify(data));
}

function buildBeta(corePath) {
	var hotfix = JSON.parse(fs.readFileSync(corePath + "/pages/hotfix.json"));
	var ver = hotfix.belongs.split(".").map((e) => parseInt(e));
	fs.writeFileSync(corePath + "/export/命令助手(" + hotfix.version + ").lib", asLibrary([
		'Plugins.inject(function(o){',
			'const pub=' + JSON.stringify(hotfix.version) + ',ver=' + JSON.stringify(ver) + ',shell=' + JSON.stringify(hotfix.hotfix.shell) + ',ds=' + JSON.stringify(hotfix.info) + ';',
			'function u(p,b){',
				'var o=new java.io.FileOutputStream(p);',
				'o.write(android.util.Base64.decode(b,2));',
				'o.close();',
			'}',
			'o.name="命令助手尝鲜包 - "+pub;',
			'o.description="命令助手Beta版安装器\\n";',
			'o.uuid="64ac6220-cf64-465a-8af8-1c9dd2835cd0";',
			'o.author="命令助手制作组";',
			'o.version=ver;',
			'if (Date.parse(CA.publishDate)>=Date.parse(pub)){',
				'CA.Library.removeLibrary(path);',
				'return void(o.description+="您正在使用本尝鲜版或更高版本\\n\\n"+ds);',
			'}',
			'if(MapScript.host!="Android")return void(o.description+="本安装器仅在App版上可用");',
			'if(shell!=ScriptActivity.getShellVersion())return void(o.description+="本安装器不适用于您的版本");',
			'u(MapScript.baseDir+"core.js",' + JSON.stringify(fs.readFileSync(corePath + "/pages/hotfix.js").toString("base64")) + ');',
			'u(MapScript.baseDir+"core.sign",' + JSON.stringify(fs.readFileSync(corePath + "/pages/hotfix.sign").toString("base64")) + ');',
			'Common.showTextDialog(o.description+="重新启动命令助手后即可使用");',
		'})'
	].join("")));
}
function asLibrary(s) {
	var o;
	var dh = Buffer.alloc(15), date = Date.now();
	dh.write("LIBRARY");
	dh.writeInt32BE(Math.floor(date / 0xffffffff), 7);
	dh.writeInt32BE(date & 0xffffffff, 11);
	s = zlib.gzipSync(s);
	o = Buffer.alloc(dh.length + s.length);
	dh.copy(o, 0);
	s.copy(o, dh.length);
	return o;
}

function getShellVersion(s) {
	var r = s.match(/buildConfigField "int", "SHELL_VERSION", "(\d+)"/);
	if (r) return parseInt(r[1]);
}

function getVersionCode(s) {
	var r = s.match(/versionCode (\d+)/);
	if (r) return parseInt(r[1]);
}

function getMinSDKVersion(s) {
	var r = s.match(/minSdkVersion (\d+)/);
	if (r) return parseInt(r[1]);
}

function digestSHA1(data) {
	var digest = crypto.createHash("sha1");
	digest.update(data);
	return digest.digest("base64");
}

function debug(corePath, signPath) {
	console.log("Updating build.gradle");
	updateBuild(corePath);
	console.log("Encrypting...");
	sign(require(corePath + "/loader").load(corePath + "/命令助手.js", 'utf-8'), signPath, scriptPath);
}

function release(corePath, signPath) {
	console.log("Updating build.gradle");
	updateBuild(corePath);
	console.log("Encrypting...");
	sign(fs.readFileSync(corePath + "/build/min.js", 'utf-8'), signPath, scriptPath, true);
	makeUpdate(corePath, signPath);
	buildBeta(corePath);
}

function hotfix(corePath, signPath) {
	updateBuild(corePath);
	makeUpdate(corePath, signPath);
	buildBeta(corePath);
}

function exportApk(corePath) {
	var update = JSON.parse(fs.readFileSync(corePath + "/update.json"));
	fs.copyFileSync(cwd + "/app/release/app-release.apk", corePath + "/export/命令助手(" + update.version + ").apk");
}

function help() {
	console.log("node updateCore.js <mode> <corePath> <signPath>");
	console.log(" <mode> 'debug', 'hotfix', 'export' or 'release'");
	console.log(" <corePath> root path of project ca");
	console.log(" <signPath> sign to be encrypted with");
}

if (process.argv.length != 5) {
	help();
} else {
	if (process.argv[2] == "debug") {
		debug(process.argv[3], process.argv[4]);
	} else if (process.argv[2] == "hotfix") {
		hotfix(process.argv[3], process.argv[4]);
	} else if (process.argv[2] == "export") {
		exportApk(process.argv[3]);
	} else {
		release(process.argv[3], process.argv[4]);
	}
}