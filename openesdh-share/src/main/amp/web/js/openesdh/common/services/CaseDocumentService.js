define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "alfresco/core/NodeUtils",
        "openesdh/pages/_TopicsMixin",
        "alfresco/dialogs/AlfFormDialog",
        "dojo/window"
    ],
    function (declare, AlfCore, CoreXhr, array, lang, NodeUtils, _TopicsMixin, AlfFormDialog, win) {

        return declare([AlfCore, CoreXhr, _TopicsMixin], {

            i18nRequirements: [
                {i18nFile: "./i18n/CaseDocumentService.properties"}
            ],

            /**
             * An object which contains the document constraints as received from the repository.
             * In the form
             * {variantConstraint: ["string value_1". "string value_n"], ...}
             */
            documentConstraints : null,

            /**
             * The documents NodeRef for a case.
             */
            documentsNodeRef: null,

            /**
             * The current nodeRef of the selected document record in a case.
             */
            currentDocRecordNodeRef: null,

            constructor: function (args) {
                lang.mixin(this, args);

                this.alfSubscribe("OE_SHOW_UPLOADER", lang.hitch(this, this._showUploader));
                this.alfSubscribe("OE_SHOW_ATTACHMENTS_UPLOADER", lang.hitch(this, this._showUploader));
                this.alfSubscribe("OE_CASE_DOCUMENT_SERVICE_UPLOAD_REQUEST_RECEIVED", lang.hitch(this, this._onFileUploadRequest));
                this.alfSubscribe("OE_PREVIEW_DOC", lang.hitch(this, this.onPreviewDoc));
                this.alfSubscribe("GET_DOCUMENT_RECORD_INFO", lang.hitch(this, this._docRecordInfo));
                this.alfSubscribe(this.CaseDocumentRowSelect, lang.hitch(this, this._retrieveDocumentdetails));
                this.alfSubscribe(this.DocumentVersionUploaderTopic, lang.hitch(this, this._showVersionUploader));
                this.alfSubscribe(this.DocumentVersionRevertTopic, lang.hitch(this, this._revertDocumentVersion));
                this.alfSubscribe(this.DocumentVersionRevertFormSubmitTopic, lang.hitch(this, this._onVersionRevertSubmit));
                this.alfSubscribe(this.GetDocumentVersionsTopicClick, lang.hitch(this, this._retrieveDocumentVersions));

                this._getDocumentConstraints();
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
                var finalWidget = [
                    {
                        name: "alfresco/forms/controls/FileSelect",
                        config: {
                            label: "Select files to upload...",
                            name: "files"
                        }
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            name: "targetData.destination",
                            value: (payload.alfTopic == "OE_SHOW_ATTACHMENTS_UPLOADER")? this.currentDocRecordNodeRef: this.documentsNodeRef,
                            postWhenHiddenOrDisabled: true
                        }
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            name: "targetData.description",
                            value: "",
                            postWhenHiddenOrDisabled: true
                        }
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            name: "targetData.overwrite",
                            value: false,
                            postWhenHiddenOrDisabled: true
                        }
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            name: "targetData.uploadDirectory",
                            value: null,
                            postWhenHiddenOrDisabled: true
                        }
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            name: "targetData.siteId",
                            value: null,
                            postWhenHiddenOrDisabled: true
                        }
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            name: "targetData.username",
                            value: null,
                            postWhenHiddenOrDisabled: true
                        }
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            name: "targetData.containerId",
                            value: null,
                            postWhenHiddenOrDisabled: true
                        }
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            name: "targetData.thumbnails",
                            value: "doclib",
                            postWhenHiddenOrDisabled: true
                        }
                    }
                ];
                var docRecordWidgets = [
                    {
                        name: "openesdh/common/widgets/controls/Select",
                            config: {
                                id: "doc_category",
                                label: this.message("upload-dialog.control.label.category"),
                                optionsConfig: { fixed: this._createConstraintSelection("category") },
                                name: "targetData.doc_category",
                                fieldId: "ebf9b987-9744-47bc-8823-klar7aa783f4"
                            }
                    },
                    {
                        name: "openesdh/common/widgets/controls/Select",
                            config: {
                                id: "doc_type",
                                label: this.message("upload-dialog.control.label.type"),
                                optionsConfig: { fixed: this._createConstraintSelection("type") },
                                name: "targetData.doc_type",
                                fieldId: "ebf9b987-6598-47ad-8823-ee9d2ee783f4"
                            }
                    },
                    {
                        name: "openesdh/common/widgets/controls/Select",
                            config: {
                                id: "doc_state",
                                label: this.message("upload-dialog.control.label.state"),
                                optionsConfig: { fixed: this._createConstraintSelection("state") },
                                name: "targetData.doc_state",
                                fieldId: "ebf9b987-9744-32ll-8823-ee9d9afkt7f4"
                            }
                    }
                ];

                if(payload.alfTopic != "OE_SHOW_ATTACHMENTS_UPLOADER")
                    finalWidget = finalWidget.concat(docRecordWidgets);

                this.uploadDialog = new AlfFormDialog({
                    dialogTitle: "Select files to upload",
                    dialogConfirmationButtonTitle: "Upload",
                    dialogCancellationButtonTitle: "Cancel",
                    formSubmissionTopic: "OE_CASE_DOCUMENT_SERVICE_UPLOAD_REQUEST_RECEIVED",
                    widgets: finalWidget
                });

                this.uploadDialog.show();
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
            _revertDocumentVersion: function alfresco_services_ContentService__showUploader(payload) {
                var targetData = {
                    destination: null, siteId: null, containerId: null,
                    uploadDirectory: null, updateNodeRef: payload.documentNodeRef,
                    overwrite: payload.versioning, thumbnails: null, username: null
                };

                var fileUploadControl = {
                    name: "alfresco/forms/controls/FileSelect",
                    config: {
                        label: this.message("upload-dialog.form-control.label.file-control"),
                        name: "files"
                    }
                }; //conditionally insert this into the widget array
                var widgetArray = [
                    {
                    name: "alfresco/forms/controls/DojoRadioButtons",
                    config: {
                        label: this.message("upload-dialog.form-control.label.version-options"),
                        name: "majorVersion",
                        value: "false",
                        optionsConfig: {
                            fixed: [
                                { label: this.message("upload-dialog.form-control.label.version-options.minor"), value: "false" },
                                { label: this.message("upload-dialog.form-control.label.version-options.major"), value: "true" }
                            ]
                        }
                    }
                },
                    {
                        name: "alfresco/forms/controls/DojoTextarea",
                        config: {
                            label: this.message("upload-dialog.form-control.label.description"),
                            name: "description"
                        }
                    }
                ];

                if(payload.revert) {
                    targetData = {nodeRef: payload.documentNodeRef, version: payload.version};
                }
                else{
                    widgetArray.splice(0,0, fileUploadControl);
                }

                this.uploadDialog = new AlfFormDialog({
                    dialogTitle: (payload.revert) ? this.message("revert-dialog.label.title") : this.message("upload-dialog.label.title"),
                    dialogConfirmationButtonTitle: (payload.revert) ? this.message("revert-dialog.button.label.revert") : this.message("upload-dialog.button.label.upload=Upload"),
                    dialogCancellationButtonTitle: this.message("dialog.button.label.cancel"),
                    formSubmissionTopic: (payload.revert) ? this.DocumentVersionRevertFormSubmitTopic : "OE_CASE_DOCUMENT_SERVICE_UPLOAD_REQUEST_RECEIVED",
                    formSubmissionPayload: targetData,
                    widgets: widgetArray
                });

                this.uploadDialog.show();
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
            _showVersionUploader: function alfresco_services_ContentService__showUploader(payload) {
                this.uploadDialog = new AlfFormDialog({
                    dialogTitle: "upload-dialog.label.title",
                    dialogConfirmationButtonTitle: "Upload",
                    dialogCancellationButtonTitle: "Cancel",
                    formSubmissionTopic: "OE_CASE_DOCUMENT_SERVICE_UPLOAD_REQUEST_RECEIVED",
                    formSubmissionPayload: {},
                    widgets: [
                        {
                            name: "alfresco/forms/controls/FileSelect",
                            config: {
                                label: this.message("upload-dialog.form-control.label.file-control"),
                                name: "files"
                            }
                        },
                        {
                            name: "alfresco/forms/controls/DojoTextarea",
                            config: {
                                label: this.message("upload-dialog.form-control.label.description"),
                                name: "description"
                            }
                        },
                        {
                            name: "alfresco/forms/controls/DojoRadioButtons",
                            config: {
                                label: this.message("upload-dialog.form-control.label.version-options"),
                                name: "targetData.majorVersion",
                                value: "false",
                                optionsConfig: {
                                    fixed: [
                                        { label: this.message("upload-dialog.form-control.label.version-options.minor"), value: "false" },
                                        { label: this.message("upload-dialog.form-control.label.version-options.major"), value: "true" }
                                    ]
                                }
                            }
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                name: "targetData.destination",
                                value: null,
                                postWhenHiddenOrDisabled: true
                            }
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                name: "targetData.siteId",
                                value: null,
                                postWhenHiddenOrDisabled: true
                            }
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                name: "targetData.containerId",
                                value: null,
                                postWhenHiddenOrDisabled: true
                            }
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                name: "targetData.uploadDirectory",
                                value: null,
                                postWhenHiddenOrDisabled: true
                            }
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                name: "targetData.thumbnails",
                                value: null,
                                postWhenHiddenOrDisabled: true
                            }
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                name: "targetData.username",
                                value: null,
                                postWhenHiddenOrDisabled: true
                            }
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                name: "targetData.updateNodeRef",
                                value: payload.documentNodeRef,
                                postWhenHiddenOrDisabled: true
                            }
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                name: "targetData.overwrite",
                                value: payload.versioning,
                                postWhenHiddenOrDisabled: true
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
             *
             *
             * @instance
             * @param {object} payload The file data payload to pass on
             */
            _onVersionRevertSubmit: function alfresco_services_ContentService__onFileUploadRequest(payload) {
                var url = Alfresco.constants.PROXY_URI + "api/revert";

                this.serviceXhr({
                    url: url,
                    method: "POST",
                    data: payload,
                    successCallback:function (response, config) {
                        this.alfPublish(this.GetDocumentVersionsTopic, response);
                    },
                    callbackScope: this});
            },

            _docRecordInfo: function (nodeRef) {
                // Get caseInfo from webscript
                if (nodeRef == null) {
                    console.log("openesdh/common/services/CaseDocumentService(452) Error", "Null nodeRef");
                    return false;
                }
                var docRecordNodeRefURI = NodeUtils.processNodeRef(nodeRef).uri;
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/documentInfo/" + docRecordNodeRefURI;

                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback:function (response) {
                        this.alfPublish(this.CaseRefreshDocInfoTopic, response)
                    },
                    callbackScope: this});
            },

            _getDocumentConstraints: function () {
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/case/document/constraints";
                var _this = this;

                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback:function (response) {
                        _this.documentConstraints = response;
                    },
                    callbackScope: this});
            },


            _createConstraintSelection: function dk_openesdh__createConstraintSelection(constraintType) {
                var options = [];
                var constraintTarget = constraintType+"Constraint";
                var constraintValues = this.documentConstraints[constraintTarget];
                for (var state in constraintValues) {
                    options.push({
                        value: constraintValues[state].value,
                        label: constraintValues[state].label
                    });
                }
                return options;
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
                this.alfPublish(this.CaseReloadDocumentsTopic, {});
                this.alfPublish(this.CaseDocumentReloadAttachmentsTopic, {nodeRef:this.currentDocRecordNodeRef});
            },

            _retrieveDocumentdetails: function openESDH_CaseDocumentService__retrieveCaseDocumentDetails(payload){
                var rowData = payload.row;
                this.currentDocRecordNodeRef = rowData.id;
                this.alfPublish("GET_DOCUMENT_RECORD_INFO", rowData.id);
                this.alfPublish(this.CaseDocumentReloadAttachmentsTopic, {nodeRef:rowData.id});
                this.alfPublish(this.GetDocumentVersionsTopicClick, {nodeRef:rowData.data.mainDocNodeRef});
            },

            _retrieveDocumentVersions: function openESDH_CaseDocumentService__retrieveCaseDocumentDetails(payload){
                var nodeRef = payload.nodeRef;
                this.alfPublish(this.GetDocumentVersionsTopic, {nodeRef:nodeRef});
            }
        });
    });