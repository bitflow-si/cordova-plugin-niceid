var exec = require('cordova/exec');
module.exports.requestNiceId = function (arg0, success, error) {
    exec(success, error, 'NiceId', 'requestNiceId', [arg0]);
};

