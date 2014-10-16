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

        nonAmdDependencies: [
            "/modules/form/control-wrapper.js",
            "/components/object-finder/object-finder.js"
        ],

        cssRequirements: [{cssFile:"/components/object-finder/object-finder.css"}],
        
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
            

            
            var itemType = "cm:category";
            var many = false;

            // TODO: internationalize
            var picker = this.  createAuthorityPicker(itemType, pickerId, this.label, this.initialValue, many,
                (function(scope) {
                    return function(obj) {
                        scope.selectedItems = obj.selectedItems;
                    };
                })(this));

            alert("hej");

        },
        
        // Return the filter data as an object
        getValue: function () {
            return this.selectedItems;
        },
        
        
        /**
         * Create a picker for authorities
         *
         * @method createAuthorityPicker
         * @param itemType {string} type of item to select, i.e. 'cm:object' or 'cm:person' 
         * @param containerName {string} htmlId of the element to contain the picker
         * @param value {string} initial value for the control
         * @param many {boolean} If the picker should allow choosing multiple items
         * @param label {string} Label to disply on the picker
         * @param callback {function(obj)} Function to call when the value changes
         */
        createAuthorityPicker: function(itemType, containerName, label, value, many, callback) {
            var domId = Alfresco.util.generateDomId();
            var picker = new Alfresco.module.ControlWrapper(domId);
            picker.setOptions(
                {
                    type: "category",
                    container: containerName,
                    value: value,
                    label: label,
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
