define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/_base/array",
        "alfresco/core/NodeUtils",
        "alfresco/dialogs/AlfFormDialog",
        "alfresco/services/ActionService",
        "openesdh/extensions/core/ObjectProcessingMixin"],
    function(declare, lang, array, NodeUtils, AlfFormDialog, ActionService, ObjectProcessingMixin) {
        return declare([ActionService, ObjectProcessingMixin], {

            i18nRequirements: [{i18nFile: "./i18n/ContactsActionService.properties"}],

            deleteMultipleTopic: "CONTACTS_DELETE_MULTIPLE",

            contactNoderef: null,

            contactData: null,

            editFormWidgets: null,

            constructor: function (args) {
                this.alfSubscribe(this.deleteMultipleTopic, lang.hitch(this, this.onDeleteMultipleContacts));
                this.alfSubscribe("EDIT_CONTACT", lang.hitch(this, this._updateContact));
                this.alfSubscribe("EDIT_CONTACT_FORM_DIALOG", lang.hitch(this, this.onEditDialogRequest));
                this.alfSubscribe("SHOW_EDIT_CONTACT_DIALOG", lang.hitch(this, this.onShowEditDialog));
            },

            /**
             * Override the implementation since we don't have permission
             * information about the contacts.
             * @param payload
             */
            onSelectedFilesChanged: function (payload) {
                var files = this.getSelectedDocumentArray();

                // Publish the information about the actions so that menu items can be filtered...
                this.alfPublish(this.selectedDocumentsChangeTopic, {
                    selectedFiles: files
                    //userAccess: userAccess,
                    //commonAspects: commonAspects,
                    //allAspects: allAspects
                });
            },

            onDeleteMultipleContacts: function (payload) {
                var items = this.getSelectedDocumentArray();
                var nodeRefs = items.map(function (item) {
                    return item.nodeRef;
                });

                var contactTypeId = payload.contactType.replace(":", "_");

                var confirmationTitle = this.message("contacts.tool.delete-multiple." + contactTypeId + ".confirmation.title", [items.length]),
                    confirmationPrompt = this.message("contacts.tool.delete-multiple." + contactTypeId + ".confirmation.message", [items.length]),
                    successMessage = this.message("contacts.tool.delete-multiple." + contactTypeId + ".success", [items.length]),
                    requiresConfirmation = true;

                if (items.length > 0) {
                    this.alfPublish("ALF_CRUD_DELETE", {
                        // TODO: We are abusing this URL a bit.
                        url: "slingshot/datalists/action/items?alf_method=delete",
                        method: "POST",

                        nodeRefs: nodeRefs,

                        requiresConfirmation: requiresConfirmation,
                        confirmationTitle: confirmationTitle,
                        confirmationPrompt: confirmationPrompt,
                        successMessage: successMessage
                    });
                }
            },

            onEditDialogRequest: function (payload){
                var _this = this;
                this.contactNoderef = payload.nodeRef;
                this.editFormWidgets = payload.widgetsForEdit;
                var contactNodeRefURI = NodeUtils.processNodeRef(payload.nodeRef).uri;
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/contact/"+contactNodeRefURI;

                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback:function (response) {
                        _this.contactData = response;
                        this.alfPublish("SHOW_EDIT_CONTACT_DIALOG", payload)
                    },
                    failureCallback: function(response){
                        _this.contactData = null;
                        alert("Error: verifying contact information. Please contact systems' administrator");
                    },
                    callbackScope: this});
            },

            onShowEditDialog: function (payload){
                var publishOnSuccessTopic = (payload.successResponseTopic);
                if(this.editContactDialog){
                    this.editContactDialog.destroy();
                }
                console.log("\n\n ==>ContactsActionService payload received.\n\n");
                this.editContactDialog = new AlfFormDialog({
                    id:"EDIT_CONTACT_DIALOG",
                    dialogTitle: "dialog.title.edit",
                    dialogConfirmationButtonTitle: this.message("dialog.button.label.update"),
                    dialogCancellationButtonTitle: this.message("dialog.button.label.cancel"),
                    formSubmissionTopic: "EDIT_CONTACT",
                    formSubmissionPayload: {
                        publishOnSuccessTopic: publishOnSuccessTopic
                    },
                    fixedWidth: true,
                    widgets: this._fillFields(payload)
                });
                this.editContactDialog.show();
            },

            _updateContact: function (payload){
                var contactNodeRefURI = NodeUtils.processNodeRef(this.contactNoderef).uri;
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/contact/"+contactNodeRefURI;
                var _this = this;

                this.serviceXhr({
                    url: url,
                    method: "PUT",
                    data: payload,
                    successCallback:function (response) {
                        console.log("===> Contact updated <===\n");
                        _this.contactData = null;
                        this.alfPublish(payload.publishOnSuccessTopic ,response);
                    },
                    failureCallback: function(response){
                        _this.contactData = null;
                        alert("Error: unable to update contact information. Please contact systems' administrator");
                    },
                    callbackScope: this});
            },

            _fillFields: function(payload){
                var formWidgets = lang.clone(this.editFormWidgets);
                this.processObject([ "processInstanceTokens"], formWidgets );
                return formWidgets;
            }

        });
    });