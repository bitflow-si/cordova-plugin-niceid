var exec = require('cordova/exec');
module.exports.requestNiceId = function (arg0, success, error) {
    exec(success, error, 'NiceId', 'requestNiceId', [arg0]);
};

module.exports.downloadFile = function (arg0, success, error) {
    exec(success, error, 'NiceId', 'downloadFile', [arg0]);
};

module.exports.startScanBeacon = function (success, error) {
    exec(success, error, 'NiceId', 'startScanBeacon', []);
};

