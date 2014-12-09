define(["dojo/_base/declare",
        "alfresco/documentlibrary/views/AlfDocumentListView",
        "alfresco/core/CoreXhr",
        "service/constants/Default",
        "alfresco/documentlibrary/views/layouts/Row",
        "dojo/_base/lang"],
    function (declare, AlfDocumentListView, CoreXhr, AlfConstants, Row, lang) {
        return declare([AlfDocumentListView, CoreXhr], {

            cssRequirements: [{cssFile:"../../../css/Common.css"}],

            /**
             * Widgets to be appended to the header after the columns are loaded.
             * Could be useful for adding an "Actions" header column.
             */
            extraWidgetsForHeader: [],

            /**
             * Widgets to be appended to the row after the columns are loaded.
             * This could be useful for adding "Actions" cells.
             */
            extraRowWidgets: [],

            /**
             * Topic to publish to when the columns have loaded.
             * This can be used to load data for the list, so the data shows
             * when the columns have loaded (instead of loading data on page
             * load, when the columns might not have been loaded).
             */
            columnsReadyTopic: "",

            widgetsForHeader: [],

            /**
             * Provide a row widget here. Cells will be filled in
             * automatically.
             */
            widgets: [],

            /**
             * What item type to load columns for, e.g. cm:content.
             */
            itemType: "cm:content",

            getViewName: function () {
                return "table";
            },

            postCreate: function () {
                this.inherited(arguments);
                this.loadColumns();
            },

            loadColumns: function () {
                this.serviceXhr({
                    url: AlfConstants.URL_SERVICECONTEXT + "components/data-lists/config/columns",
                    query: {
                        itemType: this.itemType
                    },
                    method: "GET",
                    handleAs: "json",
                    successCallback: function (response, config) {
                        this._onLoadColumnsSuccess(response);
                    },
                    callbackScope: this
                });
            },

            _onLoadColumnsSuccess: function (response) {
                var columns = response.columns;

                var rowWidgets = [];

                this.widgets[0].config.widgets = rowWidgets;

                var widgetsForHeader = [];

                for (var i = 0; i < columns.length; i++) {
                    var property = columns[i];
                    // Add a header cell for each column
                    widgetsForHeader.push({
                        name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                        config: {
                            label: property.label || property.name,
                            sortValue: property.name,
                            additionalCssClasses: "cursor-pointer",
                            sortable: true
                        }
                    });

                    // Add a cell to the row for each column
                    rowWidgets.push({
                        name: "alfresco/documentlibrary/views/layouts/Cell",
                        config: {
                            additionalCssClasses: "siteName mediumpad",
                            widgets: [
                                {
                                    name: "alfresco/renderers/Property",
                                    config: {
                                        propertyToRender: property.name
                                    }
                                }
                            ]
                        }
                    });
                }

                // Append the extra widgets for the header
                this.widgetsForHeader = widgetsForHeader.concat(this.extraWidgetsForHeader);

                // Add actions
                for (var j = 0; j < this.extraRowWidgets.length; j++) {
                    rowWidgets.push(this.extraRowWidgets[j]);
                }

                // Render the header/view because we now have the columns
                this._renderHeader();
                this.renderView();
                this.alfPublish(this.columnsReadyTopic, {});
            }
        });
    });