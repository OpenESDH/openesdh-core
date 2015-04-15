define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "alfresco/core/NodeUtils",
        "dojo/_base/array",
        "dojo/_base/lang",
        "service/constants/Default",
        "openesdh/common/services/_AuthorityServiceTopicsMixin"],
    function (declare, AlfCore, CoreXhr, NodeUtils, array, lang, AlfConstants, _TopicsMixin) {

        return declare([AlfCore, CoreXhr, _TopicsMixin], {

            i18nRequirements: [
                {i18nFile: "./i18n/AuthorityService.properties"}
            ],

            /**
             * The base url for which to use to load authorities
             */
            baseURL: "api/forms/picker/authority/children?selectableType=",

            /**
             * The authority type to search for; set to persons by default.
             * Values are cm:person, cm:group
             */
            authorityType: "cm:person",

            /**
             * Limit the size of the search
             */
            resultSize: "1000",

            /**
             * The search term
             */
            searchTerm : "*",

            constructor: function (args) {
                lang.mixin(this, args);
                this.alfSubscribe(this.GetAuthorities, lang.hitch(this, "_onGetAuthorities"));
            },

            _onGetAuthorities: function (payload){
                var searchTerm = (payload.searchTerm != undefined) ? payload.searchTerm : "*";
                console.log("Authority Service (45) This is the payload: " + payload.searchTerm);

                var authType =  (payload.authorityType != undefined) ? payload.authorityType : this.authorityType;

                var url = AlfConstants.PROXY_URI + this.baseURL + authType+"&searchTerm="+searchTerm+"&size="+this.resultSize;
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    data: payload,
                    successCallback:function (response) {
                        this.alfPublish(payload.alfResponseTopic+"_SUCCESS", response)
                    },
                    callbackScope: this});
            }
        });
    });