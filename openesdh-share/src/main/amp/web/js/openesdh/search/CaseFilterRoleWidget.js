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
"esdh/frontpage/CaseFilterWidget",
"esdh/frontpage/CaseFilterAuthorityWidget",
"esdh/frontpage/_CaseTopicsMixin"
],
function(declare, _Widget, _Templated, Core, CoreXhr, dom, domConstruct, domClass, array, lang, on, Select, CaseFilterWidget, CaseFilterAuthorityWidget, _CaseTopicsMixin) {
    return declare([CaseFilterWidget, _CaseTopicsMixin], {
        
        i18nRequirements: [{i18nFile: "./i18n/CaseFilterRoleWidget.properties"}],
        
        postCreate: function () {
            var _this = this;
            this.inherited(arguments);
            
            console.log("CaseFilterRoleWidget: post create");
            
            
            var typeFilter = this.filterPane.getFilterByName('type');
            var caseType = 'esdh:case';
            if (typeFilter != null) {
                console.log("type filter", typeFilter.value);

                // If there is a current type filter,
                // Initialize the role options with the roles for the currently filtered case type
                // If role information for that case type doesn't exist, use the default case type
                caseType = typeFilter.value;
            }

            this.filterDef.options = this._rolesToOptions(this.getRolesByCaseType(caseType));
            
            this.filterWidget = new Select({ options: this.filterDef.options });
            
            
            console.log("initial value", this.initialValue);
            
            var authorityInitialValue = null;
            var roleInitialValue = null;
            if (this.initialValue) {
                authorityInitialValue = this.initialValue.authority;
                roleInitialValue = this.initialValue.role;
            }
            
            if (roleInitialValue) {
                this.filterWidget.set('value', roleInitialValue);
            }
            
            this.filterWidget.placeAt(this.containerNode);
            this.filterWidget.startup(); 
            
            
            // Create authority picker widget
            
            var authorityFilterDef = this.filterDef;
            authorityFilterDef.widgetOptions = this.filterDef.widgetOptions.authorityPicker;
            
            var init = {filterDef: authorityFilterDef, initialValue: authorityInitialValue, filterPane: this.filterPane, label: this.message('label.authority-to-search')};
            this.authorityWidget = new CaseFilterAuthorityWidget(init);
            console.log('authority widget', this.authorityWidget);
            this.authorityWidget.placeAt(this.containerNode);
            this.authorityWidget.startup();
           
            this.filterWidget.on("change", function () {
                _this.publishChangeEvent();
            });
            
            this.alfSubscribe("CASE_FILTER_ON_CHANGE", lang.hitch(this, "onFilterChange"));
            this.alfSubscribe(this.CaseFilterRemoveTopic, lang.hitch(this, "onFilterPaneFilterRemoved"));
        },
        
        _rolesToOptions: function (roles) {
                var options = [];

                array.forEach(roles, function (role, i) {
                    options.push({value: role.name, label: role.label, groupsAllowed: role.groupsAllowed});
                });

                return options;
        },
        
        onFilterChange: function (payload) {
            console.log("onFilterChange", this, payload);
            if (payload.name == 'type') {
                // If the case type filter was changed, update the role widget options
                var caseType = payload.value;
                if (payload.operator === '!=') {
                    // Handle NOT operator: Just show default case type roles
                    caseType = 'esdh:case';
                }
                var roles = this.getRolesByCaseType(caseType);
                var options = this._rolesToOptions(roles);
                this.updateOptions(options);
            }
        },
        
        onFilterPaneFilterRemoved: function (payload) {
            console.log("onFilterPaneFilterRemoved", payload);
            if (payload.filter == 'type') {
                // When type filter has been removed, reset the role filter options
                this.updateOptions(this._rolesToOptions(this.getRolesByCaseType('esdh:case')));
            }
        },
        
        /**
         * Returns the roles array for the given case type, or the default roles if the case type is invalid or doesn't exist
         * @param {type} caseType
         * @returns {object}
         */
        getRolesByCaseType: function (caseType) {
            if (typeof caseType == 'undefined' || !caseType || typeof this.filterPane.caseTypes[caseType] == 'undefined') {
                caseType = 'esdh:case';
            }
            
            return this.filterPane.caseTypes[caseType].roles;
        },
        
        /**
         * Update the select widget's options with the given options.
         * @param {type} options
         * @returns {undefined}
         */
        updateOptions: function (options) {
            console.log("Update options", options);
            // We can't just call .set("options", options), because we have to call
            // .startup() afterwards and that for some reason leaves around another instance of the widget
            // So, we just destroy the widget and create it again with the new options
            var prevValue = this.filterWidget.get("value");
            this.filterWidget.destroy();
            this.filterWidget = new Select({ options: options });
            this.filterWidget.set("value", prevValue);
            this.filterWidget.placeAt(this.containerNode, "first");
            this.filterWidget.startup();
        },
        
        // Return the filter data as an object
        getValue: function () {
            return { role: this.filterWidget.get('value'), authority: this.authorityWidget.getValue() };
        }
    });
});
