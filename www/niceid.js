cordova.define("cordova-plugin-niceid", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.requestNiceId = function (arg0, success, error) {
        exec(success, error, 'NiceId', 'requestNiceId', [arg0]);
    }
});
