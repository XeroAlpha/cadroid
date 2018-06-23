var fs = require("fs");
var process = require("process");
var zlib = require("zlib");

function main(corePath, signPath) {
	var srcbuf = zlib.gzipSync(fs.readFileSync(corePath + "/build/min.js"));
	var sgnbuf = fs.readFileSync(signPath);
	var i, srcsize = srcbuf.length, sgnsize = sgnbuf.length;
	for (i = 0; i < srcsize; i++) {
		srcbuf[i] ^= sgnbuf[i % sgnsize];
	}
	fs.writeFileSync("./app/src/main/assets/script.js", srcbuf);
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