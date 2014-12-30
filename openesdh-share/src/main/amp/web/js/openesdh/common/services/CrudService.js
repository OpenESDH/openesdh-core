/**
 * Extend the Alfresco-defined CrudService to fix some bugs.
 */
define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "service/constants/Default",
        "alfresco/dialogs/AlfDialog",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/json",
        "alfresco/services/CrudService"],
    function (declare, AlfCore, CoreXhr, AlfConstants, AlfDialog, lang, array, dojoJson, CrudService) {
        return declare([CrudService], {
            /**
             * Makes a GET request to the Repository using the 'url' attribute provided in the payload passed
             * in the publication on the topic that this function subscribes to. The 'url' is expected to be a
             * Repository WebScript URL and should not include the Repository proxy stem.
             *
             * @instance
             * @param {object} payload
             */
            onGetAll: function alfresco_services_CrudService__onGetAll(payload) {
                var url = this.getUrlFromPayload(payload);
                if (url !== null) {
                    this.serviceXhr({url: url,
                        // BUGFIX: Pass the payload as the query, not the data!
                        query: this.clonePayload(payload),
                        alfTopic: (payload.alfResponseTopic ? payload.alfResponseTopic : null),
                        method: "GET"});
                }
            },

            /**
             * onDelete/requestDeleteConfirmation/performDelete/etc has been modified to pass the payload on as data to
             * the XHR request.
             */

            /**
             * Handles delete requests. If the supplied payload contains an attribute "requiresConfirmation"
             * that is set to true, then a dialog will be displayed prompting the user to confirm the delete
             * action. The payload can optionally contain localized messages for the dialog title, prompt
             * and button labels.
             *
             * @instance
             * @param {object} payload
             */
            onDelete: function alfresco_services_CrudService__onDelete(payload) {
                // TODO: Need to determine whether or not the ID should be provided in the payload or
                //       as part of the URL.
                var url = this.getUrlFromPayload(payload);
                var data = this.clonePayload(payload);
                delete data.requiresConfirmation;
                delete data.method;
                // Allow the caller to specify what method to use to request
                var method = payload.method != null ? payload.method : "DELETE";
                if (url !== null)
                {
                    if (payload.requiresConfirmation === true)
                    {
                        this.requestDeleteConfirmation(url, method, data, payload);
                    }
                    else
                    {
                        this.performDelete(url, method, data, payload.responseTopic, payload.successMessage);
                    }
                }
            },

            /**
             * Called from [onDelete]{@link module:alfresco/services/CrudService#onDelete} when user confirmation
             * for the delete action is required. Displays a dialog prompting the user to confirm the action.
             * The dialog title, prompt and button labels can all be configured via the attributes on the
             * supplied payload.
             *
             * @instance
             * @param {string} url The URL to use to perform the delete
             * @param {object} payload The original request payload.
             */
            requestDeleteConfirmation: function alfresco_services_CrudService__requestDeleteConfirmation(url, method, data, payload) {

                var responseTopic = this.generateUuid();
                this._deleteHandle = this.alfSubscribe(responseTopic, lang.hitch(this, this.onDeleteConfirmation), true);

                var title = (payload.confirmationTitle) ? payload.confirmationTitle : this.message("crudservice.generic.delete.title");
                var prompt = (payload.confirmationPrompt) ? payload.confirmationPrompt : this.message("crudservice.generic.delete.prompt");
                var confirmButtonLabel = (payload.confirmationButtonLabel) ? payload.confirmationButtonLabel : this.message("crudservice.generic.delete.confirmationButtonLabel");
                var cancelButtonLabel = (payload.cancellationButtonLabel) ? payload.cancellationButtonLabel : this.message("crudservice.generic.delete.cancellationButtonLabel");

                var dialog = new AlfDialog({
                    generatePubSubScope: false,
                    title: title,
                    textContent: prompt,
                    widgetsButtons: [
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: confirmButtonLabel,
                                publishTopic: responseTopic,
                                publishPayload: {
                                    url: url,
                                    method: method,
                                    data: data,
                                    responseTopic: payload.responseTopic,
                                    successMessage: payload.successMessage
                                }
                            }
                        },
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: cancelButtonLabel,
                                publishTopic: "close"
                            }
                        }
                    ]
                });
                dialog.show();
            },

            /**
             * Handles the actual deletion, this is abstracted to a separate function so that it can be called from
             * both [onDelete]{@link module:alfresco/services/CrudService#onDelete} and
             * [onDeleteConfirmation]{@link module:alfresco/services/CrudService#onDeleteConfirmation} depending upon
             * whether or not the user needs to confirm the delete action.
             *
             * @instance
             * @param {string} url The URL to use to perform the delete
             * @param {string} responseTopic The topic to publish on completion of deletion.
             * @param {string} successMessage The message to display on successful deletion
             */
            performDelete: function alfresco_services_CrudService__performDelete(url, method, data, responseTopic, successMessage) {
                this.serviceXhr({url: url,
                    method: method,
                    data: data,
                    alfTopic: responseTopic,
                    successMessage: successMessage,
                    successCallback: this.refreshRequest,
                    callbackScope: this});
            },

            /**
             * This function is called when the user confirms that they wish to peform the delete action.
             *
             * @instance
             * @param {object} payload An object containing the the deletion details.
             */
            onDeleteConfirmation: function alfresco_services_CrudService__onDeleteConfirmation(payload) {
                this.alfUnsubscribeSaveHandles([this._deleteHandle]);
                this.performDelete(payload.url, payload.method, payload.data, payload.responseTopic, payload.successMessage);
            }
        });
    });