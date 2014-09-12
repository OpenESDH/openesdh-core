define(["dojo/_base/declare",
"dijit/_WidgetBase",
"dijit/_TemplatedMixin",
"dojo/text!./templates/FilterWidget.html",
"alfresco/core/Core",
"alfresco/core/CoreXhr",
"dojo/dom",
"dojo/dom-construct",
"dojo/dom-class",
"dojo/_base/array",
"dojo/_base/lang",
"dojo/on"],
function(declare, _Widget, _Templated, template, Core, CoreXhr, dom, domConstruct, domClass, array, lang, on) {
    return declare([_Widget, _Templated, Core, CoreXhr], {
        
        templateString: template,
        
        cssRequirements: [{cssFile:"./css/FilterWidget.css"}],
        
        // The filter definition should be passed into the widget
        filterDef: null,
        
        // The initial value to set the widget to
        initialValue: '',
        
        // The dijit widget (optional, it will be destroyed automatically)
        filterWidget: null,
        
        postCreate: function () {
            this.inherited(arguments);
            console.log("FilterWidget: Post create");
        },
        
        destroy: function () {
            // Destroy any dijit widget under us (if we have one)
            if (this.filterWidget) {
                console.log("DESTROY", this.filterWidget);
                this.filterWidget.destroy();
            }
        },
        
        /**
         * Return the filter data as an object
         * @returns {object}
         */
        getValue: function () {
            console.log("getValue");
            return null;
        },
        
        publishChangeEvent: function () {
            this.alfPublish("XSEARCH_FILTER_WIDGET_ON_CHANGE",
                {
                  name: this.filterDef.name,
                  value: this.getValue()
                });
        }
    });
});
