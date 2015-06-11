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
                var initialPath = this.initialPath ? this.initialPath : "";
                return {
                    id : this.generateUuid(),
                    initialPath: initialPath,
                    name: this.name,
                    value: this.value
                };
            },

            /**
             * @instance
             */
            createFormControl: function (config) {
                return new CategoryPickerWidget(config);
            },

            getValue: function () {
                var value = this.wrappedWidget.get("value");
                for (var property in value) {
                    if (!value.hasOwnProperty(property)) continue;
                    value = property;
                }
                return value;
            },

            setValue: function (value) {
                return this.wrappedWidget.set("value", value);
            }
        });
    });