var uni = uni || {};
uni.postMessage = function(options) {
    if (!options) return;
    var data = options.data;
    if (window.AndroidInterface && typeof window.AndroidInterface.getMessage === 'function') {
        if (typeof data === 'string') {
            window.AndroidInterface.getMessage(data);
        } else {
            window.AndroidInterface.getMessage(JSON.stringify(data));
        }
    }
};
