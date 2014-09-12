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
"dijit/form/Select",
"openesdh/search/CaseFilterWidget",
"openesdh/search/_CaseTopicsMixin"
],
function(declare, _Widget, _Templated, Core, CoreXhr, dom, domConstruct, domClass, array, lang, on, Select, CaseFilterWidget, _CaseTopicsMixin) {
    return declare([CaseFilterWidget, _CaseTopicsMixin], {
        postCreate: function () {
            var _this = this;
            this.inherited(arguments);
            
            console.log("CaseFilterSelectWidget: post create");


            var options = null;
            if ("constraints" in this.filterDef) {
                array.forEach(this.filterDef.constraints, function (constraint) {
                    // Convert the array of parameters into a map
                    // because of the funny way Alfresco outputs them
                    var constraintParams = {};
                    array.forEach(constraint.parameters, function (parameter) {
                        var keys = Object.keys(parameter);
                        array.forEach(keys, function (key) {
                            constraintParams[key] = parameter[key];
                        });
                    });
                    constraint.parameters = constraintParams;
                    if (constraint.type == "LIST") {
                        if (constraint.parameters.sorted) {
                            constraint.parameters.allowedValues.sort();
                        }
                        console.log(constraint);
                        if (!options) {
                            options = [];
                        }
                        array.forEach(constraint.parameters.allowedValues, function (value) {
                            options.push({ label: value, value: value });
                        });
                    }
                });
            }
            console.log("Constraint options" + options);
            if (!options) {
                options = this.filterDef.options;
            }
            this.filterWidget = new Select({ options: options });
            this.filterWidget.set('value', this.initialValue);
            this.filterWidget.placeAt(this.containerNode);
            this.filterWidget.startup();
           
            this.publishChangeEvent();
           
            this.filterWidget.on("change", function () {
                _this.publishChangeEvent();
            });
        },

        // Return the filter data as an object
        getValue: function () {
            return this.filterWidget.get('value');
        }
    });
});
