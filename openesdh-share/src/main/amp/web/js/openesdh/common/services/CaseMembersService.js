define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin"],
    function (declare, AlfCore, CoreXhr, array, lang, _CaseMembersServiceTopicsMixin) {

        return declare([AlfCore, CoreXhr, _CaseMembersServiceTopicsMixin], {

            nodeRef: "",

            caseId: null,

            constructor: function (args) {
                lang.mixin(this, args);
                this.alfSubscribe(this.CaseMembersAddToRoleTopic, lang.hitch(this, "_onCaseMemberAddToRole"));
                this.alfSubscribe(this.CaseMembersChangeRoleTopic, lang.hitch(this, "_onCaseMemberChangeRole"));
                this.alfSubscribe(this.CaseMembersRemoveRoleTopic, lang.hitch(this, "_onCaseMemberRemoveRole"));
                this.alfSubscribe(this.CaseMembersGet, lang.hitch(this, "_loadCaseMembers"));
                this.alfSubscribe(this.CaseMembersGroupGet, lang.hitch(this, "_getCaseMembersGroup"));
                this.alfSubscribe(this.CaseNavigateToUserProfilePage, lang.hitch(this, "_navigateToUserProfile"));
                //Parties
                this.alfSubscribe(this.AddContactsToPartyRoleTopic, lang.hitch(this, "_onCaseAddContactToRole"));
                this.alfSubscribe(this.GetCaseParties, lang.hitch(this, "_getCaseParties"));
                this.alfSubscribe(this.PartyChangeRoleTopic, lang.hitch(this, "_onCasePartyChangeRole"));
                this.alfSubscribe(this.PartyRemoveRoleTopic, lang.hitch(this, "_onCaseRemovePartyRole"));
            },

            _getCaseParties: function (payload) {
                // Get members from webscript
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/case/"+this.caseId+"/parties",
                    method: "GET",
                    handleAs: "json",
                    successCallback: function (response, config) {
                        this.alfPublish(this.CasePartiesTopic, {parties: response});
                    },
                    failureCallback: function(response, config){
                        alert("Failed to load parties");
                    },
                    callbackScope: this
                });
            },

            _onCasePartyChangeRole: function (payload) {
                this.alfLog("debug", "Change", payload.partyId, "from role", payload.oldRole, "to role", payload.newRole);
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/case/"+this.caseId+"/party",
                    method: "PUT",
                    handleAs: "json",
                    data: {
                        fromRole: payload.oldRole,
                        newRole: payload.newRole,
                        partyId: payload.partyId
                    },
                    successCallback: payload.successCallback,
                    failureCallback: payload.failureCallback
                });
            },

            _onCaseAddContactToRole: function (payload) {
                this.alfLog("debug", "Add", payload.contactNodeRefs, "to role", payload.role);

                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/case/"+payload.caseId+"/party/"+payload.role,
                    method: "POST",
                    handleAs: "json",
                    data: {
                        contactNodeRefs: payload.contactNodeRefs
                    },
                    successCallback: payload.successCallback,
                    failureCallback: payload.failureCallback
                });
            },

            _onCaseRemovePartyRole: function (payload) {
                this.alfLog("debug", "Remove", payload.partyId, "from role", payload.role);
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/case/"+this.caseId+"/party/"+payload.role,
                    method: "DELETE",
                    handleAs: "json",
                    data: {
                        //For some reason repo is not receiving this object even though it's sent.
                        partyId: payload.partyId
                    },
                    query: {
                        partyId: payload.partyId
                    },
                    successCallback: payload.successCallback,
                    failureCallback: payload.failureCallback
                });
            },


            _loadCaseMembers: function () {
                // Get members from webscript
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/casemembers",
                    query: {
                        nodeRef: this.nodeRef
                    },
                    method: "GET",
                    handleAs: "json",
                    successCallback: function (response, config) {
                        this.alfPublish(this.CaseMembersTopic, {members: response});
                    },
                    callbackScope: this
                });
            },

            _navigateToUserProfile: function (payload) {
                window.location = Alfresco.util.uriTemplate("userprofilepage", {userid: payload.userName});
            },

            _getCaseMembersGroup: function (payload) {
                // Get members of a group that is the member of a case
                var groupShortName = payload.groupShortName;
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/groups/"+groupShortName+"/children?maxItems=10&&sortBy=authority" ,
                    query: {},
                    method: "GET",
                    handleAs: "json",
                    successCallback: function (response, config) {
                        this.alfPublish(this.CaseMembersGroupRetrieved, {members: response.data});
                    },
                    callbackScope: this
                });
            },

            _onCaseMemberAddToRole: function (payload) {
                this.alfLog("debug", "Add", payload.authorityNodeRefs, "to role", payload.role);
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/casemembers",
                    method: "POST",
                    handleAs: "json",
                    data: {},
                    query: {
                        nodeRef: this.nodeRef,
                        authorityNodeRefs: payload.authorityNodeRefs,
                        role: payload.role
                    },
                    successCallback: payload.successCallback,
                    failureCallback: payload.failureCallback
                });
            },

            _onCaseMemberChangeRole: function (payload) {
                this.alfLog("debug", "Change", payload.authority, "from role", payload.oldRole, "to role", payload.newRole);
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/casemembers",
                    method: "POST",
                    handleAs: "json",
                    data: {},
                    query: {
                        nodeRef: this.nodeRef,
                        authority: payload.authority,
                        fromRole: payload.oldRole,
                        role: payload.newRole
                    },
                    successCallback: payload.successCallback,
                    failureCallback: payload.failureCallback
                });
            },

            _onCaseMemberRemoveRole: function (payload) {
                this.alfLog("debug", "Remove", payload.authority, "from role", payload.role);
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/casemembers",
                    method: "DELETE",
                    handleAs: "json",
                    query: {
                        nodeRef: this.nodeRef,
                        authority: payload.authority,
                        role: payload.role
                    },
                    successCallback: payload.successCallback,
                    failureCallback: payload.failureCallback
                });
            }
        });
    });