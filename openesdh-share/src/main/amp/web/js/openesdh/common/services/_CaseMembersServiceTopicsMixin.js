define(["dojo/_base/declare"],
    function(declare) {
        return declare(null, {
            CaseMembersTopic: "CASE_MEMBERS",
            CasePartiesTopic: "CASE_PARTIES",
            CaseMembersGet: "CASE_MEMBERS_GET",
            CaseMembersSelected: "CASE_MEMBERS_SELECTED",
            CaseMembersAddToRoleTopic: "CASE_MEMBERS_ADD_TO_ROLE",
            CaseMembersChangeRoleTopic: "CASE_MEMBERS_CHANGE_ROLE",
            CaseMembersRemoveRoleTopic: "CASE_MEMBERS_REMOVE_ROLE",
            CaseMembersGroupGet: "CASE_RETRIEVE_GROUP_MEMBERS",
            CaseMembersGroupRetrieved: "CASE_RETRIEVE_GROUP_MEMBERS_SUCCESS",
            CaseNavigateToUserProfilePage: "CASE_NAVIGATE_TO_USERPROFILE",
            AddContactsToPartyRoleTopic: "CASE_ADD_CONTACT_TO_ROLE",
            GetCaseParties: "CASE_PARTIES_GET",
            PartiesSelectedTopic : "PARTIES_SELECTED",
            PartyChangeRoleTopic : "PARTY_CHANGE_ROLE",
            PartyRemoveRoleTopic : "PARTY_REMOVE_ROLE"
        });
    });