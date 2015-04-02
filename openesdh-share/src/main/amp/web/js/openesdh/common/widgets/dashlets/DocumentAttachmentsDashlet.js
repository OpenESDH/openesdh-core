/**
 * DocumentAttachmentsDashlet
 *
 * @module openesdh/common/widgets/dashlets/DocumentAttachmentsDashlet
 * @extends openesdh/common/widgets/dashlets/Dashlet
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "alfresco/core/Core",
        "dijit/registry",
        "dojo/_base/lang",
        "openesdh/common/widgets/dashlets/Dashlet",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"
    ],
    function (declare, AlfCore, dijitRegistry, lang, Dashlet, _DocumentTopicsMixin) {

        return declare([Dashlet, _DocumentTopicsMixin], {

            cssRequirements: [{cssFile: "./css/CaseDocumentsDashlet.css"}],

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.case.DocumentAttachmentsDashlet",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/MyCasesDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/DocumentAttachmentsDashlet.properties"}],

            currentDocumentNodeRef: null,

            widgetsForTitleBarActions: [
                {
                    name: "alfresco/buttons/AlfButton",
                    id: "upload_attachment_button",
                    config: {
                        label: "Tilføj Bilag",//TODO can't seem to localise
                        // TODO: Add icon class
                        iconClass: "add-icon-16",
                        publishTopic: "OE_SHOW_ATTACHMENTS_UPLOADER",
                        disabled: true
                    }
                }
            ],

            widgetsForBody: [
                {
                    name: "openesdh/pages/case/widgets/DocumentAttachmentsGrid",
                    config: {
                        showPagination: false,
                        sort: [
                            { attribute: 'cm:name', descending: true }
                        ],
                        showColumnHider: false
                    }
                }
            ],

            constructor: function (args) {
                lang.mixin(this, args);
                //Set the row selection topic
                this.widgetsForBody[0].config.rowSelectionTopic = this.AttachmentRowSelect;
                this.widgetsForBody[0].config.rowDeselectionTopic = this.AttachmentRowDeselect;
                this.widgetsForBody[0].config.gridRefreshTopic = this.AttachmentGridRefresh;
            },
            postCreate: function () {
                this.inherited(arguments);
                var uploadButtonObj = dijitRegistry.byId("upload_attachment_button");
                uploadButtonObj.label = this.message("dashlet.button.label.attachment.upload");
            }
        });
    });