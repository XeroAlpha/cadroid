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
	s = s.replace(/versionName ".*"/, "versionName \"" + lv.version + "\"");
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
	fs.writeFileSync(corePath + "/pages/hotfix.json", JSON.stringify(data));
}

function getShellVersion(s) {
	var r = s.match(/buildConfigField "int", "SHELL_VERSION", "(\d+)"/);
	if (r) return parseInt(r[1]);
}

function getVersionCode(s) {
	var r = s.match(/versionCode (\d+)/);
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
	sign(fs.readFileSync(corePath + "/命令助手.js", 'utf-8'), signPath, scriptPath);
}

function release(corePath, signPath) {
	console.log("Updating build.gradle");
	updateBuild(corePath);
	console.log("Encrypting...");
	sign(fs.readFileSync(corePath + "/build/min.js", 'utf-8'), signPath, scriptPath, true);
	makeUpdate(corePath, signPath);
}

function help() {
	console.log("node updateCore.js <mode> <corePath> <signPath>");
	console.log(" <mode> 'debug' or 'release'");
	console.log(" <corePath> root path of project ca");
	console.log(" <signPath> sign to be encrypted with");
}

if (process.argv.length != 5) {
	help();
} else {
	if (process.argv[2] == "debug") {
		debug(process.argv[3], process.argv[4]);
	} else {
		release(process.argv[3], process.argv[4]);
	}
}