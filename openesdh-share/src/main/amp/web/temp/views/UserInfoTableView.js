/**
 *
 * @module alfresco/documentlibrary/views/AlfTableView
 * @extends module:alfresco/documentlibrary/views/AlfDocumentListView
 * @author Dave Draper
 */
define(["dojo/_base/declare",
       "alfresco/documentlibrary/views/AlfTableView"],
    function(declare, AlfTableView, template) {

       return declare([AlfTableView], {

          widgetsForHeader: [
                          {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "",
                   sortable: false
                }
             },
             {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "label.name",
                   sortable: true
                }
             },
             {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "label.title",
                   sortable: true
                }
             },
             {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "label.description",
                   sortable: true
                }
             },
             {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "label.creator",
                   sortable: true
                }
             },
             {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "label.created",
                   sortable: true
                }
             },
             {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "label.modifier",
                   sortable: true
                }
             },
             {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "label.modified",
                   sortable: true
                }
             },
             {
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                   label: "",
                   sortable: false
                }
             }
          ],


          /**
           * The definition of how a single item is represented in the view.
           *
           * @instance
           * @type {object[]}
           */
          widgets: [
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
                                  name: "alfresco/renderers/Selector"
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/Indicators"
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/InlineEditProperty",
                                  config: {
                                     propertyToRender: "node.properties.cm:name",
                                     postParam: "prop_cm_name",
                                     renderAsLink: true
                                  }
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/InlineEditProperty",
                                  config: {
                                     propertyToRender: "node.properties.cm:title",
                                     postParam: "prop_cm_title"
                                  }
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/InlineEditProperty",
                                  config: {
                                     propertyToRender: "node.properties.cm:description",
                                     postParam: "prop_cm_description"
                                  }
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/Property",
                                  config: {
                                     propertyToRender: "node.properties.cm:creator",
                                     postParam: "prop_cm_creator"
                                  }
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/Property",
                                  config: {
                                     propertyToRender: "node.properties.cm:created",
                                     postParam: "prop_cm_created"
                                  }
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/Property",
                                  config: {
                                     propertyToRender: "node.properties.cm:modifier",
                                     postParam: "prop_cm_modifier"
                                  }
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/Property",
                                  config: {
                                     propertyToRender: "node.properties.cm:modified",
                                     postParam: "prop_cm_modified"
                                  }
                               }
                            ]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [
                               {
                                  name: "alfresco/renderers/Actions"
                               }
                            ]
                         }
                      }
                   ]
                }
             }
          ]
       });
    });