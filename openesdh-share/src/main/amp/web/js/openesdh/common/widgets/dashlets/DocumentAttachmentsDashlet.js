/**
 * DocumentAttachmentsDashlet
 *
 * @module openesdh/common/widgets/dashlets/DocumentAttachmentsDashlet
 * @extends openesdh/common/widgets/dashlets/Dashlet
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "alfresco/core/Core",
        "openesdh/common/widgets/dashlets/Dashlet",
        "dojo/_base/lang",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"
    ],
    function (declare, AlfCore, Dashlet, lang, _DocumentTopicsMixin) {

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

            widgetsForTitleBarActions: [
                {
                    name: "alfresco/buttons/AlfButton",
                    config: {
                        // TODO: Add icon class
                        iconClass: "add-icon-16",
                        publishTopic: "OE_SHOW_UPLOADER"
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
                        ]
                    }
                },
                // This widget is required to handle ALF_UPLOAD_REQUEST topics
                { name: "alfresco/upload/AlfUpload" }
            ],

            constructor: function (args) {
                lang.mixin(this, args);
                //Set the row selection topic
                this.widgetsForBody[0].config.rowSelectionTopic = this.AttachmentRowSelect;
                this.widgetsForBody[0].config.rowDeselectionTopic = this.AttachmentRowDeselect;
                this.widgetsForBody[0].config.gridRefreshTopic = this.AttachmentGridRefresh;
            }
        });
    });