/**
 * @module openesdh/common/widgets/controls/DojoDateExt
 * @extends alfresco/forms/controls/DojoDateTextBox
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/date/stamp",
        "alfresco/forms/controls/DojoDateTextBox"],
    function(declare, lang, stamp, DateTextBox) {
        return declare([DateTextBox], {

            /**
             * @instance
             */
            getWidgetConfig: function alfresco_forms_controls_DojoDateTextBox__getWidgetConfig() {
                // Return the configuration for the widget
                var value = ( this.value == "") ? this.value = new Date() : this.value ;
                if (this.value instanceof Date)
                {
                    value = this.value;
                }
                else if (lang.isString(this.value))
                {
                    value = stamp.fromISOString(this.value, { selector: "date" })
                }
                return {
                    id : this.id + "_CONTROL",
                    name: this.name,
                    value: value,
                    options: (this.options != null) ? this.options : []
                };
            }

        });
    });