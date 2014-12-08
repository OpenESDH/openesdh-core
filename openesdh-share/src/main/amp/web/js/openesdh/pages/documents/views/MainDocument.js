
/**
 * @module openesdh/pages/documents/views/MainDocument
 * @extends module:alfresco/documentlibrary/views/AlfDocumentListView
 * @author DarkStar1
 */

define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/_base/array",
        "alfresco/documentlibrary/views/AlfDetailedView"
    ],
    function (declare, lang, array, AlfDetailedView) {
        return declare([AlfDetailedView], {

            /**
             * Should the widget subscribe to events triggered by the documents request?
             * This should be set to true in the widget config for standalone/isolated usage.
             *
             * @instance
             * @type Boolean
             * @default true
             */
            subscribeToDocRequests: true,

            /**
             * This is the property that is used to lookup documents in the subscribed topic.
             *
             * @instance
             * @type {string}
             * @default  "response.items"
             */
            itemsProperty: "response.mainDocument",

            /**
             * Determines whether or not the browser supports drag and drop file upload.
             *
             * @instance
             */
            postCreate: function openesdh_caseCard_mainDoc_constructor() {
                this.inherited(arguments);
                this.dndUploadCapable = false;
                console.log("MainDocuments.js 41) postCreate");
            },

            /**
             * @instance The original expects an array so we override this to push the single object into an array first
             * before continuing
             * @param {object} payload The details of the documents that have been provided.
             */
            onDocumentsLoaded: function alfresco_documentlibrary_views_AlfDocumentListView__onDocumentsLoaded(payload) {
                var item = lang.getObject(this.itemsProperty, false, payload);
                var items = [];
                console.log("MainDocuments.js 64) loaded");

                if (item != null) {
                    items.push(item); //push the single object into an array.
                    array.forEach(items, lang.hitch(this, this.processItem));
                    this.setData({
                        items: items
                    });
                    this.renderView(false);
                }
                else {
                    this.alfLog("warn", "Payload contained no 'response.items' attribute", payload, this);
                }
            },

            /**
             * The definition of how a single item is represented in the view.
             *
             * @instance
             * @type {object[]}
             */
            widgets: [{
                name: "alfresco/documentlibrary/views/layouts/Row",
                config: {
                    widgets: [
                        {
                            name: "alfresco/documentlibrary/views/layouts/Cell",
                            config: {
                                width: "16px",
                                widgets: [{name: "alfresco/renderers/Indicators"}]
                            }
                        },
                        {
                            name: "alfresco/documentlibrary/views/layouts/Cell",
                            config: {
                                width: "100px",
                                widgets: [{
                                    name: "alfresco/renderers/Thumbnail"
                                }
                                ]
                            }
                        },
                        {
                            name: "alfresco/documentlibrary/views/layouts/Cell",
                            config: {
                                widgets: [{
                                    name: "alfresco/documentlibrary/views/layouts/Column",
                                    config: {
                                        widgets: [
                                            {
                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                config: {
                                                    widgets: [{name: "alfresco/renderers/LockedBanner"}]
                                                }
                                            },
                                            {
                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                config: {
                                                    widgets: [
                                                        {
                                                            name: "alfresco/renderers/InlineEditPropertyLink",
                                                            config: {
                                                                propertyToRender: "node.properties.cm:name",
                                                                postParam: "prop_cm_name",
                                                                renderSize: "large"
                                                            }
                                                        },
                                                        {
                                                            name: "alfresco/renderers/InlineEditProperty",
                                                            config: {
                                                                propertyToRender: "node.properties.cm:title",
                                                                postParam: "prop_cm_title",
                                                                renderedValuePrefix: "(",
                                                                renderedValueSuffix: ")",
                                                                renderFilter: [{
                                                                    property: "node.properties.cm:title",
                                                                    values: [""],
                                                                    negate: true
                                                                }]
                                                            }
                                                        },
                                                        {
                                                            name: "alfresco/renderers/Version",
                                                            config: {
                                                                onlyShowOnHover: true,
                                                                renderFilter: [
                                                                    {
                                                                        property: "node.isContainer",
                                                                        values: [false]
                                                                    },
                                                                    {
                                                                        property: "workingCopy.isWorkingCopy",
                                                                        values: [false],
                                                                        renderOnAbsentProperty: true
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            {
                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                config: {
                                                    widgets: [
                                                        {name: "alfresco/renderers/Date"},
                                                        {
                                                            name: "alfresco/renderers/Size",
                                                            config: {
                                                                renderFilter: [{
                                                                    property: "node.isContainer",
                                                                    values: [false]
                                                                }]
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            {
                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                config: {
                                                    widgets: [{
                                                        name: "alfresco/renderers/InlineEditProperty",
                                                        config: {
                                                            propertyToRender: "node.properties.cm:description",
                                                            postParam: "prop_cm_description",
                                                            warnIfNotAvailable: true,
                                                            warnIfNoteAvailableMessage: "no.description.message"
                                                        }
                                                    }]
                                                }
                                            },
                                            {
                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                config: {
                                                    widgets: [{
                                                        name: "alfresco/renderers/Tags",
                                                        config: {
                                                            propertyToRender: "node.properties.cm:taggable",
                                                            postParam: "prop_cm_taggable",
                                                            warnIfNotAvailable: true,
                                                            warnIfNoteAvailableMessage: "no.tags.message",
                                                            renderFilter: [{
                                                                property: "workingCopy.isWorkingCopy",
                                                                values: [false],
                                                                renderOnAbsentProperty: true
                                                            }]
                                                        }
                                                    }]
                                                }
                                            },
                                            {
                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                config: {
                                                    widgets: [{
                                                        name: "alfresco/renderers/Category",
                                                        config: {
                                                            renderFilter: [
                                                                {
                                                                    property: "node.properties.cm:categories",
                                                                    values: [null],
                                                                    negate: true
                                                                }
                                                            ]
                                                        }
                                                    }]
                                                }
                                            },
                                            {
                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                config: {
                                                    renderFilter: [{
                                                        property: "workingCopy.isWorkingCopy",
                                                        values: [false],
                                                        renderOnAbsentProperty: true
                                                    }],
                                                    widgets: [
                                                        {name: "alfresco/renderers/Favourite"},
                                                        {name: "alfresco/renderers/Separator"},
                                                        {name: "alfresco/renderers/Like"},
                                                        {name: "alfresco/renderers/Separator"},
                                                        {name: "alfresco/renderers/Comments"},
                                                        {
                                                            name: "alfresco/renderers/Separator",
                                                            config: {
                                                                renderFilter: [{
                                                                    property: "node.isContainer",
                                                                    values: [false]
                                                                }]
                                                            }
                                                        },
                                                        {
                                                            name: "alfresco/renderers/QuickShare",
                                                            config: {
                                                                renderFilter: [{
                                                                    property: "node.isContainer",
                                                                    values: [false]
                                                                }]
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                        ]
                                    }
                                }]
                            }
                        },
                        {
                            name: "alfresco/documentlibrary/views/layouts/Cell",
                            config: {
                                width: "100px",
                                widgets: [{ name: "alfresco/renderers/Actions"  }]
                            }
                        }
                    ]
                }
            }]
        });
    })
;
