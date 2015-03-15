define(["dojo/_base/declare",
        "alfresco/forms/controls/BaseFormControl",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "alfresco/core/Core",
        "dojo/dom",
        "dojo/dom-construct",
        "dojo/dom-class",
        "dojo/_base/array",
        "dojo/_base/lang",
        "dojo/on",
        "dojo/Deferred",
        "dojo/dom-style"
    ],
    function(declare, BaseFormControl, _Widget, _Templated, Core, dom, domConstruct, domClass, array, lang, on, Deferred, domStyle) {
        return declare([BaseFormControl], {
            value: [],

            multiple: true,
            cssRequirements: [{cssFile:"/components/object-finder/object-finder.css"}],


            getWidgetConfig: function() {
                // Return the configuration for the widget
                return {
                    id : this.generateUuid(),
                    name: this.name,
                    value: this.value
                };

            },

            isPromisedWidget: true,

            createFormControl: function(config) {


                require(["/share/modules/form/control-wrapper.js",
                         "/share/components/object-finder/object-finder.js"
                        ], lang.hitch(this, this.setupPicker));

                this.wrappedWidget = new Deferred();
                return this.wrappedWidget;
            },

            postCreate: function() {
                this.inherited(arguments);
                domStyle.set(this._titleRowNode, {display: "none"});
            },

            processValidationRules: function () {
             this.inherited(arguments);
             return true;
            },

            setupPicker: function () {

                var pickerId = Alfresco.util.generateDomId();

                // Create a container element for the picker
                var pickerContainer = domConstruct.create("span", {
                    innerHTML: '',
                    id: pickerId
                }, this.containerNode);

                var itemType = "cm:object";
                var many = this.multiple;

                // TODO: internationalize
                var picker = this.createAuthorityPicker(itemType, pickerId, this.label, this.initialValue, many,
                    (function(scope) {
                        return function(obj) {
                            scope.value = obj.selectedItems;
                            scope.onValueChangeEvent(scope.name, scope.value, obj.selectedItems);
                        };
                    })(this));

                this.wrappedWidget.resolve(picker);

            },


            // Return the filter data as an object
            getValue: function () {
                return this.value.toString();
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
                        type: "authority",
                        name: this.name,
                        container: containerName,
                        value: value,
                        label: label,
                        selectActionLabel: "",
                        selectActionLabelId: "",
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
