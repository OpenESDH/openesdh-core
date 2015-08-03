define(["dojo/_base/declare",
        "alfresco/forms/controls/DojoValidationTextBox",
        "openesdh/common/widgets/controls/form/BaseFormControl",
        "dojo/text!./templates/DijitValidationTextBox.html",
        "openesdh/common/widgets/controls/dijit/form/DijitValidationTextBox",
        "dojo/dom-class"
        ],
    function(declare, DojoValidationTextBox, BaseFormControl, template, DijitValidationTextBox, domClass) {

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
             },
             
             createFormControl: function alfresco_forms_controls_TextBox__createFormControl(config, /*jshint unused:false*/ domNode) {
                 var textBox = new DijitValidationTextBox(config);
                 // Handle adding classes
                 var additionalCssClasses = "";
                 if (this.additionalCssClasses)
                 {
                    additionalCssClasses = this.additionalCssClasses;
                 }
                 domClass.add(this.domNode, "alfresco-forms-controls-TextBox " + additionalCssClasses);
                 this.addIcon(textBox);
                 return textBox;
              }
        });
    });