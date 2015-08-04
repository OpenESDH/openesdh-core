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
            }
        });
    });