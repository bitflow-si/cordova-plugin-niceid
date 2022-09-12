var exec = require('cordova/exec');
module.exports.requestNiceId = function (args, success, error) {
    exec(success, error, 'NiceId', 'requestNiceId', args);
};

module.exports.downloadFile = function (arg0, success, error) {
    exec(success, error, 'NiceId', 'downloadFile', [arg0]);
};

module.exports.startScanBeacon = function (success, error) {
    exec(success, error, 'NiceId', 'startScanBeacon', []);
};

module.exports.requestQRScanner = function (success, error) {
    exec(success, error, 'NiceId', 'requestQRScanner', []);
};

module.exports.checkPermissions = function (success, error) {
    exec(success, error, 'NiceId', 'checkPermissions', []);
};
