/**
 *
 * @module openesdh/common/widgets/views/NotesView
 * @extends module:alfresco/documentlibrary/views/AlfDocumentListView
 * @author Seth Yastrov
 */
define(["dojo/_base/declare",
        "alfresco/documentlibrary/views/AlfDocumentListView"],
    function (declare, AlfDocumentListView) {

        return declare([AlfDocumentListView], {
            widgetsForHeader: null,

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
                                additionalCssClasses: "mediumpad",
                                widgets: [
                                    {
                                        name: "alfresco/renderers/Property",
                                        config: {
                                            propertyToRender: "created"
                                        }
                                    },
                                    {
                                        name: "alfresco/renderers/Property",
                                        config: {
                                            propertyToRender: "author"
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                }
            },
                {
                    name: "alfresco/documentlibrary/views/layouts/Row",
                    config: {
                        widgets: [
                            {
                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                config: {
                                    additionalCssClasses: "mediumpad",
                                    widgets: [
                                        {
                                            name: "alfresco/renderers/Property",
                                            config: {
                                                propertyToRender: "content"
                                            }
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }]

        });
    });