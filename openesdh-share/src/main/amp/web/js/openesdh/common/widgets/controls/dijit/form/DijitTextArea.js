define(["dojo/_base/declare",
        "dijit/form/Textarea",
        "openesdh/common/widgets/controls/dijit/form/DijitTextBox",
        "dojo/text!./templates/DijitTextArea.html"
        ],
    function(declare, SimpleTextarea, DijitTextBox, template) {

        return declare([SimpleTextarea, DijitTextBox], { 
            
            templateString: template,
            
            baseClass: "",
            
            constructor: function(){
                this.inherited(arguments);
            },
            
            _onInput: function(/*Event*/ evt){
    			// summary:
    			//		Called AFTER the input event has happened
    			//		See if the placeHolder text should be removed or added while editing.
    			this.inherited(arguments);
    			//this._updatePlaceHolder();
    			
    			console.log('wsup');
    		},
        });
    });