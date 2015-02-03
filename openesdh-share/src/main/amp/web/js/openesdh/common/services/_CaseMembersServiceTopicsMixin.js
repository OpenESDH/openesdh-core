define(["dojo/_base/declare"],
    function(declare) {
        return declare(null, {
            CaseMembersTopic: "CASE_MEMBERS",
            CaseMembersGet: "CASE_MEMBERS_GET",
            CaseMembersAddToRoleTopic: "CASE_MEMBERS_ADD_TO_ROLE",
            CaseMembersChangeRoleTopic: "CASE_MEMBERS_CHANGE_ROLE",
            CaseMembersRemoveRoleTopic: "CASE_MEMBERS_REMOVE_ROLE",
            CaseMembersGroupGet: "CASE_RETRIEVE_GROUP_MEMBERS",
            CaseMembersGroupRetrieved: "CASE_RETRIEVE_GROUP_MEMBERS_SUCCESS",
            CaseNavigateToUserProfilePage: "CASE_NAVIGATE_TO_USERPROFILE"
        });
    });