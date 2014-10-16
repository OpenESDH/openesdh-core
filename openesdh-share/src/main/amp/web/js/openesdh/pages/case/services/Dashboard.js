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
                this.alfSubscribe("JOURNALIZE_CLICK_TEST", lang.hitch(this, "_onClickTest"));


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
                console.log("hej");
                console.log( payload.dialogContent[1].getValue());

//                var saveAsDefault = payload.dialogContent[0].getValue();
//                var name;
//                if (saveAsDefault) {
//                    name = this.DEFAULT_SEARCH_NAME;
//                } else {
//                    name = payload.dialogContent[1].getValue();
//                }
//                if (name == '') {
//                    // Don't allow blank names
//                    Alfresco.util.PopupManager.displayMessage(
//                        {
//                            text: this.message('message.save_search.failure')
//                        });
//                } else {
//                    var result = this.saveSearch(this.currentSearch, name);
//                    // TODO: Ask to overwrite if name already exists.
//                }
            },

            _onClickTest: function (payload) {
                Alfresco.util.PopupManager.displayMessage(
                        {
                            text: this.message('hej')
                        });
            },

            _onClickJournalize: function (payload) {
                var _this = this;



                this.widgets = [];




//                var picker = new FilterCategoryWidget();
//                picker.startup();
//
//
//                console.log(picker);




                var dialog = new AlfDialog({
                    pubSubScope: this.pubSubScope,
                    title: this.message("xsearch.save_search_as.dialog.title"),
                    widgetsContent: [


                        {
                            name: "openesdh/common/widgets/controls/FilterCategoryWidget",
                            config: {
                                label: this.message("asdasd"),
                                publishTopic: "JOURNALIZE_CLICK_TEST",
                                publishPayload: {}


                            }
                        }


                    ],
                    widgetsButtons: [
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: this.message("journalize.button.ok"),
                                publishTopic: "JOURNALIZE_CLICK_OK",
                                publishPayload: {}
                            }
                        },
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: this.message("journalize.button.cancel"),
                                publishTopic: "XSEARCH_SAVE_SEARCH_AS_CANCEL",
                                publishPayload: {}
                            }
                        }
                    ]

                });
                dialog.show();


            }
        });
    });