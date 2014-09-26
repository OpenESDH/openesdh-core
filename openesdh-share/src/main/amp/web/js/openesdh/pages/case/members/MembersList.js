define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/MembersList.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin",
        "openesdh/pages/case/members/MemberRoleWidget",
        "openesdh/common/utils/AuthorityPickerUtils",
        "alfresco/core/NotificationUtils",
        "alfresco/html/Label"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, array, _CaseMembersServiceTopicsMixin, MemberRoleWidget, AuthorityPickerUtils, NotificationUtils, Label) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin, AuthorityPickerUtils, NotificationUtils], {
            templateString: template,

            cssRequirements: [
                {cssFile: "./css/MembersList.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/MembersList.properties"}
            ],

            /**
             * An array containing the role types for the case.
             * @type {string[]}
             */
            roleTypes: null,

            postCreate: function () {
                this.inherited(arguments);

                this.widgets = [];

                this.alfSubscribe(this.CaseMembersTopic, lang.hitch(this, "_onCaseMembers"));
                this.alfSubscribe("CASE_MEMBERS_ADD_TO_ROLE_CLICK", lang.hitch(this, "_onAddCaseMembers"));
                this.alfPublish(this.CaseMembersGet, {});
            },

            _onAddCaseMembers: function (payload) {
                console.log(payload);
                var role = payload.role;
                var _this = this;

                this.popupAuthorityPicker("cm:object", true, [],
                    function(obj) {
                        var authorityNodeRefs = obj.selectedItems;
                        if (authorityNodeRefs.length == 0) {
                            return false;
                        }

                        var textResult = this.message('case-members.add-role-success');
                        var textError = this.message('case-members.add-role-failure');
                        var textAlreadyAssigned = this.message('case-members.add-role-already-assigned');

                        _this.alfPublish(_this.CaseMembersAddToRoleTopic, {
                            authorityNodeRefs: authorityNodeRefs,
                            role: role,
                            successCallback: function () {
                                _this.displayMessage(textResult);
                                _this.alfPublish(_this.CaseMembersGet, {});
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

            _onCaseMembers: function (payload) {
                var _this = this;

                array.forEach(this.widgets, function (widget, i) {
                    widget.destroyRecursive();
                });

                this.widgets = [];

                var members = payload.members;
                // Sort by display name
                members.sort(function (a, b) {
                    return a.displayName.localeCompare(b.displayName);
                });

                array.forEach(members, function (member) {
                    var memberRoleWidget = new MemberRoleWidget({
                        authorityType: member.authorityType,
                        authority: member.authority,
                        displayName: member.displayName,
                        authorityRole: member.role,
                        roleTypes: _this.roleTypes
                    });
                    _this.widgets.push(memberRoleWidget);
                    memberRoleWidget.placeAt(_this.containerNode);
                });

//                if (members.length == 0) {
//                    var labelWidget = new Label({
//                        label: this.message("case-members.none")
//                    });
//                    this.widgets.push(labelWidget);
//                    labelWidget.placeAt(this.containerNode);
//                }
            }
        });
    });
