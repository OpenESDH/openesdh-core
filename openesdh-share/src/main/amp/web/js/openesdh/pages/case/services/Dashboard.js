define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "alfresco/dialogs/AlfDialog",
        "openesdh/common/widgets/controls/FilterCategoryWidget",
        "alfresco/pickers/Picker",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin"],
    function (declare, AlfCore, CoreXhr, AlfDialog, FilterCategoryWidget, Picker, array, lang, _TopicsMixin ) {

        return declare([AlfCore, CoreXhr, _TopicsMixin],  {

            destinationNodeRef: null,

            nodeRef: "",

            constructor: function (args) {
                lang.mixin(this, args);
                nodeRef = args.NodeRef;
                this._caseInfo(args.nodeRef);
            },

            _caseInfo: function (nodeRef) {
                // Get caseInfo from webscript
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/caseinfo?nodeRef=" + nodeRef;
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: this._onSuccessCallback,
                    callbackScope: this});

                this.alfSubscribe("JOURNALIZE", lang.hitch(this, "_onClickJournalize"));
                this.alfSubscribe("JOURNALIZE_CLICK_OK", lang.hitch(this, "handleJournalizeClickOk"));



                this.alfSubscribe("XSEARCH_FILTER_WIDGET_ON_CHANGE", lang.hitch(this, "handleJournalizeClickOk"));
            },

            _onSuccessCallback: function (response, config) {

                var domReadyFunction = (function (scope) {
                    return function () {
                        scope.alfPublish(scope.CaseInfoTopic, response);
                        scope.alfPublish("ALF_UPDATE_PAGE_TITLE", {title: response["cm:title"].value});
                    }
                })(this);

                require(["dojo/ready"], function(ready) {
                    // will not be called until DOM is ready
                    ready(domReadyFunction);
                });
            },

            handleJournalizeClickOk: function (payload) {
//                console.log("hej");
//                console.log( payload.dialogContent[1].getValue());


            },

            _onJournalizeSuccessCallback: function (response, config) {

                Alfresco.util.PopupManager.displayMessage(
                        {
                            text: this.message('journalize.success')
                        });

                window.location.reload();
            },

            _onClickJournalize: function () {
                var _this = this;

                console.log(_this);

                this.widgets = [];

                var url = Alfresco.constants.PROXY_URI + "api/openesdh/journalize?nodeRef=" + this.nodeRef + "&journalKey=workspace://SpacesStore/8544ad16-e88f-4dce-979e-1eff675262ee";
                this.serviceXhr({
                    url: url,
                    method: "PUT",
                    successCallback: this._onJournalizeSuccessCallback,
                    callbackScope: this});


//                var picker = new FilterCategoryWidget();
//                picker.startup();
//
//                console.log(picker.options);
//                picker.connect();

//                console.log(picker);




//                var dialog = new AlfDialog({
//                    pubSubScope: this.pubSubScope,
//                    title: this.message("xsearch.save_search_as.dialog.title"),
//                    widgetsContent: [
//
//
//                        {
//                            name: "openesdh/common/widgets/controls/FilterCategoryWidget",
//                            config: {
//                                label: this.message("asdasd"),
//                                publishTopic: "JOURNALIZE_CLICK_TEST",
//                                publishPayload: {}
//
//
//                            }
//                        }
//
//
//                    ],
//                    widgetsButtons: [
//                        {
//                            name: "alfresco/buttons/AlfButton",
//                            config: {
//                                label: this.message("journalize.button.ok"),
//                                publishTopic: "JOURNALIZE_CLICK_OK",
//                                publishPayload: {}
//                            }
//                        },
//                        {
//                            name: "alfresco/buttons/AlfButton",
//                            config: {
//                                label: this.message("journalize.button.cancel"),
//                                publishTopic: "XSEARCH_SAVE_SEARCH_AS_CANCEL",
//                                publishPayload: {}
//                            }
//                        }
//                    ]
//
//                });
//                dialog.show();


            }
        });
    });