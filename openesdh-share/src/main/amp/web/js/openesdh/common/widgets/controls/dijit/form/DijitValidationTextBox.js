define(["dojo/_base/declare",
        "dijit/form/ValidationTextBox",
        "dojo/dom-class"
        ],
    function(declare, ValidationTextBox, domClass) {

        return declare([ValidationTextBox], {
            
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