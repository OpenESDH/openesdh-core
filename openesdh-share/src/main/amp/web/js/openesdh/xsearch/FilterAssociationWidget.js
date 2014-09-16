define(["dojo/_base/declare",
"dijit/_WidgetBase",
"dijit/_TemplatedMixin",
"alfresco/core/Core",
"alfresco/core/CoreXhr",
"dojo/dom",
"dojo/dom-construct",
"dojo/dom-class",
"dojo/_base/array",
"dojo/_base/lang",
"dojo/on",
"openesdh/xsearch/FilterWidget"
],
function(declare, _Widget, _Templated, Core, CoreXhr, dom, domConstruct, domClass, array, lang, on, FilterWidget) {
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
            domClass.add(pickerContainer, "magenta-hide-label");

            var opts = this.filterDef.widgetOptions;
            if (!opts) {
                opts = {};
            }
            
            var itemType = opts.itemType ? opts.itemType : "cm:object";
            var many = opts.multiple ? opts.multiple : false;
            var startLocation = opts.startLocation ? opts.startLocation : false;

            // TODO: internationalize
            var picker = this.createAssociationPicker(itemType, pickerId, "", this.initialValue, many, startLocation,
                (function(scope) {
                    return function(obj) {
                        console.log("Item selected:", obj.selectedItems[0]);
                        scope.selectedItems = obj.selectedItems;
                    }
                })(this));
        },
        
        // Return the filter data as an object
        getValue: function () {
            return this.selectedItems;
        },
        
        /**
         * Create a picker for associations
         *
         * @method createAssociationPicker
         * @param itemType {string} type of item to select, i.e. 'cm:object' or 'cm:person' 
         * @param containerName {string} htmlId of the element to contain the picker
         * @param value {string} initial value for the control
         * @param many {boolean} If the picker should allow choosing multiple items
         * @param startLocation {string} The path to start the picker in
         * @param label {string} Label to disply on the picker
         * @param callback {function(obj)} Function to call when the value changes
         */
        createAssociationPicker: function(itemType, containerName, label, value, many, startLocation, callback) {
            var picker = new Alfresco.module.ControlWrapper(Alfresco.util.generateDomId());
            picker.setOptions(
                {
                    type: "association",
                    container: containerName,
                    value: value,
                    label: label,
                    controlParams:
                    {
                       startLocation: startLocation,
                       multipleSelectMode: many
                    },
                    field:
                    {
                        endpointType: itemType,
                        endpointMany: many,
                        endpointMandatory: false 
                    },
                    fnValueChanged:
                    {		       
                        fn: function(obj)
                        {
                            callback(obj);
                        },
                        scope: this
                    }
                });
            picker.render();
            return picker;
        }
    });
});
