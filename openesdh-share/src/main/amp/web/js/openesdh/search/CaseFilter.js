define(["dojo/_base/declare",
"dijit/_WidgetBase",
"dijit/_TemplatedMixin",
"dojo/text!./templates/CaseFilter.html",
"alfresco/core/Core",
"alfresco/core/CoreXhr",
"dojo/dom",
"dojo/dom-construct",
"dojo/dom-class",
"dojo/_base/array",
"dojo/_base/lang",
"dojo/on",
"alfresco/buttons/AlfButton",

"openesdh/search/CaseFilterSelectWidget",
"openesdh/search/CaseFilterTextWidget",
"openesdh/search/CaseFilterAuthorityWidget",
"openesdh/search/CaseFilterAssociationWidget",
"openesdh/search/CaseFilterSingleUserSelectWidget",
"openesdh/search/CaseFilterRoleWidget",
"openesdh/search/CaseFilterDateRangeWidget",

"openesdh/search/_CaseTopicsMixin"
],
function(declare, _Widget, _Templated, template, Core, CoreXhr, dom, domConstruct, domClass, array, lang, on, AlfButton,
CaseFilterSelectWidget, CaseFilterTextWidget, CaseFilterAuthorityWidget, CaseFilterAssociationWidget, CaseFilterSingleUserSelectWidget, CaseFilterRoleWidget, CaseFilterDateRangeWidget,
_CaseTopicsMixin) {
    return declare([_Widget, _Templated, Core, CoreXhr, _CaseTopicsMixin], {
        
        templateString: template,
        
        cssRequirements: [{cssFile:"./css/CaseFilter.css"}],
        
        
        // The widget containing the filter
        filterWidget: null,
        
        postCreate: function () {
            this.inherited(arguments);
            
            var _this = this;
            
            console.log("CaseFilter: Post create");

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

            // Create filter widget based on field type
            require(["openesdh/search/" + this.filterDef.widgetType], function (FilterWidget) {
                _this.filterWidget = FilterWidget(init);
            });
            console.log("Filter widget" + this.filterWidget);
            
            this.filterWidget.placeAt(this.widgetNode);
            this.filterWidget.startup();
            
            this.alfSubscribe("CASE_FILTER_WIDGET_ON_CHANGE", lang.hitch(this, "onFilterWidgetChange"));
                     
            on(this.operatorSelect, "change", function (){
                console.log('operator Change!');
                _this.onFilterWidgetChange();
            });
        },
        
        onFilterWidgetChange: function (payload) {
            this.alfPublish('CASE_FILTER_ON_CHANGE', this.getFilter());
        },
        
        getName: function () {
            return this.filterDef.name;
        },

        // Return the filter data as an object
        getFilter: function () {
            console.log(this.filterWidget);
            
            var value = this.filterWidget.getValue();
            
            return {
                name: this.filterDef.name,
                value: value,
                operator: this.operatorSelect.value
            };
        },
        
        destroy: function () {
            this.inherited(arguments);
            console.log("CaseFilter: destroy");
            // Destroy the filter widget
            if (this.filterWidget) {
                this.filterWidget.destroyRecursive();
            }
        },
        
        _onRemoveFilterClick: function (event) {
            console.log('remove', event);
            var payload = this.filterDef.name;
            this.alfPublish(this.CaseFilterRemoveTopic, { filter: payload });
        }
    });
});
