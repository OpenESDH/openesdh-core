define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin",
        "alfresco/dialogs/AlfFormDialog",
        "dojo/window"
    ],
    function (declare, AlfCore, CoreXhr, array, lang, _TopicsMixin, AlfFormDialog, win) {

        return declare([AlfCore, CoreXhr, _TopicsMixin], {

            documentsNodeRef: null,

            constructor: function (args) {
                lang.mixin(this, args);

                this.alfSubscribe("OE_SHOW_UPLOADER", lang.hitch(this, this._showUploader));
                this.alfSubscribe("OE_CASE_DOCUMENT_SERVICE_UPLOAD_REQUEST_RECEIVED", lang.hitch(this, this._onFileUploadRequest));
                this.alfSubscribe("OE_PREVIEW_DOC", lang.hitch(this, this.onPreviewDoc));
                this.alfSubscribe(this.CaseDocumentRowSelect, lang.hitch(this, this._retrieveDocumentdetails));
                this.alfSubscribe(this.GetDocumentVersionsTopicClick, lang.hitch(this, this._retrieveDocumentVersions));
            },

            onPreviewDoc: function (payload) {
                // Because the content of the previewer will load asynchronously it's important that
                // we set some dimensions for the dialog body, otherwise it will appear off-center
                var vs = win.getBox();
                this.alfPublish("ALF_CREATE_DIALOG_REQUEST", {
                    contentWidth: (vs.w*0.7) + "px",
                    contentHeight: (vs.h-64) + "px",
                    handleOverflow: false,
                    dialogTitle: payload.displayName,
                    additionalCssClasses: "no-padding",
                    widgetsContent: [
                        {
                            name: "alfresco/documentlibrary/AlfDocument",
                            config: {
                                widgets: [
                                    {
                                        name: "alfresco/preview/AlfDocumentPreview"
                                    }
                                ]
                            }
                        }
                    ],
                    widgetsButtons: [
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: this.message("button.close"),
                                publishTopic: "NO_OP"
                            }
                        }
                    ],
                    publishOnShow: [
                        {
                            publishTopic: "ALF_RETRIEVE_SINGLE_DOCUMENT_REQUEST",
                            publishPayload: {
                                nodeRef: payload.nodeRef
                            }
                        }
                    ]
                });
            },

            /**
             * This function will open a [AlfFormDialog]{@link module:alfresco/forms/AlfFormDialog} containing a
             * [file select form control]{@link module:alfresco/forms/controls/FileSelect} so that the user can
             * select one or more files to upload. When the dialog is confirmed the
             * [_onFileUploadRequest]{@link module:alfresco/services/ContentService#_onFileUploadRequest}
             * function will be called to destroy the dialog and pass the upload request on.
             *
             * @instance
             * @param {object} payload
             */
            _showUploader: function alfresco_services_ContentService__showUploader(payload) {
                this.uploadDialog = new AlfFormDialog({
                    dialogTitle: "Select files to upload",
                    dialogConfirmationButtonTitle: "Upload",
                    dialogCancellationButtonTitle: "Cancel",
                    formSubmissionTopic: "OE_CASE_DOCUMENT_SERVICE_UPLOAD_REQUEST_RECEIVED",
                    formSubmissionPayload: {
                        targetData: {
                            destination: this.documentsNodeRef,
                            siteId: null,
                            containerId: null,
                            uploadDirectory: null,
                            updateNodeRef: null,
                            description: "",
                            overwrite: false,
                            thumbnails: "doclib",
                            username: null
                        }
                    },
                    widgets: [
                        {
                            name: "alfresco/forms/controls/FileSelect",
                            config: {
                                label: "Select files to upload...",
                                name: "files"
                            }
                        }
                    ]
                });
                this.uploadDialog.show();
            },

            /**
             * This function will be called whenever the [AlfFormDialog]{@link module:alfresco/forms/AlfFormDialog} created
             * by the [showUploader function]{@link module:alfresco/services/ContentService#showUploader} is confirmed to
             * trigger a dialog. This will destroy the dialog and pass the supplied payload onto the [AlfUpload]{@link module:alfresco/upload/AlfUpload}
             * module to actually perform the upload. It is necessary to destroy the dialog to ensure that all the subscriptions
             * are removed to prevent subsequent upload requests from processing old data.
             *
             * @instance
             * @param {object} payload The file upload data payload to pass on
             */
            _onFileUploadRequest: function alfresco_services_ContentService__onFileUploadRequest(payload) {
                if (this.uploadDialog != null) {
                    this.uploadDialog.destroyRecursive();
                }
                var responseTopic = this.generateUuid();
                this._uploadSubHandle = this.alfSubscribe(responseTopic, lang.hitch(this, "_onFileUploadComplete"), true);
                payload.alfResponseTopic = responseTopic;
                this.alfPublish("ALF_UPLOAD_REQUEST", payload);
            },

            /**
             * This function is called once the document upload is complete. It publishes a request to reload the
             * current document list data.
             *
             * @instance
             */
            _onFileUploadComplete: function alfresco_services_ContentService__onFileUploadComplete() {
                this.alfLog("log", "Upload complete");
                this.alfUnsubscribe(this._uploadSubHandle);
                this.alfPublish(this.ReloadDocumentsTopic, {});
            },

            _retrieveDocumentdetails: function openESDH_CaseDocumentService__retrieveCaseDocumentDetails(payload){
                var rowData = payload.row;
                console.log("CaseDocumentsService(142) NodeRef:"+rowData.id);
                this.alfPublish(this.CaseDocumentReloadAttachmentsTopic, {nodeRef:rowData.id});
                this.alfPublish(this.GetDocumentVersionsTopicClick, {nodeRef:rowData.data.mainDocNodeRef});
            },

            _retrieveDocumentVersions: function openESDH_CaseDocumentService__retrieveCaseDocumentDetails(payload){
                var nodeRef = payload.nodeRef;
                console.log("CaseDocumentsService(150) NodeRef: "+payload.nodeRef);
                this.alfPublish(this.GetDocumentVersionsTopic, {nodeRef:nodeRef});
            }
        });
    });