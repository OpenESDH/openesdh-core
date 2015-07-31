define(["dojo/_base/declare",
        "alfresco/forms/controls/DojoTextarea",
        "openesdh/common/widgets/controls/form/BaseFormControl",
        "dojo/text!./templates/DijitTextArea.html"
        ],
    function(declare, DojoTextarea, BaseFormControl, template) {

        return declare([DojoTextarea, BaseFormControl], {
            
            constructor: function(){
                this.inherited(arguments);
            },
            
            getWidgetConfig: function alfresco_forms_controls_TextArea__getWidgetConfig() {
                // Return the configuration for the widget
                return {
                   id : this.generateUuid(),
                   name: this.name,
                   rows: this.rows,
                   cols: this.cols,
                   templateString: template
                };
             },            
        });
    });