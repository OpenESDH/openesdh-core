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
"openesdh/search/MagentaSingleUserSelect",
"openesdh/search/CaseFilterWidget"
],
function(declare, _Widget, _Templated, Core, CoreXhr, dom, domConstruct, domClass, array, lang, on, MagentaSingleUserSelect, CaseFilterWidget) {
    return declare([CaseFilterWidget], {
        postCreate: function () {
            this.inherited(arguments);

            // Bug in SingleUserSelect widget: We use our own overridden version
            this.filterWidget = new MagentaSingleUserSelect({});
            this.filterWidget.set('value', this.initialValue);
            this.filterWidget.placeAt(this.containerNode);
            this.filterWidget.startup();
        },
        
        // Return the filter data as an object
        getValue: function () {
            return this.filterWidget.get('value');
        }
    });
});
