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
            
            this.filterWidget = new Select({ options: this.filterDef.options });
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
