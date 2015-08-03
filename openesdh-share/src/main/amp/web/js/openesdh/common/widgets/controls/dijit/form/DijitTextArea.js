define(["dojo/_base/declare",
        "dijit/form/SimpleTextarea",
        "openesdh/common/widgets/controls/dijit/form/DijitTextBox",
        "dojo/text!./templates/DijitTextArea.html"
        ],
    function(declare, SimpleTextarea, DijitTextBox, template) {

        return declare([DijitTextBox, SimpleTextarea], { 
            
            templateString: template,
            
            constructor: function(){
                this.inherited(arguments);
            }
        });
    });