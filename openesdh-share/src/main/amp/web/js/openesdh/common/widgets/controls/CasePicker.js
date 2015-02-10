define([
    "dojo/_base/declare",
    "alfresco/forms/controls/DojoValidationTextBox"
], function(declare, DojoValidationTextBox) {
    return declare([DojoValidationTextBox], {

        description: "yyyymmdd-nnn..",

        validationConfig: {
            regex: "^[0-9]{4}[01][0-9][0-3][0-9][-][0-9]+$",
            errorMessage: "Invalid case number"
        },

        postCreate: function() {
            this.inherited(arguments);
        }
    });
});