define(["dojo/_base/declare",
        "alfresco/forms/controls/DojoValidationTextBox",
        "openesdh/common/widgets/controls/form/BaseFormControl",
        "openesdh/common/widgets/controls/dijit/form/DijitValidationTextBox",
        "dojo/dom-class"
        ],
    function(declare, DojoValidationTextBox, BaseFormControl, DijitValidationTextBox, domClass) {

        return declare([DojoValidationTextBox, BaseFormControl], {
            
            constructor: function(){
                this.inherited(arguments);
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