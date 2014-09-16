define(["dojo/_base/declare",
"dijit/_WidgetBase",
"dijit/_TemplatedMixin",
"dojo/text!./templates/FilterPane.html",
"alfresco/core/Core",
"alfresco/core/CoreXhr",
"dojo/dom-construct",
"dojo/dom-class",
"dojo/dom",
"dojo/on",
"dojo/_base/lang",
"dojo/_base/array",
"dijit/registry",
"alfresco/buttons/AlfButton",
"dijit/DropDownMenu",
"dijit/MenuItem",
"dijit/form/DropDownButton",
"dojo/fx",
"dojo/fx/Toggler",
"openesdh/xsearch/Filter",
"openesdh/xsearch/_TopicsMixin"],
function(declare, _Widget, _Templated, template, Core, CoreXhr, domConstruct, domClass, dom, on, lang, array, registry, AlfButton, DropDownMenu, MenuItem, DropDownButton, coreFx, Toggler, Filter, _TopicsMixin) {
    return declare([_Widget, _Templated, Core, CoreXhr, _TopicsMixin], {
        
        templateString: template,
        
        i18nRequirements: [{i18nFile: "./i18n/FilterPane.properties"}],
        
        cssRequirements: [{cssFile:"./css/FilterPane.css"}],
        
        widgets: [],

        /**
         * An array holding the current filter widgets.
         *
         * @instance
         * @type {object[]}
         */
        filterWidgets: [],

        // Keep track of whether the filters were initially set (e.g. when page is loaded)
        _filtersSet: false,


        postCreate: function () {  
            var _this = this;
            
            this.inherited(arguments);
  
            // Setup click handlers 
            // Apply button
            this.applyButton = new AlfButton({
                label: "<span class='magenta ui-icon update'></span> " + this.message("xsearch.filterpane.apply_filters"),
                onClick: lang.hitch(this, '_onApplyButtonClick'),
                id: 'apply-filters-button'
            });
            this.applyButton.placeAt(this.applyButtonNode);
            

            // Remove all button
            this.removeAllButton = new AlfButton({
                label: "<span class='magenta ui-icon cross'></span> " + this.message("xsearch.filterpane.remove_all_filters"),
                onClick: lang.hitch(this, '_onRemoveAllButtonClick'),
                id: 'remove-all-filters-button'
            });
            this.removeAllButton.placeAt(this.removeAllButtonNode);

                        
            // Create new filter button
            this.createNewFilterButton();
            
            // Update filter list
            this.onFilterListChanged();
  
            // Setup Filter Pane Toggler
            var toggler = new Toggler({
                node: this.filtersContainerToggleNode,
                visible: true,
                showFunc: coreFx.wipeIn,
                hideFunc: coreFx.wipeOut
            });

            toggler.visible = true;

            var filtersLabel = this.message("xsearch.filterpane.filters");
            var filtersToggleButton = new AlfButton({
                label: filtersLabel + "&nbsp;&#9650;",
                onClick: function (e) {
                    if (toggler.visible) {
                        toggler.visible = false;
                        toggler.hide();
                        this.set("label", filtersLabel + "&nbsp;&#9660;");
                    } else {
                        toggler.visible = true;
                        toggler.show();
                        this.set("label", filtersLabel + "&nbsp;&#9650;");
                    }
                }
            });
            filtersToggleButton.placeAt(this.filtersToggleButtonNode);

            // Subscribe to removing filters
            this.alfSubscribe(this.FilterRemoveTopic, lang.hitch(this, "_handleFilterRemove"));
            
            // Subscribe to filters being set
            this.alfSubscribe(this.FiltersSetTopic, lang.hitch(this, "_handleFiltersSet"));
            
            console.log("FilterPane: Post create");
            console.log("Types", this.types);
        },
        
        /**
         * Adds a single filter widget with given the name.
         * Optionally, set the operator and value.
         * @param {string} filterName
         * @param {string} operator The xsearch operator
         * @param {string} value The value of the filter
         */
        addFilterWidget: function (filterName, operator, value) {
            var _this = this;

            console.log("Add filter widget");
            console.log(this.properties[filterName]);
            var filterDef = this.properties[filterName];
            filterDef.name = filterName;
            
            // Prepare operators from operatorSets definitions
            filterDef.operators = [];
            
            if (typeof filterDef.operatorSets === 'undefined') {
                // Default to equality operator set
                filterDef.operatorSets = ['equality'];
            }
            
            array.forEach(filterDef.operatorSets, function (operatorSet, i) {
               console.log('operator', operatorSet);
               console.log('operatorSets', _this.operatorSets[operatorSet]);
               filterDef.operators = filterDef.operators.concat(_this.operatorSets[operatorSet]); 
            });

            filterDef.widgetType = filterDef.control;

            console.log(filterDef);
            var filterWidget = new Filter({
                filterDef: filterDef,
                filter: {operator: operator, value: value},
                filterPane: this
            });
            filterWidget.startup();
            
            console.log(filterWidget);
            filterWidget.placeAt(_this.containerNode);
            this.filterWidgets.push(filterWidget);
        },
        
        createNewFilterButton: function () {
            var _this = this;
            
            // New filter drop-down menu
            var menu = new DropDownMenu({ style: "display: none;"});
            
            var _onNewFilterClick = function (event) {
                console.log('newFilterOnClick', this);
                _this.addFilterWidget(this.name);
                _this.onFilterListChanged();   
            };

            // Sort alphabetically by title
            this.availableFilters = this.availableFilters.sort(function (a, b) {
                return _this.properties[a].title.localeCompare(_this.properties[b].title);
            });

            // Add a menu item for each available filter
            array.forEach(this.availableFilters, function (filter, i) {
                if (typeof _this.properties[filter] == 'undefined') {
                    throw "No model definition for filter: '" + filter + "'";
                }
                var menuItem = new MenuItem({
                    label: _this.properties[filter].title,
                    name: filter,
                    onClick: _onNewFilterClick,
                    id: "new-filter-" + filter.replace(":", "-")
                });
                menu.addChild(menuItem); 
            });

            this.newFilterMenu = menu;

            this.newFilterButton = new DropDownButton({
                label: "<span class='magenta ui-icon plus add-filter-icon'></span> " + this.message("xsearch.filterpane.new_filter"),
                dropDown: this.newFilterMenu,
                id: "new-filter-button"
            });
            this.newFilterButton.placeAt(this.newFilterButtonNode);
        },
        
        _onApplyButtonClick: function (event) {
            this.applyFilters();
        },

        applyFilters: function () {
            console.log('Apply filters');

            // Build filters array
            var filters = [];

            array.forEach(this.filterWidgets, function (filterWidget, i) {
                filters.push(filterWidget.getFilter());
            });

            console.log('Filters', filters);

            this.alfPublish(this.FiltersApplyTopic, { filters: filters });
        },
        
        removeAllFilters: function () {
            array.forEach(this.filterWidgets, function (f, i) {
                f.destroyRecursive();
            });
            this.filterWidgets = [];
            this.onFilterListChanged();
        },
        
        _onRemoveAllButtonClick: function (event) {
            this.removeAllFilters();
            this.applyFilters();
        },
        
        _handleFilterRemove: function (payload) {
            console.log('handleFilterRemove', payload);

            // Destroy the filter widget and remove from filterWidgets array
            this.filterWidgets = dojo.filter(this.filterWidgets, function (item, index) {
                if (item.getName() === payload.filter) {
                    console.log("Removing filter", payload.filter);
                    item.destroyRecursive();
                    return false;
                }
                return true;
            });

            this.onFilterListChanged();
            // Apply filters if the last filter has been removed
            if (this.filterWidgets.length == 0) {
                this.applyFilters();
            }
        },
        
        _handleFiltersSet: function (payload) {
            console.log("handleFiltersSet", payload);
            if (this._filtersSet) {
                return;
            }
            this._filtersSet = true;
            
            // Acknowledge that we've got the event to set the filters
            this.alfPublish(this.FiltersSetAckTopic, {});
            
            if (payload.filters === undefined) {
                return;
            }
            
            var _this = this;
            
            this.removeAllFilters();
            
            array.forEach(payload.filters, function (filter) {
                console.log("addFilterWidget", filter);
                _this.addFilterWidget(filter.name, filter.operator, filter.value);
            });
            
            this.onFilterListChanged();
        },
        

        addFilters: function (filters) {
            var _this = this;
            
            // Create filter widgets
            array.forEach(filters, function (filterName, i) {
                _this.addFilterWidget(filterName);
            });
        },
        
        /** 
         * Returns a filter object for the given filter name, or null if no such filter is set
         * @param {string} name
         * @returns {object}
         */
        getFilterByName: function (name) {
            for (var i = 0; i < this.filterWidgets.length; i++) {
                if (this.filterWidgets[i].getName() == name) {
                    return this.filterWidgets[i].getFilter();
                }
            }
            
            return null;
        },
        
        onFilterListChanged: function () {
            // Update New filter drop-down menu
            var currentFilters = dojo.map(this.filterWidgets, function (item, index) {
                return item.getName();
            });
            var newFilters = dojo.filter(this.availableFilters, function (item, index) {
                return dojo.indexOf(currentFilters, item) === -1;
            });
            
            console.log('curFilters', currentFilters);
            console.log('newFilters', newFilters);
            
            // Update New filter drop-down menu, showing/hiding filters as needed
            array.forEach(this.newFilterMenu.getChildren(), function (menuItem, i) {
                if (dojo.indexOf(newFilters, menuItem.name) === -1) {
                    domClass.add(menuItem.domNode, "share-hidden");
                } else {
                    domClass.remove(menuItem.domNode, "share-hidden");
                }
            });
            
            // Hide the New Filter button if there are no more filters that can be added
            console.log('cur filters', currentFilters.length, 'available', this.availableFilters.length);
            if (currentFilters.length === this.availableFilters.length) {
                domClass.add(this.newFilterButton.domNode, "share-hidden");
            } else {
                domClass.remove(this.newFilterButton.domNode, "share-hidden");
            }

            // Show/hide 'Apply Filters' and enable/disable 'Remove All Filters' buttons
            if (this.filterWidgets.length == 0) {
                domClass.add(this.applyButton.domNode, "share-hidden");
                this.removeAllButton.set("disabled", true);
            } else {
                domClass.remove(this.applyButton.domNode, "share-hidden");
                this.removeAllButton.set("disabled", false);
            }
        }
    });
});
