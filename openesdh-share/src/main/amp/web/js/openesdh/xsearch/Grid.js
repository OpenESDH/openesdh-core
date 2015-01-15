/**
 * A grid to show search results in.
 * @module openesdh/xsearch/Grid
 * @extends dijit/_WidgetBase
 * @mixes dijit/_TemplatedMixin
 * @mixes alfresco/core/Core
 * @mixes alfresco/core/CoreXhr
 * @mixes module:openesdh/xsearch/_TopicsMixin
 */
define(["dojo/_base/declare",
"dijit/_WidgetBase",
"dijit/_TemplatedMixin",
"dojo/text!./templates/Grid.html",
"alfresco/core/Core",
"alfresco/core/CoreXhr",
"dojo/dom",
"dojo/dom-construct",
"dojo/dom-class",
"dojo/keys",
"dojo/_base/xhr",
"dojo/_base/array",
"dojo/_base/lang",
"dijit/registry",
"dojo/on",
"dojo/dom-attr",
"openesdh/xsearch/_TopicsMixin",

"alfresco/dialogs/AlfDialog",

"dojo/json",

"dojo/store/Memory",
"dojo/store/JsonRest",
"dojo/store/util/QueryResults",

"dgrid/extensions/DijitRegistry",
"dgrid/Grid",
"dgrid/Keyboard",
"dgrid/Selection",
"dgrid/extensions/Pagination",
"dojo/i18n!dgrid/extensions/nls/pagination",
"dgrid/extensions/ColumnResizer",
"dgrid/extensions/ColumnHider",
"dgrid/extensions/ColumnReorder"],
function(declare, _Widget, _Templated, template, Core, CoreXhr, dom, domConstruct, domClass, keys, xhr, array, lang, registry, on, domAttr, _TopicsMixin,
AlfDialog,
json,
Memory, JsonRest, QueryResults,
DijitRegistry, Grid, Keyboard, Selection, Pagination, i18nPagination, ColumnResizer, ColumnHider, ColumnReorder) {
    return declare([_Widget, _Templated, Core, CoreXhr, _TopicsMixin], {
        templateString: template,
        
        i18nRequirements: [
            {i18nFile: "./i18n/Grid.properties"},
            {i18nFile: "./i18n/GridActions.properties"}
        ],
        
        // Override default Dijit theme to look like Alfresco's.
        // This applies to the whole page, but I haven't found a better place to put it.
        cssRequirements: [
            {cssFile:"./css/AlfrescoStyle.css"},
            {cssFile:"./css/Grid.css"},
            {cssFile:"./css/GridActions.css"},
            {cssFile:"./css/Icons.css"},
            {cssFile:"./css/GridColumns.css"}
        ],

        /**
         * An object containing metadata about the properties that we want to show in the grid.
         *
         *
         * @instance
         * @type {object}
         * @default null
         */
        properties: null,

        /**
         * The base type that the search will be based on.
         *
         * All searches will be performed with this type.
         *
         * @instance
         * @type {string}
         * @default ""
         */
        baseType: "",

        /**
         * An object keyed by type containing metadata about types.
         * The metadata objects should contain the "title" of the type,
         * as it should be displayed in the TYPE column.
         *
         * @instance
         * @type {object}
         * @default null
         */
        types: null,

        /**
         * An array specifying which columns should be visible when the widget is loaded.
         * @instance
         * @type {string[]}
         */
        visibleColumns: null,

        /**
         * An array specifying which columns should be available for the user to show.
         *
         * @instance
         * @type {string[]}
         */
        availableColumns: null,

        /**
         * An array containing the actions which should be available on all
         * result rows.
         *
         * @instance
         * @type {object[]}
         */
        actions: null,

        /**
         * An array holding the property names of the currently visible columns.
         *
         * @instance
         * @type {string[]}
         * @default null
         */
        columns: null,

        /**
         * The Grid widget
         *
         * @instance
         * @type {dgrid/Grid}
         * @default null
         */
        grid: null,
        
        DEFAULT_SEARCH_NAME: '__default__',

        // TODO: Get from URI
        scriptURI: '/page/hdp/ws/cases',
        
        postCreate: function () {
            this.inherited(arguments);

            this.currentSearch = {};
            this.preferences = new Alfresco.service.Preferences();
        },

        startup: function () {
            var _this = this;

            this.loadInitialSearch();
            this.createGrid();
            
            // Subscribe to applying filters
            this.alfSubscribe(this.FiltersApplyTopic, lang.hitch(this, "handleFiltersApply"));
            
            // Subscribe to saving xsearch
            this.alfSubscribe("XSEARCH_SAVE_SEARCH_AS_TOPIC", lang.hitch(this, "handleSaveSearchAs"));
            this.alfSubscribe("XSEARCH_SAVE_SEARCH_AS_OK", lang.hitch(this, "handleSaveSearchAsOK"));
            
            this.alfSubscribe(this.ManageSavedSearchesTopic, lang.hitch(this, "handleManageSavedSearches"));
            this.alfSubscribe("XSEARCH_MANAGE_SAVED_SEARCHES_DELETE", lang.hitch(this, "handleManageSavedSearchesDelete"));
        },

        overrideScrollbarSizeTests: function () {
            // We override these tests for scrollbar width and height, to provide a default scrollbar size,
            // Because the tests included by dgrid sometimes fail, resulting in a size of 0.
            // This causes problems where the dgrid doesn't show the ColumnHider button (the little + icon in the upper right corner of the grid),
            // And also doesn't add enough space to accommodate the scrollbars
            // The code is pulled almost intact from dgrid's List.js
            // The proper workaround would be to figure out why the test is failing to detect the correct size in certain circumstances
            var defaultSize = 16;
            var _this = this;
            require(["dojo/has", "put-selector/put"], function (has, put) {
                function cleanupTestElement(element) {
                    element.className = "";
                    document.body.removeChild(element);
                }

                function getScrollbarSize(element, dimension) {
                    // Used by has tests for scrollbar width/height
                    put(document.body, element, ".dgrid-scrollbar-measure");
                    var size = element["offset" + dimension] - element["client" + dimension];
                    cleanupTestElement(element);
                    if (!size) {
                        // Default to 16 px if nothing is detected
                        _this.alfLog("debug", "Scrollbar " + dimension + " not detected, defaulting to " + defaultSize + "px");
                        size = defaultSize;
                    }
                    return size;
                }

                has.add("dom-scrollbar-width", function (global, doc, element) {
                    return getScrollbarSize(element, "Width");
                }, false, true);
                has.add("dom-scrollbar-height", function (global, doc, element) {
                    return getScrollbarSize(element, "Height");
                }, false, true);
            });
        },
        
        loadInitialSearch: function () {
            var _this = this;
            var searchArg = Alfresco.util.getQueryStringParameter('search');
            var all = Alfresco.util.getQueryStringParameter('all');
            if (all === null) {
                this.allSearch = false;
                // If 'all' parameter is not set, load a xsearch
                if (searchArg !== null) {
                    this.searchName = searchArg;
                } else {
                    this.searchName = this.DEFAULT_SEARCH_NAME;
                }
            } else {
                this.allSearch = true;
            }
            
            var search = this.loadSearch(this.searchName);
            this.alfLog("debug", "Loaded search", search);
            
            if (search !== null) {
                this.currentSearch = search;
                if (typeof this.currentSearch.columns == 'undefined') {
                    this.currentSearch.columns = {};
                }
                
                // Repeatedly try to set the filters, because FilterPane might not have loaded yet,
                // in which case the loaded xsearch's filters would not be set in the UI
                var intervalID = window.setInterval(function () {
                    // Publish current filters when we load the widget
                    // so the filters reflect the current xsearch
                    _this.alfPublish(_this.FiltersSetTopic, { filters: _this.currentSearch.filters });
                }, 50);
                
                // Stop trying to set the filters when we get acknowledgement from FilterPane
                this.alfSubscribe(this.FiltersSetAckTopic, function () {
                    clearInterval(intervalID);
                });
            } else {
                // All cases
                this.currentSearch = {filters: [], columns: {}, sortColumn: null, sortAsc: true};
            }
        },
        
        handleFiltersApply: function (payload) {
            this.alfLog('debug', 'handleFiltersApply', payload.filters);
            this.currentSearch.filters = payload.filters;
            this.applySearch();
        },

        handleSaveSearchAsOK: function (payload) {
            var saveAsDefault = payload.dialogContent[0].getValue();
            var name;
            if (saveAsDefault) {
                name = this.DEFAULT_SEARCH_NAME;
            } else {
                name = payload.dialogContent[1].getValue();
            }
            if (name == '') {
                // Don't allow blank names
                Alfresco.util.PopupManager.displayMessage(
                {
                    text: this.message('message.save_search.failure')
                });
            } else {
                var result = this.saveSearch(this.currentSearch, name);
                // TODO: Ask to overwrite if name already exists.
            }
        },

        handleSaveSearchAs: function (payload) {
            var defaultSearch = false;
            var searchName = '';
            if (!this.allSearch) {
                if (this.searchName == this.DEFAULT_SEARCH_NAME) {
                    defaultSearch = true;
                } else {
                    searchName = this.searchName;
                }
            }
            var dialog = new AlfDialog({
               pubSubScope: this.pubSubScope,
               title: this.message("xsearch.save_search_as.dialog.title"),
                widgetsContent: [
                    {
                       name: "alfresco/forms/controls/DojoCheckBox",
                       config: {
                          fieldId: 'save_as_default',
                          value: defaultSearch,
                          label: this.message("xsearch.save_search_as.dialog.save_as_default.label"),
                       }
                    },
                    {
                       name: "alfresco/forms/controls/DojoValidationTextBox",
                       config: {
                          fieldId: 'search_name',
                          label: this.message("xsearch.save_search_as.dialog.search_name.label"),
                          value: searchName,
                          disablementConfig: {
                              initialValue: defaultSearch,
                              rules: [
                                  {
                                      targetId: 'save_as_default',
                                      isNot: [false]
                                  }
                              ]
                          }
                       }
                    }
                ],
                widgetsButtons: [
                   {
                      name: "alfresco/buttons/AlfButton",
                      config: {
                         label: this.message("xsearch.button.ok"),
                         publishTopic: "XSEARCH_SAVE_SEARCH_AS_OK",
                         publishPayload: {}
                      }
                   },
                   {
                      name: "alfresco/buttons/AlfButton",
                      config: {
                         label: this.message("xsearch.button.cancel"),
                         publishTopic: "XSEARCH_SAVE_SEARCH_AS_CANCEL",
                         publishPayload: {}
                      }
                   }
                ]

            });
            dialog.show();
        },
        
        handleManageSavedSearches: function () {
            var _this = this;
            
            this.getSavedSearches(function (savedSearches) {
                var options = [];
                for (var x in savedSearches) {
                    if (x === _this.DEFAULT_SEARCH_NAME) {
                        continue;
                    }
                    options.push({ label: x, value: x });
                }
                if (options.length === 0) {
                    Alfresco.util.PopupManager.displayMessage(
                    {
                        text: _this.message("message.no_saved_searches")
                    });
                    return;
                }
                
                var dialog = new AlfDialog({
                   pubSubScope: _this.pubSubScope,
                   title: _this.message("xsearch.manage_saved_searches.dialog.title"),
                    widgetsContent: [
                        {
                           name: "alfresco/forms/controls/DojoSelect",
                           config: {
                              label: _this.message("xsearch.manage_saved_searches.dialog.select_search.label"),
                              options: options
                           }
                        }
                    ],
                    widgetsButtons: [
                       {
                          name: "alfresco/buttons/AlfButton",
                          config: {
                             label: _this.message("xsearch.manage_saved_searches.dialog.button.delete"),
                             publishTopic: "XSEARCH_MANAGE_SAVED_SEARCHES_DELETE",
                             publishPayload: {}
                          }
                       },
                       {
                          name: "alfresco/buttons/AlfButton",
                          config: {
                             label: _this.message("xsearch.button.cancel"),
                             publishTopic: "XSEARCH_MANAGE_SAVED_SEARCHES_CANCEL",
                             publishPayload: {}
                          }
                       }
                    ]

                });
                dialog.show();
            });
        },
        
        handleManageSavedSearchesDelete: function (payload) {
            var name = payload.dialogContent[0].getValue();
            if (name !== '') {
                var result = this.deleteSearch(name);
            }
        },
        
        getStoreQuery: function () {
            return {
                "baseType": this.baseType,
                "filters" : encodeURIComponent(json.stringify(this.currentSearch.filters))
            };
        },
        
        applySearch: function() {
            this.grid.set("query", this.getStoreQuery());
        },
        
        createGrid: function() {
            var _this = this;
            
            // Custom JsonRest store hacked to look for ETag header in the response instead of Content-Range header
            var CustomRest = JsonRest;

//            var store = new Memory({ data: data });
            var store = new CustomRest({
                target: Alfresco.constants.PROXY_URI + "api/openesdh/search",
                sortParam: "sortBy",
                idProperty: "nodeRef"
            });
            
            // Create a custom Pagination class with a distinct i18n bundle
            // In this case, we're overriding just one value, so we're
            // loading the original one to serve as a basis
            var i18nCustomized = lang.mixin({}, i18nPagination, {
                status: "${start} - ${end} af ${total} sager"
            });
            var MyPagination = declare(Pagination, {
                i18nPagination: i18nCustomized
            });

            this.overrideScrollbarSizeTests();

            // Note: We mixin _Widget because otherwise we can't call grid.placeAt
            var CustomGrid = declare([_Widget, Grid, DijitRegistry, Keyboard, Selection, MyPagination, ColumnResizer, ColumnHider, ColumnReorder]);

            // Load columns from model
            var columns = [];
            
            array.forEach(this.availableColumns, function (columnName, i) {
                var propDef = _this.properties[columnName];
                if (typeof propDef === 'undefined') {
                    this.alfLog("debug", _this.properties);
                    throw "Column " + columnName + " has no property definition.";
                }
                var dataType = null;
                if (typeof propDef.dataType !== 'undefined') {
                    dataType = propDef.dataType;
                }
                var sortable = typeof propDef.sortable !== 'undefined' ? propDef.sortable : true;
                var hidden = array.indexOf(_this.visibleColumns, columnName) === -1;
                if (typeof _this.currentSearch.columns[columnName] !== 'undefined') {
                    // Load column visibility from xsearch setting if it exists
                    hidden = !_this.currentSearch.columns[columnName];
                }
                var formatter;
                // TODO: Make formatter functions configurable
                if (dataType === 'd:datetime') {
                    formatter = function (value) {
                        if (typeof value !== 'undefined') {
                            return Alfresco.util.formatDate(Alfresco.util.fromISO8601(value), "dd/mm/yyyy");
                        } else {
                            return '';
                        }
                    };
                } else if (dataType === 'cm:authority') {
                    formatter = function (value) {
                        if (typeof value !== 'undefined') {
                            if (typeof value === "string"){
                                // Convert single value to array
                                value = [value];
                            }
                            return value.map(function (v) {
                                    if (v === null) {
                                        return '';
                                    } else {
                                        return v;
//                                        return '<a href="' + Alfresco.constants.URL_PAGECONTEXT + 'user/' + v + '/profile' + '">' + v + '</a>';
                                    }
                                }).join(", ");
                        } else {
                            return '';
                        }
                    };
                } else if (dataType === 'd:boolean') {
                    formatter = function (value) {
                        // TODO: i18n
                        return value ? 'Ja' : 'Nej';
                    };
                } else if (columnName === 'TYPE') {
                    // Format type
                    formatter = function (value) {
                        return _this.types[value].title;
                    };
                } else {
                    formatter = function (value) {
                        if (typeof value !== 'undefined') {
                            return value;
                        } else {
                            return '';
                        }
                    };
                }
                
                columns.push({ field: columnName, label: _this.properties[columnName].title, formatter: formatter, sortable: sortable, hidden: hidden});
                _this.currentSearch.columns[columnName] = !hidden;
            });

            this.alfLog("debug", "Columns " + columns);
            
            // Add Actions column
            columns.push({
                field: "nodeRef",
                label: this.message("xsearch.grid.actions"),
                renderCell: lang.hitch(this, '_renderActionsCell'),
                sortable: false,
                unhidable: true
            });

            var initialQuery = this.getStoreQuery();

            this.grid = new CustomGrid({
                store: store,
                query: initialQuery,
                columns: columns,
                noDataMessage: this.message("xsearch.grid.no_data_message"),
                loadingMessage: this.message("xsearch.grid.loading_message"),
                rowsPerPage: 25,
                pageSizeOptions: [25, 50, 75, 100],
                selectionMode: "single",
                cellNavigation: false
            });
            
            this.grid.on("dgrid-columnstatechange", function(evt){
                _this.currentSearch.columns[evt.column.field] = !evt.hidden;
                _this.alfLog("debug", "Column for field " + evt.column.field + " is now " +
                        (evt.hidden ? "hidden" : "shown"));
            });
            
            this.addKeyHandlers();
            
            this.grid.placeAt(this.containerNode);
            this.grid.startup();
        },
        
        addKeyHandlers: function() {
            // Polyfill
            if (!Object.keys) {
                Object.keys = function(o){
                    if (o !== Object(o)) {
                         throw new TypeError('Object.keys called on non-object');
                    }
                    var ret=[], p;
                    for (p in o) {
                        if (Object.prototype.hasOwnProperty.call(o,p)) {
                            ret.push(p);
                        }
                    }
                    return ret;
                };
            }

            var getSelectedNodeRef = function (grid) {
                if (grid.selection) {
                    var selectionArray = Object.keys(grid.selection);
                    if (selectionArray.length > 0) {
                        return selectionArray[0];
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            };

            var _this = this;

            array.forEach(this.actions, function (action, i) {
                var key = action.key;
                if (!key) {
                    return;
                }
                var shift = action.shift;
                _this.grid.addKeyHandler(key, function (event) {
                    if (shift && !event.shiftKey) {
                        return;
                    }
                    var nodeRef = getSelectedNodeRef(this);
                    if (nodeRef) {
                        window.location = Alfresco.constants.URL_PAGECONTEXT + action.href.replace("{nodeRef}", nodeRef);
                    }
                });
            });
        },
        
        /**
         * Renders a list of actions given the case item
         */
        _renderActionsCell: function (item, value, node, options) {
            var _this = this;
            var actionElems = [];
            array.forEach(this.actions, function (action, i) {
                var label = _this.message(action.label);
                var href = Alfresco.constants.URL_PAGECONTEXT + action.href.replace("{caseId}", item["oe:id"]);
                actionElems.push("<span><a class='magenta ui-icon grid-action action-" + action.id + "' href='" + href + "' title='" + label + "'>" + label + "</a></span>");
            });
            
            var div = '<div style="white-space: nowrap;">' + actionElems.join('') + "</div>";
            domConstruct.place(div, node);
        },

        
        setColumns: function (columns) {
            this.columns = columns;
        },

        /**
         * Loads a xsearch.
         * @param name {string} the name of the xsearch to load
         * @returns {object} The saved xsearch or null if none exists.
         */
        loadSearch: function (name) {
            var savedSearches = this.getCachedSavedSearches();
            var savedSearch = savedSearches[name];
            if (savedSearch === undefined) {
                return null;
            }
            
            return savedSearch;
        },
        
        setSavedSearches: function (savedSearches, success, failureMessage) {
            var successCallback = null, successMessage = null;
            if (typeof success === 'function') {
                successCallback = success;
            } else {
                successMessage = success;
            }
            var failureMessage = failureMessage ? failureMessage : this.message('message.save_search.failure');
            this.preferences.set('savedSearches', json.stringify(savedSearches),
                {
                  successCallback: {
                      fn: successCallback,
                      scope: this,
                  },
                  successMessage: successMessage,
                  failureMessage: failureMessage
                });
        },
        
        getCachedSavedSearches: function () {
            var preferences = this.preferences.get();
            if (!preferences) {
                return {};
            }
            var prefVal = preferences.savedSearches;
            if (!prefVal) {
                return {};
            } else {
                return json.parse(prefVal);
            }
        },
        
        getSavedSearches: function (callback) {
            var preferences = this.preferences.request('savedSearches',
                {
                    successCallback: {
                        fn: function (response) {
                            if (!response.json) {
                                callback({});
                                return;
                            }
                            
                            var prefVal = response.json.savedSearches;
                            if (!prefVal) {
                                callback({});
                            } else {
                                callback(json.parse(prefVal));
                            }
                        },
                        scope: this
                    },
                    failureMessage: this.message('message.save_search.failure')
                });
        },
        
        /**
         * Takes a xsearch object and saves it
         * @param {object} search
         * @param {string} name of the xsearch
         * @returns {undefined}
         */
        saveSearch: function (search, name) {
            var _this = this;
            this.getSavedSearches(function (savedSearches) {
                this.alfLog("debug", "Saved searches", savedSearches);

                savedSearches[name] = search;
                _this.setSavedSearches(savedSearches, function (response) {
                    // Display the success message
                    Alfresco.util.PopupManager.displayMessage(
                    {
                        text: _this.message('message.save_search.success')
                    });
                    
                    // Delay so the user can see the message, before redirecting
                    window.setTimeout(function () {
                        if (name === _this.DEFAULT_SEARCH_NAME) {
                            location.href = _this.scriptURI;
                        } else {
                            location.href = _this.scriptURI + '?search=' + encodeURIComponent(name);
                        }
                    }, 500);
                });
            });
        },
        
        deleteSearch: function (name) {
            var _this = this;
            this.getSavedSearches(function (savedSearches) {
                delete savedSearches[name];
                _this.setSavedSearches(savedSearches,
                    function (response) {
                        // Display the success message
                        Alfresco.util.PopupManager.displayMessage(
                        {
                            text: _this.message('message.delete_search.success')
                        });

                        // Delay so the user can see the message, before reloading
                        window.setTimeout(function () {
                            if (name === _this.searchName) {
                                // Redirect to the default xsearch if the current xsearch was deleted
                                location.href = _this.scriptURI;
                            } else {
                                // Reload
                                window.location.reload();
                            }
                        }, 500);
                    }, _this.message('message.delete_search.failure'));
            });
        }
    });
});