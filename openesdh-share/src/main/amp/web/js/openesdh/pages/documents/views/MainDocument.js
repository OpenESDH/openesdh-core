define(["dojo/_base/declare",
        "alfresco/documentlibrary/views/AlfDetailedView"
    ],
    function (declare, AlfDetailedView) {
        return declare([AlfDetailedView], {

            /**
             * Implements the widget life-cycle method to add drag-and-drop upload capabilities to the root DOM node.
             * This allows files to be dragged and dropped from the operating system directly into the browser
             * and uploaded to the location represented by the document list.
             *
             * @instance
             */

            /**
             * Determines whether or not the browser supports drag and drop file upload.
             *
             * @instance
             */
            constructor: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__constructor() {
                this.inherited(arguments);
                this.dndUploadCapable = false;
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
                                    widgets: [{ name: "alfresco/renderers/Indicators" }]
                                }
                            },
                            {
                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                config: {
                                    width: "100px",
                                    widgets: [{ name: "alfresco/renderers/Thumbnail"
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
                                                            widgets: [{ name: "alfresco/renderers/LockedBanner" }]
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
                                                                { name: "alfresco/renderers/Date" },
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
                                                                { name: "alfresco/renderers/Favourite" },
                                                                { name: "alfresco/renderers/Separator" },
                                                                { name: "alfresco/renderers/Like" },
                                                                { name: "alfresco/renderers/Separator" },
                                                                { name: "alfresco/renderers/Comments" },
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
                                    widgets: [
                                        {
                                            name: "alfresco/renderers/Actions"
                                        }
                                    ]
                                }
                            }
                        ]
                    }
            }]
        });
    })
;
