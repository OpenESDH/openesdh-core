define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin",
        "alfresco/dialogs/AlfFormDialog",
        "alfresco/core/NotificationUtils",
        "openesdh/common/widgets/controls/category/CategoryPickerControl"],
    function (declare, AlfCore, CoreXhr, array, lang, _TopicsMixin, AlfFormDialog, NotificationUtils, CategoryPickerControl) {

        return declare([AlfCore, CoreXhr, _TopicsMixin, NotificationUtils], {

            i18nRequirements: [
                {i18nFile: "./i18n/Dashboard.properties"}
            ],

            destinationNodeRef: null,

            caseNodeRef: "",

            constructor: function (args) {
                lang.mixin(this, args);
                this.caseNodeRef = args.caseNodeRef;

                this.alfSubscribe(this.CaseInfoTopic, lang.hitch(this, "_onCaseInfo"));
                this._caseInfo(this.caseNodeRef);

                this.alfSubscribe("JOURNALIZE", lang.hitch(this, "_onJournalize"));
                this.alfSubscribe("UNJOURNALIZE", lang.hitch(this, "_onUnJournalize"));
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
                        scope.alfPublish("ALF_UPDATE_PAGE_TITLE", {title: response.properties["cm:title"].value});
                    }
                })(this);

                require(["dojo/ready"], function (ready) {
                    // will not be called until DOM is ready
                    ready(domReadyFunction);
                });
            },

            _onCaseInfo: function (payload) {
                // Update menu
                // TODO: Check if case is journalized or not.
//                this.alfPublish("CASE_JOURNALIZED", {isJournalized: "oe:journalized" in payload.aspects});
                var isJournalized = false;
            },

            _onJournalize: function () {
                var dialog = new AlfFormDialog({
                    pubSubScope: this.pubSubScope,
                    title: this.message("journalize.dialog.title"),
                    formSubmissionTopic: "JOURNALIZE_DIALOG_OK",
                    dialogConfirmationButtonTitle: this.message("button.ok"),
                    dialogCancellationButtonTitle: this.message("button.cancel"),
                    widgetsContent: [
                        {
                            name: "openesdh/common/widgets/controls/category/CategoryPickerControl",
                            config: {
                                label: this.message("journal.key"),
                                requirementConfig: {
                                    initialValue: true
                                }
                                // TODO: Set the root category for the journal key
//                                rootNodeRef: "workspace://SpacesStore/abc/"
                            }
                        }
                    ]
                });
                dialog.show();

                this.alfSubscribe("JOURNALIZE_DIALOG_OK", lang.hitch(this, "_onJournalizeDialogOK"));
            },

            _onJournalizeDialogOK: function (payload) {
                if (!confirm(this.message("journalize.confirm"))) {
                    return false;
                }

                var journalKey;
                for (var property in payload) {
                    if (!payload.hasOwnProperty(property)) continue;
                    if (property == "alfTopic") continue;
                    journalKey = property;
                }

                var url = Alfresco.constants.PROXY_URI + "api/openesdh/journalize?nodeRef=" + this.caseNodeRef + "&journalKey=" + journalKey;
                this.serviceXhr({
                    url: url,
                    method: "PUT",
                    data: {},
                    successCallback: lang.hitch(this, function () {
                        this.displayMessage(this.message('journalize.success'));
                        window.location.reload();
                    }),
                    failureCallback: lang.hitch(this, function () {
                        this.displayMessage(this.message('journalize.failure'));
                        window.location.reload();
                    }),
                    callbackScope: this
                });
            },

            _onUnJournalize: function (payload) {
                if (confirm(this.message("unjournalize.confirm"))) {
                    var url = Alfresco.constants.PROXY_URI + "api/openesdh/journalize?nodeRef=" + this.caseNodeRef + "&unjournalize=true";
                    this.serviceXhr({
                        url: url,
                        method: "PUT",
                        data: {},
                        successCallback: lang.hitch(this, function () {
                            this.displayMessage(this.message('unjournalize.success'));
                            window.location.reload();
                        }),
                        failureCallback: lang.hitch(this, function () {
                            this.displayMessage(this.message('unjournalize.failure'));
                            window.location.reload();
                        }),
                        callbackScope: this
                    });
                }
            }
        });
    });