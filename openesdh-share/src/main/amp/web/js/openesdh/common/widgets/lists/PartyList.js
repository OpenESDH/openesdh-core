define(["dojo/_base/declare",
        "alfresco/lists/AlfSortablePaginatedList",
        "dojo/_base/lang"],
    function (declare, AlfSortablePaginatedList, lang) {
        return declare([AlfSortablePaginatedList], {

            i18nRequirements: [
                {i18nFile: "./i18n/PartyList.properties"}
            ],

            /**
             * What party type should be searched?
             * Good choices are "party:person" or "party:organization".
             *
             * @instance
             * @type string
             * @default ""
             */
            partyType: "",
            searchTerm: "",

            postMixInProperties: function () {
                this.inherited(arguments);
                this.alfSubscribe("PARTY_LIST_SEARCH", lang.hitch(this, this.loadPartyList));
                this.alfSubscribe("PARTY_LIST_RELOAD", lang.hitch(this, this.loadData));
                this.alfSubscribe("PARTY_LIST_SHOW_ALL", lang.hitch(this, this.showAll));
            },

            // Don't load data on page ready
            onPageWidgetsReady: function () {},

            postCreate: function () {
                this.inherited(arguments);
//                this.onViewSelected({
//                    value: "table"
//                });
            },

            loadPartyList: function (payload) {
                this.searchTerm = payload.term;
                this.loadData();
            },

            showAll: function () {
                this.searchTerm = '';
                this.loadData();
            },

            loadData: function () {
//                if (this.searchTerm != "") {
                    this.inherited(arguments);
//                }
            },

            processLoadedData: function () {
                this.inherited(arguments);
            },

            setDisplayMessages: function () {
                this.inherited(arguments);

                // Override the default messages
                this.noDataMessage = this.message("partylist.no.data.message");
                // TODO: override as needed:
//                this.fetchingDataMessage = this.message("alflist.loading.data.message");
//                this.renderingViewMessage = this.message("alflist.rendering.data.message");
//                this.fetchingMoreDataMessage = this.message("alflist.loading.data.message");
//                this.dataFailureMessage = this.message("alflist.data.failure.message");
            },

            updateLoadDataPayload: function (payload) {
//                if (this.searchTerm != "") {
                    payload.url = "api/openesdh/partysearch?baseType=" +
                        encodeURIComponent(this.partyType) + "&term=" +
                        encodeURIComponent(this.searchTerm);
                    // Not working:
//                    payload.baseType = this.partyType;
//                    payload.term = this.searchTerm;
//                }
            }
        });
    });