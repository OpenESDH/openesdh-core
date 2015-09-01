define(["dojo/_base/declare",
        "alfresco/forms/controls/BaseFormControl",
        "dojo/text!./templates/BaseFormControl.html"
        ],
    function(declare, 
            BaseFormControl, 
            template) {

        return declare([BaseFormControl], {
            
            templateString: template
            
        });
    });