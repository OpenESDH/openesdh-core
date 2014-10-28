define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin",
        "alfresco/dialogs/AlfDialog",
        "openesdh/common/widgets/controls/category/CategoryPickerControl"],
    function (declare, AlfCore, CoreXhr, array, lang, _TopicsMixin, AlfDialog, CategoryPickerControl) {

        return declare([AlfCore, CoreXhr, _TopicsMixin], {

            destinationNodeRef: null,

            caseNodeRef: "",

            constructor: function (args) {
                lang.mixin(this, args);
                this.caseNodeRef = args.caseNodeRef;
                this._caseInfo(this.caseNodeRef);

                this.alfSubscribe("JOURNALIZE", lang.hitch(this, "_onJournalize"));
            },

            _caseInfo: function (nodeRef) {
                // Get caseInfo from webscript
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/caseinfo?nodeRef=" + nodeRef;
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: this._onCaseInfoSuccessCallback,
                    callbackScope: this});
            },

            _onCaseInfoSuccessCallback: function (response, config) {

                var domReadyFunction = (function (scope) {
                    return function () {
                        scope.alfPublish(scope.CaseInfoTopic, response);
                        scope.alfPublish("ALF_UPDATE_PAGE_TITLE", {title: response["cm:title"].value});
                    }
                })(this);

                require(["dojo/ready"], function (ready) {
                    // will not be called until DOM is ready
                    ready(domReadyFunction);
                });
            },

            _onJournalizeSuccessCallback: function (response, config) {

                Alfresco.util.PopupManager.displayMessage(
                    {
                        text: this.message('journalize.success')
                    });

                window.location.reload();
            },

            _onJournalize: function () {
                var dialog = new AlfDialog({
                    pubSubScope: this.pubSubScope,
                    title: this.message("journalize.dialog.title"),
                    widgetsContent: [
                        {
                            name: "openesdh/common/widgets/controls/category/CategoryPickerControl",
                            config: {
                                // TODO: Set the root category for the journal key
//                                rootNodeRef: "workspace://SpacesStore/abc/"
                            }
                        }
                    ],
                    widgetsButtons: [
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: this.message("button.ok"),
                                publishTopic: "JOURNALIZE_DIALOG_OK",
                                publishPayload: {}
                            }
                        },

                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: this.message("button.cancel"),
                                publishTopic: "JOURNALIZE_DIALOG_CANCEL",
                                publishPayload: {}
                            }
                        }
                    ]
                });
                dialog.show();

                this.alfSubscribe("JOURNALIZE_DIALOG_OK", lang.hitch(this, "_onJournalizeDialogOK"));
            },

            _onJournalizeDialogOK: function (payload) {
                var selectedItems = payload.dialogContent[0].selectedItems;
                if (!selectedItems) {
                    // TODO: Better form validation.. Make it a "required" field?
                    return;
                }
                var journalKey;
                for (var nodeRef in selectedItems) {
                    journalKey = nodeRef;
                    break;
                }

                var url = Alfresco.constants.PROXY_URI + "api/openesdh/journalize?nodeRef=" + this.caseNodeRef + "&journalKey=" + journalKey;
                this.serviceXhr({
                    url: url,
                    method: "PUT",
                    successCallback: this._onJournalizeSuccessCallback,
                    callbackScope: this
                });
            }
        });
    });