define(["dojo/_base/declare",
        "alfresco/services/ActionService",
        "dojo/_base/lang",
        "dojo/_base/array"],
    function(declare, ActionService, lang, array) {
        return declare([ActionService], {

            i18nRequirements: [{i18nFile: "./i18n/ContactsActionService.properties"}],

            deleteMultipleTopic: "CONTACTS_DELETE_MULTIPLE",

            constructor: function (args) {
                this.alfSubscribe(this.deleteMultipleTopic, lang.hitch(this, this.onDeleteMultipleContacts));
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

                var confirmationTitle = this.message("parties.tool.delete-multiple." + contactTypeId + ".confirmation.title", [items.length]),
                    confirmationPrompt = this.message("parties.tool.delete-multiple." + contactTypeId + ".confirmation.message", [items.length]),
                    successMessage = this.message("parties.tool.delete-multiple." + contactTypeId + ".success", [items.length]),
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
            }
        });
    });