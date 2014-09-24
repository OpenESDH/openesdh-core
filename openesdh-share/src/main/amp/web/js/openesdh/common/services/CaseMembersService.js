define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin"],
    function (declare, AlfCore, CoreXhr, array, lang, _CaseMembersServiceTopicsMixin) {

        return declare([AlfCore, CoreXhr, _CaseMembersServiceTopicsMixin], {

            nodeRef: "",

            constructor: function (args) {
                lang.mixin(this, args);
                this.alfSubscribe(this.CaseMembersChangeRoleTopic, lang.hitch(this, "_onCaseMemberChangeRole"));
                this.alfSubscribe(this.CaseMembersRemoveRoleTopic, lang.hitch(this, "_onCaseMemberRemoveRole"));
                this.alfSubscribe(this.CaseMembersGet, lang.hitch(this, "_loadCaseMembers"));
            },

            _loadCaseMembers: function () {
                // Get members from webscript
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/casemembers?nodeRef=" + this.nodeRef;
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: this._onSuccessCallback,
                    callbackScope: this
                });
            },

            _onSuccessCallback: function (response, config) {
//                [
//                    {authorityType: "user", "authority": "admin", "authorityName": "Administrator", role: "casesimplewriter"},
//                    {authorityType: "user", "authority": "abeecher", "authorityName": "Alice Beecher", role: "casesimplereader"}
//                ]
                console.log(response);
                this.alfPublish(this.CaseMembersTopic, {members: response});
            },

            _onCaseMemberChangeRole: function (payload) {
                // TODO
                this.alfLog("debug", "Change", payload.authority, "from role", payload.oldRole, "to role", payload.newRole);
            },

            _onCaseMemberRemoveRole: function (payload) {
                // TODO
                this.alfLog("debug", "Remove", payload.authority, "from role", payload.authorityRole);
            }
        });
    });