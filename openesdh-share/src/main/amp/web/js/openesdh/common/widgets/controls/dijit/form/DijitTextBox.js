define(["dojo/_base/declare",
        "dijit/form/TextBox",
        "dojo/dom-class"
        ],
    function(declare, TextBox, domClass) {

        return declare([TextBox], {
            
        	/* disabling default behaviour */
            _updatePlaceHolder: function(){},
            
            _onBlur: function(e){
            	if(this.disabled || this.getValue().length){ return; }
    			this.inherited(arguments);
            	
            	domClass.remove(this.domNode, "active");
    		},
            
            _onFocus: function(/*String*/ by ){
            	if(this.disabled || this.readOnly){ return; }
    			this.inherited(arguments);
    			
            	domClass.add(this.domNode, "active");
            }
        });
    });