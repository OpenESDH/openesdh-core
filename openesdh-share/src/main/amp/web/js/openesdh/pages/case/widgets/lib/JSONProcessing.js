/**
* @module openesdh / common / widgets / lib / JSONProcessing
* @author Torben Lauritzen
*/
define(["dojo/_base/declare",
    ],
    function (declare) {
        return declare([], {

            /**
             * Takes
             * @param payload An array of objects on the form {type: "THE_TYPE", value: "THE_VALUE"}, where the value has to be a value known to the root 'window' object.
             * @returns An array of (native) objects.
             */
            unmarshal: function (payload) {
                var result = {};
                for (var i in payload) {
                    if (i == "alfTopic") continue;
                    result[i] = new window[payload[i].type](payload[i].value);
                }
                return result;
            }
        });
    })
;
