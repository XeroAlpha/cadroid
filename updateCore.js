var fs = require("fs");
var process = require("process");
var zlib = require("zlib");
var crc32 = require("./crc32");

var cwd = process.cwd();
var gradle = cwd + "/app/build.gradle";

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
	return content.replace("$dexCrc$", crc);
}

function sign(corePath, signPath) {
	var srcbuf = zlib.gzipSync(Buffer.from(addDexVerification(fs.readFileSync(corePath + "/build/min.js", 'utf-8'))));
	var sgnbuf = fs.readFileSync(signPath);
	var i, srcsize = srcbuf.length, sgnsize = sgnbuf.length;
	for (i = 0; i < srcsize; i++) {
		srcbuf[i] ^= sgnbuf[i % sgnsize];
	}
	fs.writeFileSync(cwd + "/app/src/main/assets/script.js", srcbuf);
}

function main(corePath, signPath) {
	console.log("Updating build.gradle");
	updateBuild(corePath);
	console.log("Encrypting...");
	sign(corePath, signPath);
}

function help() {
	console.log("node updateCore.js <corePath> <signPath>");
	console.log(" <corePath> root path of project ca");
	console.log(" <signPath> sign to be encrypted with");
}

if (process.argv.length != 4) {
	help();
} else {
	main(process.argv[2], process.argv[3]);
}