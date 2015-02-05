/**
 *
 * @module openesdh/common/widgets/dashlets/views/UserInfoTableView
 * @extends module:alfresco/documentlibrary/views/AlfTableView
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
       "alfresco/documentlibrary/views/AlfTableView"],
    function(declare, AlfTableView) {

       return declare([AlfTableView], {

           /**
            * The i18n scope to use for this widget.
            *
            * @instance
            */
           i18nScope: "openesdh.case.CaseMembersDashlet",

           showRoles : true,

           currentData: null,

           /**
            * An array of the i18n files to use with this widget.
            *
            * @instance
            * @type {object[]}
            * @default [{i18nFile: "./i18n/CaseInfoDashlet.properties"}]
            */
           i18nRequirements: [{i18nFile: "../i18n/CaseMembersDashlet.properties"}],

          /**
           * Over rides the inherited to return the name of the view that is used when saving user view preferences.
           *
           * @instance
           * @returns {string} "userinfo_table"
           */
          getViewName: function alfresco_documentlibrary_views_AlfTableView__getViewName() {
             return "userinfo_table";
          },

          onViewShown: function () {
              this.inherited(arguments);
              if (this.showRoles) {
                  this.alfPublish("SHOW_ROLES_COLUMN", {showRole: true});
              }
          },

          //Table Headers
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
                    label: "label.role",
                    sortable: true,
                    visibilityConfig: {
                        initialValue: false,
                        rules: [{
                            topic: "SHOW_ROLES_COLUMN",
                            attribute: "showRole",
                            is:[true],
                            isNot: [false]
                        }]
                    }
                }
              }
          ],

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
                            widgets: [{ name: "openesdh/common/widgets/renderers/PersonThumbnail" }]
                         }
                      },
                      {
                         name: "alfresco/documentlibrary/views/layouts/Cell",
                         config: {
                            additionalCssClasses: "mediumpad",
                            widgets: [{
                                  name: "openesdh/common/widgets/renderers/CaseMemberName",
                                  config: {
                                     propertyToRender: "displayName"
                                  }
                           }]
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
                                     propertyToRender: "role"
                                  }
                               }
                            ],
                             visibilityConfig: {
                                 initialValue: false,
                                 rules: [{
                                     topic: "SHOW_ROLES_COLUMN",
                                     attribute: "showRole",
                                     is:[true],
                                     isNot: [false]
                                 }]
                             }
                         }
                      }
                   ]
                }
          }]

       });
    });