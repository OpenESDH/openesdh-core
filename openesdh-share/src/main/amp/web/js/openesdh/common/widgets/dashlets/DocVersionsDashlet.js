/**
 * DocVersionsDashlet
 *
 * @module openesdh/common/widgets/dashlets/DocVersionsDashlet
 * @extends openesdh/common/widgets/dashlets/Dashlet
 * @author Lanre Abiwon
 */

define(["dojo/_base/declare", "alfresco/core/Core",
        "openesdh/common/widgets/dashlets/Dashlet",
        "dojo/_base/lang", "alfresco/core/NodeUtils",
        "dijit/registry",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"],
      function(declare, AlfCore, Dashlet, lang, NodeUtils, dijitRegistry, _DocumentTopicsMixin) {

         return declare([Dashlet,_DocumentTopicsMixin], {

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.dashlet.DocVersionsDashlet",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/DocVersionsDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/DocVersionsDashlet.properties"}],

             widgetsForTitleBarActions : [{
                 id: "document_version_upload_button",
                 name: "alfresco/buttons/AlfButton",
                 config: {
                     label: "Upload New Version",// msg.get("dashlet.button.label.version.upload"),
                     // TODO: Add icon class
                     iconClass: "add-icon-16",
                     publishTopic: "OE_SHOW_VERSION_UPLOADER",
                     publishPayload: {
                         versioning: true,
                         documentNodeRef: this.documentNodeRef
                     },
                     disabled: true
                 }

             }],

             /**
              * The widgets in the body
              */
             widgetsForBody : [
                 {
                     name: "openesdh/pages/case/widgets/DocumentVersionsGrid",
                     id: "document_versions_grid",
                     config: {
                         showPagination: false,
                         sort: [
                             { attribute: 'label', descending: true }
                         ],
                         showColumnHider: false
                     }
                 }
             ],

             /**
              * The nodeRef of the document for which versions we're displaying
              */
             documentNodeRef: null,

            constructor: function (args) {
                lang.mixin(this, args);
                    this.widgetsForBody[0].config.gridRefreshTopic = this.VersionsGridRefresh;
                this.alfSubscribe(this.GetDocumentVersionsTopic, lang.hitch(this, "_setDocumentNodeRef"));
                this.alfSubscribe(this.DocumentRowSelect, lang.hitch(this, "_enableUpload"));
            },

            _setDocumentNodeRef : function(payload){
                this.documentNodeRef = payload.nodeRef;
                var actualObj = dijitRegistry.byId("document_version_upload_button");
                var gridObj = dijitRegistry.byId("document_versions_grid");
                actualObj.publishPayload.documentNodeRef = payload.nodeRef;
                gridObj.nodeRef = payload.nodeRef;
            },

             _enableUpload: function () {
                 var uploadButtonObj = dijitRegistry.byId("document_version_upload_button");
                 uploadButtonObj.setDisabled(false);
             }
         });
      });