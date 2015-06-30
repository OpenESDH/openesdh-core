define(["dojo/_base/declare",
        "alfresco/lists/AlfSortablePaginatedList",
        "dojo/_base/lang"],
    function (declare, AlfSortablePaginatedList, lang) {
        return declare([AlfSortablePaginatedList], {

            i18nRequirements: [
                {i18nFile: "./i18n/ContactList.properties"}
            ],

            /**
             * What contact type should be searched?
             * Good choices are "contact:person" or "contact:organization".
             *
             * @instance
             * @type string
             * @default ""
             */
            contactType: "",
            searchTerm: "",

            sortField: "",

            postMixInProperties: function () {
                this.inherited(arguments);
                this.alfSubscribe("CONTACT_LIST_SEARCH", lang.hitch(this, this.loadContactList));
                this.alfSubscribe("CONTACT_LIST_RELOAD", lang.hitch(this, this.loadData));
                this.alfSubscribe("CONTACT_LIST_SHOW_ALL", lang.hitch(this, this.showAll));
            },

            // Don't load data on page ready
            onPageWidgetsReady: function () {},

            postCreate: function () {
                this.inherited(arguments);
            },

            loadContactList: function (payload) {
                this.searchTerm = payload.searchTerm;
                this.loadData();
            },

            showAll: function () {
                this.searchTerm = '';
                this._readyToLoad = true;
                this.loadData();
            },

            loadData: function () {
//                if (this.searchTerm != "") {
                    this.inherited(arguments);
//                }
            },

            setDisplayMessages: function () {
                this.inherited(arguments);

                // Override the default messages
                this.noDataMessage = this.message("contactlist.no.data.message");
                // TODO: override as needed:
//                this.fetchingDataMessage = this.message("alflist.loading.data.message");
//                this.renderingViewMessage = this.message("alflist.rendering.data.message");
//                this.fetchingMoreDataMessage = this.message("alflist.loading.data.message");
//                this.dataFailureMessage = this.message("alflist.data.failure.message");
            },

            updateLoadDataPayload: function (payload) {
                this.inherited(arguments);
//                if (this.searchTerm != "") {
                    payload.url = "api/openesdh/contactsearch";
                    payload.baseType = this.contactType;
                    payload.term = this.searchTerm;
//                }
            }
        });
    });