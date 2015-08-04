define(["dojo/_base/declare",
        "dijit/form/ValidationTextBox",
        "openesdh/common/widgets/controls/dijit/form/DijitTextBox",
        "dojo/text!./templates/DijitValidationTextBox.html"
        ],
    function(declare, ValidationTextBox, DijitTextBox, template) {

        return declare([DijitTextBox, ValidationTextBox], {
            
            templateString: template,
            
            baseClass: "",
            
            constructor: function(){
                this.inherited(arguments);
            }
        });
    });