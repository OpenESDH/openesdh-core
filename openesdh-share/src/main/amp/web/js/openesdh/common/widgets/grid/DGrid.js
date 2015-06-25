/**
 * An Aikau widget which wraps dgrid.
 * @module openesdh/common/widgets/grid/DGrid
 * @extends dijit/_WidgetBase
 * @mixes dijit/_TemplatedMixin
 * @mixes alfresco/core/Core
 * @mixes alfresco/core/CoreXhr
 */
define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/DGrid.html",
        "alfresco/core/Core",
        "dojo/dom-construct",
        "dojo/dom-class",
        "dojo/keys",
        "dojo/_base/array",
        "dojo/_base/lang",
        "dojo/on",
        "dojo/store/JsonRest",
        "dgrid/extensions/DijitRegistry",
        "dgrid/Grid",
        "dgrid/OnDemandGrid",
        "dgrid/Keyboard",
        "dgrid/Selection",
        "dgrid/extensions/Pagination",
        "dojo/i18n!dgrid/extensions/nls/pagination",
        "dgrid/extensions/ColumnResizer",
        "dgrid/extensions/ColumnHider",
        "dgrid/extensions/ColumnReorder"],
    function(declare, _Widget, _Templated, template, Core, domConstruct, domClass, keys, array, lang, on,
             JsonRest, DijitRegistry, Grid, OnDemandGrid, Keyboard, Selection, Pagination, i18nPagination, ColumnResizer, ColumnHider, ColumnReorder) {
        return declare([_Widget, _Templated, Core], {
            templateString: template,

            i18nRequirements: [
                {i18nFile: "./i18n/Grid.properties"}
            ],

            cssRequirements: [
                {cssFile:"./css/Grid.css"},
                {cssFile:"./css/GridColumns.css"},

                // Override default Dijit theme to look like Alfresco's.
                // This applies to the whole page, but I haven't found a better place to put it.
                {cssFile:"./css/AlfrescoStyle.css"}
            ],

            /**
             * Additional classes to be applied to the root DOM element.
             *
             * @instance
             * @type {string}
             * @default ""
             */
            additionalCssClasses: "",

            /**
             * The URI to use for the data store for the grid.
             * This must comply with how dojo/store/JsonRest makes requests.
             * See http://dojotoolkit.org/reference-guide/1.10/dojo/store/JsonRest.html#implementing-a-rest-server
             * @instance
             * @type {string}
             */
            targetURI: "api/openesdh/search",

            /**
             * The initial query to use when making a request to the store.
             * @instance
             * @type {{object}}
             */
            query: null,

            /**
             * How to initially sort the grid.
             * See dgrid docs: https://github.com/SitePen/dgrid/wiki/List
             */
            sort: null,

            rowsPerPage: 25,

            pageSizeOptions: [25, 50, 75, 100],

            autoHeight: false,

            /**
             * An array containing the actions which should be available on all
             * result rows. If null or empty, there will be no actions column
             *
             * @instance
             * @type {object[]}
             */
            actions: null,

            allowRowSelection: true,

            showPagination: true,

            showFooter: true,

            allowColumnResize: true,

            showColumnHider: true,

            allowColumnReorder: true,

            /**
             * The topic we would like to publish on row selection
             */
            rowSelectionTopic: "GRID_ROW_SELECTED",

            /**
             * The topic we would like to publish on row deselection
             */
            rowDeselectionTopic: "GRID_ROW_DESELECTED",

            gridRefreshTopic: "GRID_REFRESH",

            /* noDataMessage: String
             * Message that shows if the grid has no data - wrap it in a
             * span with class 'dojoxGridNoData' if you want it to be
             * styled similar to the loading and error messages
             */
            noDataMessage: "grid.no_data_message",

            /**
             * The Grid widget
             *
             * @instance
             * @type {dgrid/Grid}
             * @default null
             */
            grid: null,

            /**
             * Optional function to pass to dgrid to provide custom row rendering logic.
             */
            renderRow: null,

            showHeader: true,

            constructor: function (args) {
                lang.mixin(this, args);
                this.query = {};
                this.addPolyfillObjectKeys();
            },

            postCreate: function () {
                this.inherited(arguments);
                this.alfSubscribe("GRID_SET_TARGET_URI", lang.hitch(this, "onSetTargetUri"));
                this.alfSubscribe(this.gridRefreshTopic, lang.hitch(this, "onRefresh"));
                this.alfSubscribe("GRID_SORT", lang.hitch(this, "onSort"));

                // Add actions column if there are actions
                var columns = [this.getIconsColumn()];
                columns = columns.concat(this.getColumns() );
                if (this.actions != null && this.actions.length > 0) {
                    columns.push(this.getActionsColumn());
                }
                domClass.add(this.domNode, (this.additionalCssClasses != null ? this.additionalCssClasses : ""));
                this.createGrid(columns);
            },

            /**
             * Used to programmatically sort the grid.
             *
             * Example payload: { sort: [
             *     { attribute: 'cm:created', descending: true }
             * ]
             *
             * @param payload
             */
            onSort: function (payload) {
                this.grid.set("sort", payload.sort);
            },

            onRefresh: function () {
                this.grid.refresh();
            },

            /**
             * Return the array of columns used in constructing the dgrid.
             * Intended to be overrided.
             * @returns {Array}
             */
            getColumns: function () {
                return [];
            },

            /**
             * Return the actions column.
             * @returns {{field: string, label: *, renderCell: Function, sortable: boolean, unhidable: boolean}[]}
             */
            getIconsColumn: function () {
                return {
                    field: "fileType",
                    label: "",
                    renderCell: lang.hitch(this, '_renderIconsCell'),
                    sortable: false,
                    unhidable: true
                };
            },

            /**
             * Return the actions column.
             * @returns {{field: string, label: *, renderCell: Function, sortable: boolean, unhidable: boolean}[]}
             */
            getActionsColumn: function () {
                return {
                    field: "nodeRef",
                    label: this.message("grid.actions"),
                    renderCell: lang.hitch(this, '_renderActionsCell'),
                    sortable: false,
                    unhidable: true
                };
            },

            /**
             * Create and return the dojo store used by the Grid.
             * Override if needed.
             * @returns {JsonRest}
             */
            createStore: function() {
                return new JsonRest({
                    target: Alfresco.constants.PROXY_URI + this.targetURI,
                    sortParam: "sortBy",
                    idProperty: "nodeRef"
                });
            },

            onSetTargetUri: function (payload) {
                this.grid.store.target = Alfresco.constants.PROXY_URI + payload.targetUri;
                this.grid.refresh();
            },

            /**
             * Create the dgrid instance and place it in this widget.
             */
            createGrid: function(columns) {
                this.overrideScrollbarSizeTests();

                // Pagination must be used with Grid, not OnDemandGrid
                var MyGrid = this.showPagination ? Grid : OnDemandGrid;

                var mixins = [_Widget, MyGrid, DijitRegistry, Keyboard];

                // Mixin any additional classes, if needed
                // Note: the order this is done is important
                if (this.allowRowSelection) {
                    mixins.push(Selection);
                }
                if (this.showPagination) {
                    // Create a custom Pagination class with a distinct i18n bundle
                    // In this case, we're overriding just one value, so we're
                    // loading the original one to serve as a basis
                    var i18nCustomized = lang.mixin({}, i18nPagination, {
                        // "{token}" must be transformed to "${token}",
                        // since the dojo/i18n uses a different token format than Alfresco's i18n
                        status: this.message("grid.pagination.message", {
                            "start": "${start}",
                            "end": "${end}",
                            "total": "${total}"
                        })
                    });
                    var MyPagination = declare(Pagination, {
                        i18nPagination: i18nCustomized
                    });
                    mixins.push(MyPagination);
                }
                if (this.allowColumnResize) {
                    mixins.push(ColumnResizer);
                }
                if (this.showColumnHider) {
                    mixins.push(ColumnHider);
                }
                if (this.allowColumnReorder) {
                    mixins.push(ColumnReorder);
                }

                var CustomGrid = declare(mixins);

                var options = {
                    store: this.createStore(),
                    query: this.query,
                    sort: this.sort,
                    columns: columns,
                    noDataMessage: this.message(this.noDataMessage),
                    loadingMessage: this.message("grid.loading_message"),
                    rowsPerPage: this.rowsPerPage,
                    pageSizeOptions: this.pageSizeOptions,
                    selectionMode: "single",
                    cellNavigation: false,
                    showFooter: this.showFooter,
                    className: this.autoHeight ? "dgrid-autoheight" : "",
                    showHeader: this.showHeader
                };

                if (this.renderRow != null) {
                    options.renderRow = lang.hitch(this, "renderRow");
                }

                this.grid = new CustomGrid(options);
                this.addKeyHandlers();

                this.grid.on("dgrid-select", lang.hitch(this, function(event){
                    // Only single-selection is supported, for now
                    this.alfPublish(this.rowSelectionTopic, {row: event.rows[0]});
                }));

                this.grid.on("dgrid-deselect", lang.hitch(this, function(event){
                    this.alfPublish(this.rowDeselectionTopic, {row: event.rows[0]});
                }));

                this.grid.placeAt(this.containerNode);
                this.grid.startup();
            },

            addKeyHandlers: function() {
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

                array.forEach(this.actions, lang.hitch(this, function (action, i) {
                    var key = action.key;
                    if (!key) {
                        return;
                    }
                    var shift = action.shift;
                    this.grid.addKeyHandler(key, lang.hitch(this, function (event) {
                        if (shift && !event.shiftKey) {
                            return;
                        }
                        var nodeRef = getSelectedNodeRef(this.grid);
                        if (nodeRef) {
                            var item = this.grid.row(nodeRef).data;
                            if (action.href != null) {
                                window.location = this.getActionUrl(action, item);
                            } else if (action.callback != null && typeof this[action.callback] === "function") {
                                this[action.callback].call(this, item);
                            }
                        }
                    }));
                }));
            },

            /**
             * Returns the URL for the given action, substituting values into
             * the URL for the given row item.
             * @param action
             * @param item
             * @returns {*}
             */
            getActionUrl: function (action, item) {
                return Alfresco.constants.URL_PAGECONTEXT + lang.replace(action.href, item);
            },

            /**
             * Renders a list of actions given the case item
             */
            _renderActionsCell: function (item, value, node, options) {
                var div = domConstruct.toDom('<div style="white-space: nowrap;"></div>');

                array.forEach(this.actions, lang.hitch(this, function (action, i) {
                    var actionElem;
                    var label = this.message(action.label);
                    if (action.href != null) {
                        var href = this.getActionUrl(action, item);
                        actionElem = domConstruct.toDom("<span><a class='magenta ui-icon grid-action action-" + action.id + "' href='" + href + "' title='" + label + "'>" + label + "</a></span>");
                    }
                    else if (action.callback != null && typeof this[action.callback] === "function") {
                        actionElem = domConstruct.toDom("<span><a class='magenta ui-icon grid-action action-" + action.id + "' href='#' title='" + label + "'>" + label + "</a></span>");
                        on(actionElem, "click", lang.hitch(this, function () {
                            this[action.callback].call(this, item);
                        }));
                    }
                    domConstruct.place(actionElem, div);
                }));
                domConstruct.place(div, node);
            },

            /**
             * Renders a list of actions given the case item
             */
            _renderIconsCell: function (item, value, node) {
                var fileIcon = item.name ? Alfresco.util.getFileIcon(item.name) : "generic-file-32.png";

                var div = domConstruct.toDom('<div style="white-space: nowrap;" class="icon32">' +
                '<img id="' + node + '" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/'
                + ((item.fileType) ? item.fileType+"-file-32.png" : fileIcon) + '" alt="file type image Icon" />'+'</div>');

                domConstruct.place(div, node);
            },

            /**
             * Formatter function for formatting a date.
             * @param value
             * @private
             */
            _formatDate: function (value) {
                var date = new Date(value);
                return date.toLocaleDateString();
            },

            /**
             * Formatter function for formatting a date/time.
             * @param value
             * @private
             */
            _formatDateTime: function (value) {
                var date = new Date(value);
                return date.toLocaleDateString() + " - " + date.toLocaleTimeString();
            },

            /**
             * Implement Object.keys for browsers that don't support it.
             */
            addPolyfillObjectKeys: function () {
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
            }
        });
    });