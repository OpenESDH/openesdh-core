define(["dojo/_base/declare",
        "alfresco/lists/AlfSortablePaginatedList",
        "dojo/_base/lang"],
    function (declare, AlfSortablePaginatedList, lang) {
        return declare([AlfSortablePaginatedList], {

            i18nRequirements: [
                {i18nFile: "./i18n/AuthorityList.properties"}
            ],

            /**
             * What authority type should be searched?
             * Choices are "authority:person" or "authority:group".
             *
             * @instance
             * @type string
             * @default ""
             */
            authorityType : "authority:person",

            /**
             * See alfresco/lists/AlfList#loadDataPublishTopic:
             */
            loadDataPublishTopic: "ALF_GET_AUTHORITY",

            searchTerm: "",

            sortField: "",

            postMixInProperties: function () {
                this.inherited(arguments);
                this.alfSubscribe("AUTHORITY_LIST_SEARCH", lang.hitch(this, this.loadAuthorityList));
                this.alfSubscribe("AUTHORITY_LIST_RELOAD", lang.hitch(this, this.loadData));
                //this.alfSubscribe("ALF_AUTHORITY_LIST_SUCCESS", lang.hitch(this, this.showAll));
            },

            loadAuthorityList: function (payload) {
                this.searchTerm = payload.searchTerm;
                this.loadData();
            },

            setDisplayMessages: function () {
                this.inherited(arguments);

                // Override the default messages
                this.noDataMessage = this.message("authorityList.no.data.message");
                // TODO: override as needed:
//                this.fetchingDataMessage = this.message("alflist.loading.data.message");
//                this.renderingViewMessage = this.message("alflist.rendering.data.message");
//                this.fetchingMoreDataMessage = this.message("alflist.loading.data.message");
//                this.dataFailureMessage = this.message("alflist.data.failure.message");
            },

            updateLoadDataPayload: function (payload) {
                this.inherited(arguments);
                if (this.searchTerm != "") {
                    this.searchTerm = payload.searchTerm ;
                }
            }
        });
    });