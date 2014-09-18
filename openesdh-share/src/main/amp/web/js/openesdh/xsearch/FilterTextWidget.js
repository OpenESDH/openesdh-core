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
"dijit/form/TextBox",
"openesdh/xsearch/FilterWidget"
],
function(declare, _Widget, _Templated, Core, CoreXhr, dom, domConstruct, domClass, array, lang, on, TextBox, FilterWidget) {
    return declare([FilterWidget], {
        postCreate: function () {
            this.inherited(arguments);

            this.filterWidget = new TextBox({value: this.initialValue});
            this.filterWidget.placeAt(this.containerNode);
            this.filterWidget.startup();
        },
        
        // Return the filter data as an object
        getValue: function () {
            return this.filterWidget.get('value');
        }
    });
});
