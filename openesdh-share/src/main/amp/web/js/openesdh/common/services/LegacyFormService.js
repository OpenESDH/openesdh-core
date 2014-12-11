define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin"],
    function (declare, AlfCore, CoreXhr, array, lang, _CaseMembersServiceTopicsMixin) {

        return declare([AlfCore, CoreXhr, _CaseMembersServiceTopicsMixin], {

            nonAmdDependencies: [
                "/js/yui-common.js",
                "/js/alfresco.js",
                "/modules/simple-dialog.js",
                "/components/form/form.js",
                "/components/object-finder/object-finder.js"
            ],

            cssRequirements: [
                {cssFile: "/components/object-finder/object-finder.css"}
            ],

            constructor: function (args) {
                lang.mixin(this, args);
                this.alfSubscribe("LEGACY_CREATE_FORM_DIALOG", lang.hitch(this, "onCreateFormDialog"));
                this.alfSubscribe("LEGACY_EDIT_FORM_DIALOG", lang.hitch(this, "onEditFormDialog"));
            },

            onCreateFormDialog: function (payload) {
                var destination = payload.nodeRef,
                    itemType = payload.itemType,
                    dialogWidth = "dialogWidth" in payload ? payload.dialogWidth : "33em",
                    dialogTitle = payload.dialogTitle;
                var successResponseTopic = lang.getObject("successResponseTopic", "LEGACY_CREATE_FORM_SUCCESS", payload);
                var failureResponseTopic = lang.getObject("failureResponseTopic", "LEGACY_CREATE_FORM_FAILURE", payload);

                // Intercept before dialog show
                var doBeforeDialogShow = function (p_form, p_dialog)
                {
                    Alfresco.util.populateHTML(
                        [ p_dialog.id + "-form-container_h", dialogTitle ]
                    );
                };

                var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true",
                    {
                        itemKind: "type",
                        itemId: itemType,
                        destination: destination,
                        mode: "create",
                        submitType: "json"
                    });

                // Using Forms Service, so always create new instance
                var createRow = new Alfresco.module.SimpleDialog(Alfresco.util.generateDomId() + "-create");

                createRow.setOptions(
                    {
                        width: dialogWidth,
                        templateUrl: templateUrl,
                        actionUrl: null,
                        destroyOnHide: true,
                        doBeforeDialogShow:
                        {
                            fn: doBeforeDialogShow,
                            scope: this
                        },
                        onSuccess:
                        {
                            fn: function (response)
                            {
                                this.alfPublish(successResponseTopic, {
                                    nodeRef: response.json.persistedObject
                                });
                            },
                            scope: this
                        },
                        onFailure:
                        {
                            fn: function (response)
                            {
                                this.alfPublish(failureResponseTopic, {
                                    "response": response
                                });
                            },
                            scope: this
                        }
                    }).show();
            },

            onEditFormDialog: function (payload) {
                var dialogTitle = this.message(payload.dialogTitle);
                var dialogWidth = "dialogWidth" in payload ? payload.dialogWidth : "33em";
                var nodeRef = payload.nodeRef;
                var successResponseTopic = lang.getObject("successResponseTopic", "LEGACY_EDIT_FORM_SUCCESS", payload);
                var failureResponseTopic = lang.getObject("failureResponseTopic", "LEGACY_CREATE_FORM_FAILURE", payload);

                // Intercept before dialog show
                var doBeforeDialogShow = function (p_form, p_dialog) {
                    Alfresco.util.populateHTML(
                        [ p_dialog.id + "-form-container_h", dialogTitle ]
                    );
                };

                var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true",
                    {
                        itemKind: "node",
                        itemId: nodeRef,
                        mode: "edit",
                        submitType: "json"
                    });

                // Using Forms Service, so always create new instance
                var editDetails = new Alfresco.module.SimpleDialog(Alfresco.util.generateDomId() + "-editDetails");
                editDetails.setOptions(
                    {
                        width: dialogWidth,
                        templateUrl: templateUrl,
                        actionUrl: null,
                        destroyOnHide: true,
                        doBeforeDialogShow: {
                            fn: doBeforeDialogShow,
                            scope: this
                        },
                        onSuccess: {
                            fn: function (response) {
                                this.alfPublish(successResponseTopic, {"response": response});
                            },
                            scope: this
                        },
                        onFailure: {
                            fn: function (response) {
                                this.alfPublish(failureResponseTopic, {"response": response});
                            },
                            scope: this
                        }
                    }).show();
            }
        });
    });