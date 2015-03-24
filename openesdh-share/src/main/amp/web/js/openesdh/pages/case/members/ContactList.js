define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/MembersList.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin",
        "openesdh/pages/case/members/PartyRoleWidget",
        "openesdh/common/utils/AuthorityPickerUtils",
        "alfresco/core/NotificationUtils",
        "alfresco/html/Label"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, array, _CaseMembersServiceTopicsMixin, PartyRoleWidget, AuthorityPickerUtils, NotificationUtils, Label) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin, AuthorityPickerUtils, NotificationUtils], {
            templateString: template,

            cssRequirements: [
                {cssFile: "./css/ContactList.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/ContactList.properties"}
            ],

            /**
             * An array containing the party role types for the case.
             * @type {string[]}
             */
            roleTypes: null,

            /**
             * The concerned case Id if needed
             */
            caseId: null,

            /**
             * The role we're currently interested in
             */
            role: null,

            postCreate: function () {
                this.inherited(arguments);

                this.widgets = [];

                this.alfSubscribe(this.CasePartiesTopic, lang.hitch(this, "_onCaseParties"));
                this.alfSubscribe("CASE_ADD_CONTACT_TO_ROLE_CLICK", lang.hitch(this, "_onAddContacts"));

                this.alfSubscribe("ALF_WIDGETS_READY", lang.hitch(this, "onAllWidgetsReady"));
            },

            onAllWidgetsReady: function (payload) {
                //console.log("openesdh/pages/case/members/ContactList(50) "+ this.caseId);
                this.alfPublish(this.GetCaseParties, this.caseId);
            },

            _onAddContacts: function (payload) {
                this.role = payload.role;
                var _this = this;

                this.popupAuthorityPicker("contact:base", true, [],
                    function(obj) {
                        var contactNodeRefs = obj.selectedItems;
                        if (contactNodeRefs.length == 0) {
                            return false;
                        }

                        var textResult = this.message('case-party.add-role-success');
                        var textError = this.message('case-party.add-role-failure');
                        var textAlreadyAssigned = this.message('case-party.add-role-already-assigned');

                        _this.alfPublish(_this.AddContactsToPartyRoleTopic, {
                            contactNodeRefs: contactNodeRefs,
                            role: _this.role,
                            caseId: _this.caseId,
                            successCallback: function () {
                                _this.displayMessage(textResult);
                                _this.alfPublish(_this.GetCaseParties, {caseId: _this.caseId} );
                            },
                            failureCallback: function (response, config) {
                                // Handle different reasons for failure
                                if ("duplicate" in response.response.data) {
                                    _this.displayMessage(textAlreadyAssigned);
                                } else {
                                    _this.displayMessage(textError);
                                }
                            }
                        });
                    });
            },

            _onCaseParties: function (payload) {
                var _this = this;

                array.forEach(this.widgets, function (widget, i) {
                    widget.destroyRecursive();
                });

                this.widgets = [];

                var parties = payload.parties;
                // Sort by display name
                parties.sort(function (a, b) {
                    return a.displayName.localeCompare(b.displayName);
                });

                array.forEach(parties, function (party) {
                    var partyRoleWidget = new PartyRoleWidget({
                        id: party.contactId + "-" + party.role,
                        party: party.contactId,
                        displayName: party.displayName,
                        partyRole: party.role,
                        contactType: "user",
                        roleTypes: _this.roleTypes
                    });
                    _this.widgets.push(partyRoleWidget);
                    partyRoleWidget.placeAt(_this.containerNode);
                });
            }
        });
    });
