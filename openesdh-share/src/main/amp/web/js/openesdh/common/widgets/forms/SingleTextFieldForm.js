/**
 * Extends alfresco/forms/SingleTextFieldForm to
 * allow configuration of whether the text field is required.
 *
 * @extends module:alfresco/forms/SingleTextFieldForm
 */
define(["dojo/_base/declare",
        "alfresco/forms/SingleTextFieldForm"],
    function(declare, SingleTextFieldForm) {

        return declare([SingleTextFieldForm], {
            textBoxRequirementConfig: {
                initialValue: true
            },

            /**
             * Overridden to set the "widgets" attribute to be a single text box.
             *
             * @instance
             */
            postMixInProperties: function alfresco_forms_SingleTextFieldForm__postMixInProperties() {
                this.inherited(arguments);
                this.widgets[0].config.requirementConfig = this.textBoxRequirementConfig;
            }
        });
    });