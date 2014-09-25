define(["dojo/_base/declare",
        "dojo/dom",
        "dojo/dom-construct",
        "dojo/dom-class",
        "dojo/_base/array",
        "dojo/_base/lang",
        "dojo/on",
        "dojo/query",
        "dojo/_base/window"
    ],
    function (declare, dom, domConstruct, domClass, array, lang, on, query, win) {
        return declare(null, {
            selectedItems: [],

            nonAmdDependencies: [
                "/modules/form/control-wrapper.js",
                "/components/object-finder/object-finder.js"
            ],

            cssRequirements: [
                {cssFile: "/components/object-finder/object-finder.css"}
            ],

            /**
             * Pop up an authority picker.
             * The callback will be called when a value is chosen.
             * @param itemType
             * @param many
             * @param initialValue
             * @param callback
             */
            popupAuthorityPicker: function (itemType, many, initialValue, callback) {
                var pickerId = Alfresco.util.generateDomId();

                // Create a container element for the picker which is hidden
                var pickerContainer = domConstruct.create("span", {
                    innerHTML: '',
                    style: 'display: none',
                    id: pickerId
                }, win.body());

                var loadedCallback = {
                    fn: function () {
                        var _this = this;
                        // Wait for the picker button to show and then "click" it
                        window.setTimeout(function () {
                            var showPickerButton = query(".show-picker button", pickerContainer)[0];
                            showPickerButton.click();
                        }, 500);
                        console.log(arguments);
                    },
                    scope: this,
                    obj: {}
                };

                var picker = this.createAuthorityPicker(itemType, pickerId, "", initialValue, many, callback, loadedCallback);
                console.log(picker);
            },

            /**
             * Create a picker for authorities
             *
             * @method createAuthorityPicker
             * @param itemType {string} type of item to select, i.e. 'cm:object' or 'cm:person'
             * @param containerName {string} htmlId of the element to contain the picker
             * @param value {string} initial value for the control
             * @param many {boolean} If the picker should allow choosing multiple items
             * @param label {string} Label to display above the picker
             * @param callback {function(obj)} Function to call when the value changes
             */
            createAuthorityPicker: function (itemType, containerName, label, value, many, callback, loadedCallback) {
                var domId = Alfresco.util.generateDomId();
                var picker = new Alfresco.module.ControlWrapper(domId);
                picker.setOptions(
                    {
                        type: "authority",
                        container: containerName,
                        value: value,
                        label: label,
                        field: {
                            endpointType: itemType,
                            endpointMany: many,
                            endpointMandatory: false
                        },
                        fnValueChanged: {
                            fn: callback,
                            scope: this
                        }
                    });
                picker.render(loadedCallback);
                return picker;
            }
        });
    });
