define(["dojo/_base/declare",
        "alfresco/documentlibrary/views/AlfDocumentListView",
        "alfresco/core/CoreXhr",
        "service/constants/Default",
        "alfresco/documentlibrary/views/layouts/Row",
        "dojo/_base/lang"],
    function (declare, AlfDocumentListView, CoreXhr, AlfConstants, Row, lang) {
        return declare([AlfDocumentListView, CoreXhr], {
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
             * This can be used to refresh the list so the data shows.
             */
            columnsReadyTopic: "",

            widgetsForHeader: [],
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
                this.widgets = [
                    {
                        name: "alfresco/documentlibrary/views/layouts/Row",
                        config: {
                            widgets: rowWidgets
                        }
                    }
                ];

                var widgetsForHeader = [];

                for (var i = 0; i < columns.length; i++) {
                    var property = columns[i];
                    widgetsForHeader.push({
                        name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                        config: {
                            label: property.label || property.name,
                            sortable: true
                        }
                    });

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