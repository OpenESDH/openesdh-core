define(["dojo/_base/declare",
"dijit/_WidgetBase",
"dijit/_TemplatedMixin",
"alfresco/core/Core",
"alfresco/core/CoreXhr",
"dojo/dom",
"dojo/dom-construct",
"dojo/dom-class",
"dojo/dom-attr",
"dojo/_base/array",
"dojo/_base/lang",
"dojo/on",
"openesdh/xsearch/FilterWidget"
],
function(declare, _Widget, _Templated, Core, CoreXhr, dom, domConstruct, domClass, domAttr, array, lang, on, FilterWidget) {
    return declare([FilterWidget], {
        selectedItems: [],
        
        postCreate: function () {
            this.inherited(arguments);
            
            var pickerId = Alfresco.util.generateDomId();

            // Create a container element for the picker
            var pickerContainer = domConstruct.create("span", {
                innerHTML: '',
                id: pickerId
                }, this.containerNode);
                
            // Alfresco provides no way for us to not show a label, so we have to hide it
            if (typeof this.label === 'undefined') {
                domClass.add(pickerContainer, "magenta-hide-label");
            }
                
            var opts = this.filterDef.widgetOptions;
            if (!opts) {
                opts = {};
            }
            
            var initialValue = '';
            if (this.initialValue && this.initialValue.dateRange) {
                initialValue = this.initialValue.dateRange;
            }
            
            // TODO: internationalize
            this.pickerDomId = this.createDateRangePicker(pickerId, this.label, initialValue);
        },
        
        // Return the filter data as an object
        getValue: function () {
            // Get the date range value from the hidden input element containing the value
            var valueDomId = this.pickerDomId + "_wrapper-daterange";
            var valueElem = dom.byId(valueDomId);
            if (!valueElem) {
                return {};
            }
            var value = domAttr.get(valueElem, "value");
            var valueParts = value.split("|");
            if (valueParts.length == 1) {
                valueParts = ['', ''];
            }
            return {dateRange: valueParts};
        },
        
        
        /**
         * Create a picker for date ranges
         *
         * @method createDateRangePicker
         * @param containerName {string} htmlId of the element to contain the picker
         * @param value {string} initial value for the control (array containing 2 elements: from and to date)
         * @param label {string} Label to display on the picker
         */
        createDateRangePicker: function(containerName, label, value) {
            var domId = Alfresco.util.generateDomId();
            var picker = new Alfresco.module.ControlWrapper(domId);
            picker.setOptions(
                {
                    type: "daterange",
                    container: containerName,
                    value: value,
                    label: label,
                    controlParams: {
                        defaultFrom: value[0],
                        defaultTo: value[1]
                    }
                });
            picker.render();
            return domId;
        }
    });
});
