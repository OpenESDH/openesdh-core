define(["dojo/_base/declare",
        "alfresco/forms/controls/DojoTextarea",
        "openesdh/common/widgets/controls/form/BaseFormControl",
        "openesdh/common/widgets/controls/dijit/form/DijitTextArea",
        "dojo/dom-class"
        ],
    function(declare, DojoTextarea, BaseFormControl, DijitTextArea, domClass) {

        return declare([DojoTextarea, BaseFormControl], {
            
            constructor: function(){
                this.inherited(arguments);
            },
            
            createFormControl: function alfresco_forms_controls_TextArea__createFormControl(config, domNode) {
                var textArea = new DijitTextArea(config);
                // Handle adding classes
                var additionalCssClasses = "";
                if (this.additionalCssClasses != null)
                {
                   additionalCssClasses = this.additionalCssClasses;
                }
                domClass.add(this.domNode, "alfresco-forms-controls-TextArea " + additionalCssClasses);
                return textArea;
             }
                        
        });
    });