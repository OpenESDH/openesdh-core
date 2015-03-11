define(["dojo/_base/declare",
"dijit/_WidgetBase",
"dijit/_TemplatedMixin",
"dojo/text!./templates/Filter.html",
"alfresco/core/Core",
"alfresco/core/CoreXhr",
"dojo/dom",
"dojo/dom-construct",
"dojo/dom-class",
"dojo/_base/array",
"dojo/_base/lang",
"dojo/on",
"alfresco/buttons/AlfButton",

"openesdh/xsearch/FilterSelectWidget",
"openesdh/xsearch/FilterTextWidget",
"openesdh/xsearch/FilterAuthorityWidget",
"openesdh/xsearch/FilterAssociationWidget",
"openesdh/xsearch/FilterDateRangeWidget",

"openesdh/xsearch/_TopicsMixin"
],
function(declare, _Widget, _Templated, template, Core, CoreXhr, dom, domConstruct, domClass, array, lang, on, AlfButton,
FilterSelectWidget, FilterTextWidget, FilterAuthorityWidget, FilterAssociationWidget, FilterDateRangeWidget,
_TopicsMixin) {
    return declare([_Widget, _Templated, Core, CoreXhr, _TopicsMixin], {
        
        templateString: template,
        
        cssRequirements: [{cssFile:"./css/Filter.css"}],
        
        
        // The widget containing the filter
        filterWidget: null,
        
        postCreate: function () {
            this.inherited(arguments);
            
            var _this = this;

            var removeButton = new AlfButton({
                label: "<span class='magenta ui-icon cross'>Remove filter</span>",
                onClick: lang.hitch(this, '_onRemoveFilterClick')
            });
            removeButton.placeAt(this.removeButtonNode);
            
            // Generate operator selection choices
            array.forEach(this.filterDef.operators, function (operator, i) {
                domConstruct.create("option", {
                    innerHTML: operator.name,
                    value: operator.value
                }, _this.operatorSelect);
            });
            
            // Set the inital operator
            if (this.filter.operator) {
                this.operatorSelect.value = this.filter.operator;
            }

            var init = {filterDef: this.filterDef, initialValue: this.filter.value, filterPane: this.filterPane};

            // Create filter widget based on the chosen widget type
            require(["openesdh/xsearch/" + this.filterDef.widgetType], function (FilterWidget) {
                _this.filterWidget = FilterWidget(init);
            });
            
            this.filterWidget.placeAt(this.widgetNode);
            this.filterWidget.startup();
            
            this.alfSubscribe("XSEARCH_FILTER_WIDGET_ON_CHANGE", lang.hitch(this, "onFilterWidgetChange"));
                     
            on(this.operatorSelect, "change", function (){
                _this.alfLog('debug', 'Operator Change!');
                _this.onFilterWidgetChange();
            });
        },
        
        onFilterWidgetChange: function (payload) {
            this.alfPublish('XSEARCH_FILTER_ON_CHANGE', this.getFilter());
        },
        
        getName: function () {
            return this.filterDef.name;
        },

        // Return the filter data as an object
        getFilter: function () {
            var value = this.filterWidget.getValue();
            
            return {
                name: this.filterDef.name,
                value: value,
                operator: this.operatorSelect.value
            };
        },
        
        destroy: function () {
            this.inherited(arguments);
            // Destroy the filter widget
            if (this.filterWidget) {
                this.filterWidget.destroyRecursive();
            }
        },
        
        _onRemoveFilterClick: function (event) {
            var payload = this.filterDef.name;
            this.alfPublish(this.FilterRemoveTopic, { filter: payload });
        }
    });
});
