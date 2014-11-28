define(["dojo/_base/declare",
        "alfresco/lists/AlfSortablePaginatedList",
        "dojo/_base/lang"],
    function (declare, AlfSortablePaginatedList, lang) {
        return declare([AlfSortablePaginatedList], {
            postMixInProperties: function () {
                this.inherited(arguments);
                this.alfSubscribe("PARTY_LIST_SEARCH", lang.hitch(this, this.loadPartyList))
            },

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

            loadPartyList: function (payload) {
                this.searchTerm = payload.term;
                this.onViewSelected({
//                    value: payload.view
                    value: "table"
                });
                this.loadData();
            },

            loadData: function () {
                if (this.searchTerm != "") {
                    this.inherited(arguments);
                }
            },

            updateLoadDataPayload: function (payload) {
                if (this.searchTerm != "") {
                    payload.url = "api/openesdh/partysearch?baseType=" +
                        encodeURIComponent(this.partyType) + "&term=" +
                        encodeURIComponent(this.searchTerm);
                    // Not working:
//                    payload.baseType = this.partyType;
//                    payload.term = this.searchTerm;
                }
            }
        });
    });