define(["dojo/_base/declare",
        "alfresco/forms/controls/Select",
        "alfresco/forms/controls/BaseFormControl"
    ],
    function(declare, Select, BaseFormControl) {

        return declare([Select], {
            /**
             * Override alfresco/forms/controls/Select so it does NOT call this.encodeHtml on the label.
             * We call the grandparent implementation instead.
             * @param option
             * @param index
             */
            processOptionLabel: function alfresco_forms_controls_BaseFormControl__processOptionLabel(option, index) {
                BaseFormControl.prototype.processOptionLabel.apply(this, arguments);
            }
        });
    });