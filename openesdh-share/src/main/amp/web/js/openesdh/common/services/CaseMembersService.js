define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin"],
    function (declare, AlfCore, CoreXhr, array, lang, _CaseMembersServiceTopicsMixin) {

        return declare([AlfCore, CoreXhr, _CaseMembersServiceTopicsMixin], {

            nodeRef: "",

            caseId: "",

            constructor: function (args) {
                lang.mixin(this, args);
                this.alfSubscribe(this.CaseMembersAddToRoleTopic, lang.hitch(this, "_onCaseMemberAddToRole"));
                this.alfSubscribe(this.CaseMembersChangeRoleTopic, lang.hitch(this, "_onCaseMemberChangeRole"));
                this.alfSubscribe(this.CaseMembersRemoveRoleTopic, lang.hitch(this, "_onCaseMemberRemoveRole"));
                this.alfSubscribe(this.CaseMembersGet, lang.hitch(this, "_loadCaseMembers"));
                this.alfSubscribe(this.CaseMembersGroupGet, lang.hitch(this, "_getCaseMembersGroup"));
                this.alfSubscribe(this.CaseNavigateToUserProfilePage, lang.hitch(this, "_navigateToUserProfile"));
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