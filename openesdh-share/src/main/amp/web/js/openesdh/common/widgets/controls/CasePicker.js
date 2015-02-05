define([
    "dojo/_base/declare",
    "alfresco/forms/controls/DojoValidationTextBox"
], function(declare, DojoValidationTextBox) {
    return declare([DojoValidationTextBox], {
        postCreate: function() {
            this.inherited(arguments);
        }
    });
});