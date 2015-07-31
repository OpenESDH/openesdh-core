define(["dojo/_base/declare",
        "alfresco/forms/controls/DojoValidationTextBox",
        "openesdh/common/widgets/controls/form/BaseFormControl",
        "dojo/text!./templates/DijitValidationTextBox.html"
        ],
    function(declare, DojoValidationTextBox, BaseFormControl, template) {

        return declare([DojoValidationTextBox, BaseFormControl], {
            
            constructor: function(){
                this.inherited(arguments);
            },
            
            getWidgetConfig: function alfresco_forms_controls_TextBox__getWidgetConfig() {
                // Return the configuration for the widget
                var placeHolder = (this.placeHolder) ? this.message(this.placeHolder) : "";
                return {
                   id : this.generateUuid(),
                   name: this.name,
                   placeHolder: placeHolder,
                   iconClass: this.iconClass,
                   templateString: template
                };
             }            
        });
    });