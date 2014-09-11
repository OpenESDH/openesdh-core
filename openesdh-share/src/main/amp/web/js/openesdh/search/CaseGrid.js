define(["dojo/_base/declare",
"dijit/_WidgetBase",
"dijit/_TemplatedMixin",
"dojo/text!./templates/CaseGrid.html",
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
"openesdh/search/_CaseTopicsMixin",

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
function(declare, _Widget, _Templated, template, Core, CoreXhr, dom, domConstruct, domClass, keys, xhr, array, lang, registry, on, domAttr, _CaseTopicsMixin,
AlfDialog,
json,
Memory, JsonRest, QueryResults,
DijitRegistry, Grid, Keyboard, Selection, Pagination, i18nPagination, ColumnResizer, ColumnHider, ColumnReorder) {
    return declare([_Widget, _Templated, Core, CoreXhr, _CaseTopicsMixin], {
        templateString: template,
        
        i18nRequirements: [{i18nFile: "./i18n/CaseGrid.properties"}],
        
        // Override default Dijit theme to look like Alfresco's.
        // This applies to the whole page, but I haven't found a better place to put it.
        cssRequirements: [{cssFile:"./css/AlfrescoStyle.css"}, {cssFile:"./css/CaseGrid.css"}],
        
        // An array holding the property names of the currently visible columns
        columns: null,
        
        // The Grid widget
        grid: null,
        
        DEFAULT_SEARCH_NAME: '__default__',

        // TODO: Get from URI
        scriptURI: '/page/hdp/ws/cases',
        
        postCreate: function () {
            console.log('CaseGrid');
            this.inherited(arguments);
            
            this.currentSearch = {};
            
            this.preferences = new Alfresco.service.Preferences();
            console.log("Saved searches", this.getCachedSavedSearches());
        },

        startup: function () {
            console.log('CaseGrid startup');
            var _this = this;

            this.loadInitialSearch();
            this.createGrid();
            
            // Subscribe to applying filters
            this.alfSubscribe(this.CaseFiltersApplyTopic, lang.hitch(this, "handleFiltersApply"));
            
            // Subscribe to saving search
            this.alfSubscribe("CASE_SAVE_SEARCH_AS_TOPIC", lang.hitch(this, "handleSaveSearchAs"));
            this.alfSubscribe("CASEGRID_SAVE_SEARCH_AS_OK", lang.hitch(this, "handleSaveSearchAsOK"));
            
            this.alfSubscribe(this.CaseManageSavedSearchesTopic, lang.hitch(this, "handleManageSavedSearches"));
            this.alfSubscribe("CASEGRID_MANAGE_SAVED_SEARCHES_DELETE", lang.hitch(this, "handleManageSavedSearchesDelete"));
        },

        overrideScrollbarSizeTests: function () {
            // We override these tests for scrollbar width and height, to provide a default scrollbar size,
            // Because the tests included by dgrid sometimes fail, resulting in a size of 0.
            // This causes problems where the dgrid doesn't show the ColumnHider button (the little + icon in the upper right corner of the grid),
            // And also doesn't add enough space to accommodate the scrollbars
            // The code is pulled almost intact from dgrid's List.js
            // The proper workaround would be to figure out why the test is failing to detect the correct size in certain circumstances
            var defaultSize = 16;
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
                        console.log("Scrollbar " + dimension + " not detected, defaulting to " + defaultSize + "px");
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
                // If 'all' parameter is not set, load a search
                if (searchArg !== null) {
                    this.searchName = searchArg;
                } else {
                    this.searchName = this.DEFAULT_SEARCH_NAME;
                }
            } else {
                this.allSearch = true;
            }
            
            var search = this.loadSearch(this.searchName);
            console.log("Loaded search", search);
            
            if (search !== null) {
                this.currentSearch = search;
                if (typeof this.currentSearch.columns == 'undefined') {
                    this.currentSearch.columns = {};
                }
                
                // Repeatedly try to set the filters, because CaseFilterPane might not have loaded yet,
                // in which case the loaded search's filters would not be set in the UI
                var intervalID = window.setInterval(function () {
                    // Publish current filters when we load the widget
                    // so the filters reflect the current search
                    _this.alfPublish(_this.CaseFiltersSetTopic, { filters: _this.currentSearch.filters });
                }, 50);
                
                // Stop trying to set the filters when we get acknowledgement from CaseFilterPane
                this.alfSubscribe(this.CaseFiltersSetAckTopic, function () {
                    clearInterval(intervalID);
                });
            } else {
                // All cases
                this.currentSearch = {filters: [], columns: {}, sortColumn: null, sortAsc: true};
            }
        },
        
        handleFiltersApply: function (payload) {
            console.log('handleFiltersApply', payload.filters);
            this.currentSearch.filters = payload.filters;
            this.applySearch();
        },

        handleSaveSearchAsOK: function (payload) {
            console.log("Dialog OK clicked");
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
               title: this.message("casegrid.save_search_as.dialog.title"),
                widgetsContent: [
                    {
                       name: "alfresco/forms/controls/DojoCheckBox",
                       config: {
                          fieldId: 'save_as_default',
                          value: defaultSearch,
                          label: this.message("casegrid.save_search_as.dialog.save_as_default.label"),
                       }
                    },
                    {
                       name: "alfresco/forms/controls/DojoValidationTextBox",
                       config: {
                          fieldId: 'search_name',
                          label: this.message("casegrid.save_search_as.dialog.search_name.label"),
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
                         label: this.message("casegrid.button.ok"),
                         publishTopic: "CASEGRID_SAVE_SEARCH_AS_OK",
                         publishPayload: {}
                      }
                   },
                   {
                      name: "alfresco/buttons/AlfButton",
                      config: {
                         label: this.message("casegrid.button.cancel"),
                         publishTopic: "CASEGRID_SAVE_SEARCH_AS_CANCEL",
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
                console.log("handleManageSavedSearches callback, saved searches", savedSearches);
                var options = [];
                for (var x in savedSearches) {
                    if (x === _this.DEFAULT_SEARCH_NAME) {
                        continue;
                    }
                    options.push({ label: x, value: x });
                }
                console.log(options);
                if (options.length === 0) {
                    Alfresco.util.PopupManager.displayMessage(
                    {
                        text: _this.message("message.no_saved_searches")
                    });
                    return;
                }
                
                var dialog = new AlfDialog({
                   pubSubScope: _this.pubSubScope,
                   title: _this.message("casegrid.manage_saved_searches.dialog.title"),
                    widgetsContent: [
                        {
                           name: "alfresco/forms/controls/DojoSelect",
                           config: {
                              label: _this.message("casegrid.manage_saved_searches.dialog.select_search.label"),
                              options: options
                           }
                        }
                    ],
                    widgetsButtons: [
                       {
                          name: "alfresco/buttons/AlfButton",
                          config: {
                             label: _this.message("casegrid.manage_saved_searches.dialog.button.delete"),
                             publishTopic: "CASEGRID_MANAGE_SAVED_SEARCHES_DELETE",
                             publishPayload: {}
                          }
                       },
                       {
                          name: "alfresco/buttons/AlfButton",
                          config: {
                             label: _this.message("casegrid.button.cancel"),
                             publishTopic: "CASEGRID_MANAGE_SAVED_SEARCHES_CANCEL",
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
            console.log("Delete search", name);
            if (name !== '') {
                var result = this.deleteSearch(name);
            }
        },
        
        getStoreQuery: function () {
            return {
                "baseType": this.baseType,
                "filters" : json.stringify(this.currentSearch.filters)
            };
        },
        
        applySearch: function() {
            this.grid.set("query", this.getStoreQuery());
        },
        
        createGrid: function() {
            var _this = this;
            
            // Custom JsonRest store hacked to look for ETag header in the response instead of Content-Range header
            var CustomRest = JsonRest;
            
            console.log("CREATING GRID");
//            var data = [
//                { id: 1, first: "Bob", last: "Barker", age: 89 },
//                { id: 2, first: "Vanna", last: "White", age: 55 },
//                { id: 3, first: "Pat", last: "Sajak", age: 65 }
//            ];
            
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
                    console.log(_this.properties);
                    throw "Column " + columnName + " has no property definition.";
                }
                var dataType = null;
                if (typeof propDef.dataType !== 'undefined') {
                    dataType = propDef.dataType;
                }
                var sortable = typeof propDef.sortable !== 'undefined' ? propDef.sortable : true;
                var hidden = array.indexOf(_this.visibleColumns, columnName) === -1;
                if (typeof _this.currentSearch.columns[columnName] !== 'undefined') {
                    // Load column visibility from search setting if it exists
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

            console.log("Columns " + columns);
            
            // Add Actions column
            columns.push({
                field: "nodeRef",
                label: this.message("casegrid.actions"),
                renderCell: lang.hitch(this, '_renderActionsCell'),
                sortable: false,
                unhidable: true
            });

            var initialQuery = this.getStoreQuery();

            this.grid = new CustomGrid({
                store: store,
                query: initialQuery,
                columns: columns,
                noDataMessage: this.message("casegrid.no_data_message"),
                loadingMessage: this.message("casegrid.loading_message"),
                rowsPerPage: 25,
                pageSizeOptions: [25, 50, 75, 100],
                selectionMode: "single",
                cellNavigation: false
            });
            
            this.grid.on("dgrid-columnstatechange", function(evt){
                _this.currentSearch.columns[evt.column.field] = !evt.hidden;
                console.log("Column for field " + evt.column.field + " is now " +
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

            // TODO: Make keyboard shortcuts configurable
            // ENTER
            this.grid.addKeyHandler(keys.ENTER, function (event) {
                var nodeRef = getSelectedNodeRef(this);
                if (nodeRef) {    
                    // Navigate to the node details
                    window.location = Alfresco.constants.URL_PAGECONTEXT + "folder-details?nodeRef=" + nodeRef;
                }
            });
            
            // Shift+E
            this.grid.addKeyHandler(69, function (event) {
                if (event.shiftKey) {
                    var nodeRef = getSelectedNodeRef(this);
                    if (nodeRef) {    
                        // Navigate to the node edit page
                        window.location = Alfresco.constants.URL_PAGECONTEXT + "edit-metadata?nodeRef=" + nodeRef;
                    }
                }
            });
        },
        
        /**
         * Renders a list of actions given the case item.
         */
        _renderActionsCell: function (item, value, node, options) {
            // TODO: Make configurable
            var actions = [
                {path: "folder-details?nodeRef=" + item.nodeRef, label: this.message("casegrid.goto_case")},
                {path: "edit-metadata?nodeRef=" + item.nodeRef, label: this.message("casegrid.edit_case")},
                {path: "repository#filter=path|%2FSager%2FAlle%2520sager%2F" + item['cm:name'], label: this.message("casegrid.case_documents")}
            ];
            
            var actionElems = [];
            array.forEach(actions, function (action, i) {
                actionElems.push("<span><a class='magenta ui-icon action" + i + "' href='" + Alfresco.constants.URL_PAGECONTEXT + action.path + "' title='" + action.label + "'>" + action.label + "</a></span>");
            });
            
            var div = '<div style="white-space: nowrap;">' + actionElems.join('') + "</div>";
            domConstruct.place(div, node);
        },

        
        setColumns: function (columns) {
            this.columns = columns;
        },
        
         
        setData: function (data) {
           this.currentData = data;
        },
        
        /**
         * Loads a search.
         * @param name {string} the name of the search to load
         * @returns {object} The saved search or null if none exists.
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
            this.preferences.set('savedCaseSearches', json.stringify(savedSearches),
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
            var prefVal = preferences.savedCaseSearches;
            if (!prefVal) {
                return {};
            } else {
                return json.parse(prefVal);
            }
        },
        
        getSavedSearches: function (callback) {
            var preferences = this.preferences.request('savedCaseSearches',
                {
                    successCallback: {
                        fn: function (response) {
                            console.log('success callback');
                            console.log(arguments);
                            
                            if (!response.json) {
                                callback({});
                                return;
                            }
                            
                            var prefVal = response.json.savedCaseSearches;
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
         * Takes a search object and saves it
         * @param {object} search
         * @param {string} name of the search
         * @returns {undefined}
         */
        saveSearch: function (search, name) {
            var _this = this;
            this.getSavedSearches(function (savedSearches) {
                console.log("Saved searches", savedSearches);

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
                                // Redirect to the default search if the current search was deleted
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