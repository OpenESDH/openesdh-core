define(["dojo/_base/declare",
        "dijit/form/TextBox",
        "dojo/dom-class"
        ],
    function(declare, TextBox, domClass) {

        return declare([TextBox], {
            
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