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
            }
        });
    });