define([
    "dojo/_base/declare",
    "alfresco/forms/controls/DojoValidationTextBox"
], function(declare, DojoValidationTextBox) {
    return declare([DojoValidationTextBox], {

//        cssRequirements: [{cssFile: "./css/CasePicker.css"}],

//        i18nRequirements: [{i18nFile: "./i18n/CasePicker.properties"}],

        description: "yyyymmdd-nnn..",

        validationConfig: {
            regex: "^[0-9]{4}[01][0-9][0-3][0-9][-][0-9]+$",
            errorMessage: "Invalid case number" //msg.get("picker.invalid-casenumber.message")
        },

        postCreate: function() {
            this.inherited(arguments);
        }
    });
});