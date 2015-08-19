/**
 * CaseDocumentsListDashlet
 *
 * @author Jurij Rudinskij
 */
define(["dojo/_base/declare",
        "alfresco/core/Core",
        "openesdh/common/widgets/dashlets/Dashlet",
        "dojo/_base/lang",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin",
        "openesdh/common/services/PaginationService",
        "alfresco/core/I18nUtils"
    ],
    function (declare, AlfCore, Dashlet, lang, _DocumentTopicsMixin, PaginationService, I18nUtils) {
        var i18nScope = "openesdh.case.DocumentsDashlet";
        return declare([Dashlet, PaginationService, _DocumentTopicsMixin], {

            cssRequirements: [{cssFile: "./css/CaseDocumentsDashlet.css"}],

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: i18nScope,

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/MyCasesDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/CaseDocumentsDashlet.properties"}],

            /**
             * Allow Drag and drop
             */
            allowDnD: true,

            /**
             * set the topic to publish to publish to if dnd (See the _DocumentGridUploadMixin)
             */
            dndPublishTopic: "OE_SHOW_DND_UPLOADER",
            
            loadItemsTopic: "GET_CASE_DOCUMENTS",

            widgetsForBody: [
                {
                    name: "alfresco/lists/AlfSortablePaginatedList",
                    config: {
                        loadDataPublishTopic: "GET_CASE_DOCUMENTS",
                        loadDataPublishPayload: null,
                        widgets:[{
                            name: "openesdh/pages/case/widgets/CaseDocumentsList"
                        }]
                    }
                }
            ],
            
            widgetsForFooterBarActions: [
                {
                    name: "alfresco/layout/LeftAndRight",
                    config: {
                        widgets: [
                            {
                                name: "alfresco/buttons/AlfButton",
                                align: "left",
                                config: {
                                    label: I18nUtils.msg(i18nScope, "dashlet.button.label.document.create"),
                                    publishTopic: "OPENESDH_CASE_CREATE_DOC"
                                }
                            },
                            {
                                name: "alfresco/buttons/AlfButton",
                                align: "left",
                                config: {
                                    label: I18nUtils.msg(i18nScope, "dashlet.button.label.document.upload"),
                                    publishTopic: "OPENESDH_CASE_UPLOAD_DOC"
                                }
                            },
                            {
                                name: "alfresco/buttons/AlfButton",
                                align: "left",
                                config: {
                                    label: I18nUtils.msg(i18nScope, "dashlet.button.label.document.email"),
                                    publishTopic: "OPENESDH_CASE_EMAIL_DOC"
                                }
                            },
                            {
                                name: "openesdh/common/widgets/lists/CustomPaginator",
                                align: "right"
                            }
                        ]
                    }
                
                }                        
            ],

            constructor: function (args) {
                lang.mixin(this, args);
                // Pass in the nodeRef to the widget
                this.widgetsForBody[0].config.loadDataPublishPayload = {
                        url: "api/openesdh/casedocumentssearch?nodeRef=" + this.nodeRef
                };
            }
        });
    });