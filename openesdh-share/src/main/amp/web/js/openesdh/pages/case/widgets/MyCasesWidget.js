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
        "dgrid/extensions/ColumnReorder",
        "alfresco/core/CoreWidgetProcessing",
        "alfresco/forms/controls/DojoSelect"
    ],
    function(declare, _Widget, _Templated, template, Core, CoreXhr, dom, domConstruct, domClass, keys, xhr, array, lang, registry, on, domAttr, _TopicsMixin,
             AlfDialog,
             json,
             Memory, JsonRest, QueryResults,
             DijitRegistry, Grid, Keyboard, Selection, Pagination, i18nPagination, ColumnResizer, ColumnHider, ColumnReorder,CoreWidgetProcessing,DojoSelect) {
        return declare([_Widget, _Templated, Core, CoreXhr, _TopicsMixin,CoreWidgetProcessing], {
            templateString: template,

            i18nRequirements: [
                {i18nFile: "./i18n/MyCasesWidget.properties"}
            ],

            // Override default Dijit theme to look like Alfresco's.
            // This applies to the whole page, but I haven't found a better place to put it.
            cssRequirements: [
                {cssFile:"./css/AlfrescoStyle.css"},
                {cssFile:"./css/Grid.css"},
                {cssFile:"./css/GridActions.css"},
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

            bodyNode: null,
            selectField : null,



            postCreate: function () {
                this.inherited(arguments);
                var _this = this;

                this.currentSearch = {};
                var OptionsArray = new Array();


                OptionsArray.push({"label" : _this.message("mycases.select.userinvolvedsearch"), "value" : "userinvolvedsearch"});
                OptionsArray.push({"label" : _this.message("mycases.select.lastmodifiedcases"), "value" : "lastmodifiedbymesearch"});

                OptionsObject = {options : OptionsArray};

                this.selectField = new DojoSelect(OptionsObject);
                this.selectField.placeAt(this.bodyNode);

                on(this.selectField.wrappedWidget, "change", lang.hitch(this, "handleDropdownSelect"));
            },

            startup: function () {
                this.inherited(arguments);
                var _this = this;
                this.createGrid();
            },

            handleDropdownSelect: function () {
                 var test = this.selectField.getValue();

                var CustomRest = JsonRest;


                    this.grid.store = new CustomRest({
                        target: Alfresco.constants.PROXY_URI + "api/openesdh/" + this.selectField.getValue()
                    });
                    this.grid.refresh();

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



            getStoreQuery: function () {
                return {
                    "baseType": this.baseType,
                    "filters" : encodeURIComponent(json.stringify(this.currentSearch.filters))
                };
            },



            createGrid: function() {
                var _this = this;

                // Custom JsonRest store hacked to look for ETag header in the response instead of Content-Range header
                var CustomRest = JsonRest;



                var store = new CustomRest({
                    target: Alfresco.constants.PROXY_URI + "api/openesdh/userinvolvedsearch"
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

                //this.overrideScrollbarSizeTests();

                // Note: We mixin _Widget because otherwise we can't call grid.placeAt
                var CustomGrid = declare([_Widget, Grid, DijitRegistry, Keyboard, Selection, MyPagination, ColumnResizer, ColumnHider, ColumnReorder]);


                var columns = [
                    { field: "esdh:id", label: this.message("mycases.column.id")  },
                    { field: "esdh:state", label: this.message("mycases.column.state")},
                    { field: "esdh:modified", label: this.message("mycases.column.modified") }
                ]


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
                    noDataMessage: this.message(""),
                    loadingMessage: this.message("xsearch.grid.loading_message"),
                    rowsPerPage: 25,
                    pageSizeOptions: [25, 50, 75, 100],
                    selectionMode: "single",
                    cellNavigation: false
                });

                this.grid.placeAt(this.containerNode);
                this.grid.startup();
            },


            /**
             * Renders a list of actions given the case item
             */
            _renderActionsCell: function (item, value, node, options) {
                var _this = this;
                var actionElems = [];
                array.forEach(this.actions, function (action, i) {
                    var label = _this.message(action.label);
                    var href = Alfresco.constants.URL_PAGECONTEXT + action.href.replace("{nodeRef}", item.nodeRef);
                    actionElems.push("<span><a class='magenta ui-icon grid-action action-" + action.id + "' href='" + href + "' title='" + label + "'>" + label + "</a></span>");
                });

                var div = '<div style="white-space: nowrap;">' + actionElems.join('') + "</div>";
                domConstruct.place(div, node);
            },


            setColumns: function (columns) {
                this.columns = columns;
            }
        });
    });