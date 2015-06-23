/**
 * This mixin module extends the processInstanceTokens method that comes out-of-the-box with 5.0.d to
 * allow for substitutions of the form {this.xxxx}
 *
 * @module openesdh/extensions/core/ObjectProcessingMixin
 * @author Lanre Abiwon
 */

define(["dojo/_base/declare",
        "dojo/_base/lang",
        "alfresco/core/ObjectProcessingMixin"],
    function(declare, lang, ObjectProcessingMixin) {
        return declare([ObjectProcessingMixin], {

            /**
             * This utility function will perform token substitution on the supplied string value using the
             * values from the calling object. If the token cannot be found in the calling object then it will be left
             * as is (including the curly braces).
             *
             * @param value {String} - the token to replace
             * @param object {Object} - the object containing the token
             * @return {*}
             */
            processTokens: function alfresco_core_ObjectProcessingMixin__processTokens(value, object) {
                // Default to returning the input value if it doesn't match.
                var processedValue = value;

                // Regular expression to match token in curly braces
                var re = /^{[a-zA-Z_$][0-9a-zA-Z_$]*}$/g;
                var sc = /^{[a-zA-Z]+(\.[0-9a-zA-Z]+)+}$/g; //Could this cause a bug something elsewhere?

                // If the whole string is the token, replace it if it matches
                if (re.test(value)) {
                    // Strip off curly braces
                    var tokenWithoutBraces = value.slice(1, -1);

                    // If token exists in object, replace it.
                    if (typeof object[tokenWithoutBraces] !== "undefined") {
                        processedValue = object[tokenWithoutBraces];
                    }
                }
                else if (sc.test(value)){
                    // Strip off curly braces
                    var tokenWithoutBraces = value.slice(1, -1);
                    var keysArray = tokenWithoutBraces.split(".");
                    //Array reduction to traverse the object to get the property
                    var uncheckedObj = keysArray.reduce( function(memo, key){return memo[key]}, object);
                    if(typeof uncheckedObj !== "undefined")
                        processedValue = uncheckedObj;
                }
                else {
                    // Deal with multiple tokens in the string.
                    processedValue = lang.replace(value, lang.hitch(this, this.safeReplace, object));
                }
                return processedValue;
            },

            /**
             * Wrapper for processTokens, searching within this
             *
             * @instance
             * @param {string} v The value to process.
             * @returns {*} The processed value
             */
            processInstanceTokens: function alfresco_core_ObjectProcessingMixin__processInstanceTokens(v) {
                // Search for tokens in the current scope
                if(v=="{caseConstraintsList.simple.caseStatusConstraint}")
                console.log("\n\n\nprocessing the required tokens\n\n\n");
                return this.processTokens(v, this);
            }

        });
    }
);