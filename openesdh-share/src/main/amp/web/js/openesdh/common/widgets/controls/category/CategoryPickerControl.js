/**
 * Category picker control.
 *
 * Wraps the CategoryPickerWidget in a BaseFormControl so that it can be used
 * as a form control.
 */
define(["dojo/_base/declare",
        "alfresco/forms/controls/BaseFormControl",
        "openesdh/common/widgets/controls/category/CategoryPickerWidget"],
    function (declare, BaseFormControl, CategoryPickerWidget) {
        return declare([BaseFormControl], {
            /**
             * @instance
             */
            getWidgetConfig: function () {
                // Return the configuration for the widget
                return {
                    id : this.generateUuid(),
                    name: this.name,
                    value: this.value,
                    control: this
                };
            },

            /**
             * @instance
             */
            createFormControl: function (config) {
                return new CategoryPickerWidget(config);
            },

            getValue: function () {
                return this.wrappedWidget.get("value");
            },

            setValue: function (value) {
                return this.wrappedWidget.set("value", value);
            }
        });
    });