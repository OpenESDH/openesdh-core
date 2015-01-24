/**
 * Apologies for the long name
 * @module openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox
 * @extends alfresco/forms/controls/DojoValidationTextBox
 * @author Lanre Abiwon
 */

define(["dojo/_base/declare",
        "alfresco/forms/controls/DojoValidationTextBox",
        "dojo/_base/lang",
        "dojo/dom-class",
        "dojo/dom-style"],
    function(declare, ValidationTextBox, lang, domClass, domStyle) {

        return declare([ValidationTextBox], {


            cssRequirements: [{cssFile:"./css/VariableWidthDojoValidationTextBox.css"}],

            maxLength: 10,

            width: "",

            /**
             *
             * @instance
             */
            postCreate: function alfresco_html_Spacer__postCreate() {
                this.inherited(arguments);
                if (this.width != null) {
                    domStyle.set(this.wrappedWidget.domNode, "width", "100%");
                    domStyle.set(this._controlNode, "width", this.width + "em");
                }
                this.wrappedWidget.set("maxLength", this.maxLength);
            }
        });
    });