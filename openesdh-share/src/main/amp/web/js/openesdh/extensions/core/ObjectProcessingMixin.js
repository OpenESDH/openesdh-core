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

        });
    });