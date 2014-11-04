define([],
    function () {
        return {
            isEmpty: function (obj) {
                for (var i in obj)
                    if (obj.hasOwnProperty(i)) return false;
                return true;
            }
        };
    });