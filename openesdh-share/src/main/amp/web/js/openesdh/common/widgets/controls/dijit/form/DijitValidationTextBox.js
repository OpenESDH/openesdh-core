define(["dojo/_base/declare",
        "dijit/form/ValidationTextBox",
        "dojo/dom-class",
        "dojo/text!./templates/DijitValidationTextBox.html"
        ],
    function(declare, ValidationTextBox, domClass, template) {

        return declare([ValidationTextBox], {
            
            templateString: template,
            
            constructor: function(){
                this.inherited(arguments);
            },
            
            _updatePlaceHolder: function(){
                if(!this._phspan){
                    return;
                }
                
                if (!this.textbox.value) {
                    domClass.add(this._phspan, "active");
                } else {
                    domClass.remove(this._phspan, "active"); 
                }
            }
        });
    });