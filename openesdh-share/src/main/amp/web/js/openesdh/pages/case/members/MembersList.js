define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/text!./templates/MembersList.html",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "alfresco/core/Core",
        "alfresco/dialogs/AlfFormDialog",
        "alfresco/core/NotificationUtils",
        "alfresco/core/CoreWidgetProcessing",
        "openesdh/pages/case/members/MemberRoleWidget",
        "openesdh/common/widgets/picker/PickerWithHeader",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin"
    ],
    function (declare, lang, array, template, _Widget, _Templated, Core, AlfFormDialog, NotificationUtils, CoreWidgetProcessing,
              MemberRoleWidget, PickerWithHeader, _CaseMembersServiceTopicsMixin) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin, NotificationUtils], {
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

            //Not able to mix this in the dialog payload submitted
            currentRole: null,

            _allWidgetsReady: 0,

            postCreate: function () {
                this.inherited(arguments);

                this.widgets = [];
                this.alfSubscribe(this.CaseMembersTopic, lang.hitch(this, "_onCaseMembers"));
                this.alfSubscribe(this.caseMembersSelected, lang.hitch(this, "_addMemberToRole"));
                this.alfSubscribe("CASE_MEMBERS_ADD_TO_ROLE_CLICK", lang.hitch(this, "_onAddCaseMembersToRoleClick"));
                this.alfSubscribe("ALF_WIDGETS_READY", lang.hitch(this, "onAllWidgetsReady"));
            },

            onAllWidgetsReady: function () {
                this._allWidgetsReady++;
                if (this._allWidgetsReady == 1) {
                    this.alfPublish(this.CaseMembersGet, {});
                }
            },

            _onAddCaseMembersToRoleClick: function (payload) {
                this.currentRole = payload.role;
                if(this.authorityPickerDialog){
                    this.authorityPickerDialog.destroy();
                }
                this.authorityPickerDialog = new AlfFormDialog({
                    dialogTitle: this.message("members.list.dialog.title"),
                    dialogConfirmationButtonTitle: this.message("members.list.dialog.button.label.ok"),
                    dialogCancellationButtonTitle: this.message("members.list.dialog.button.label.cancel"),
                    formSubmissionTopic: this.caseMembersSelected,
                    fixedWidth: true,
                    widgetsContent: [
                        {
                            name: "openesdh/common/widgets/picker/PickerWithHeader",
                            config: {
                                widgetsForPickerHeader:[
                                    {
                                        name: "alfresco/layout/HorizontalWidgets",
                                        config: {
                                            widgets: [
                                                {
                                                    name: "openesdh/common/widgets/forms/SingleTextFieldForm",
                                                    config: {
                                                        id: "caseMembersSearchFieldForm",
                                                        useHash: false,
                                                        showOkButton: true,
                                                        okButtonLabel: "Search", //this.message("button.label.search"),
                                                        showCancelButton: false,
                                                        okButtonPublishTopic: "OE_UPDATE_SEARCH_TERM",
                                                        okButtonPublishGlobal: false,
                                                        textBoxLabel: "Search",
                                                        textFieldName: "searchTerm",
                                                        okButtonIconClass: "alf-white-search-icon",
                                                        okButtonClass: "call-to-action",
                                                        textBoxIconClass: "alf-search-icon",
                                                        textBoxRequirementConfig: {
                                                            initialValue: false
                                                        }
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                ],
                                widgetsForPickedItems: [
                                    {
                                        name: "alfresco/pickers/PickedItems",
                                        config:{
                                            widgets: [
                                                {
                                                    name: "alfresco/documentlibrary/views/layouts/Row",
                                                    config: {
                                                        widgets: [
                                                            {
                                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                                config: {
                                                                    width: "20px",
                                                                    widgets: [
                                                                        { name: "openesdh/common/widgets/renderers/PersonThumbnail" }
                                                                    ]
                                                                }
                                                            },
                                                            {
                                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                                config: {
                                                                    widgets: [
                                                                        {
                                                                            name: "alfresco/renderers/PropertyLink",
                                                                            config: {
                                                                                propertyToRender: "name",
                                                                                renderAsLink: false,
                                                                                publishTopic: ""
                                                                            }
                                                                        }
                                                                    ]
                                                                }
                                                            },
                                                            {
                                                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                                                config: {
                                                                    width: "20px",
                                                                    widgets: [
                                                                        {
                                                                            name: "alfresco/renderers/PublishAction",
                                                                            config: {
                                                                                iconClass: "delete-16",
                                                                                publishTopic: "ALF_ITEM_REMOVED",
                                                                                publishPayloadType: "CURRENT_ITEM"
                                                                            }
                                                                        }
                                                                    ]
                                                                }
                                                            }
                                                        ]
                                                    }
                                                }
                                            ]
                                        },
                                        assignTo: "pickedItemsWidget"
                                    }
                                ],
                                widgetsForRootPicker: [
                                    {
                                        name: "alfresco/layout/HorizontalWidgets",
                                        config: {
                                            widgets: [
                                                {
                                                    name: "openesdh/common/widgets/picker/AuthorityListPicker",
                                                    config: {
                                                        authorityType: "{authorityType}"
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                ],
                                singleItemMode: false,
                                generatePubSubScope: true
                            }
                        }
                    ]
                });
                this.authorityPickerDialog.show();
            },

            _addMemberToRole: function(payload){
                var _this = this;
                var textResult = this.message('case-members.add-role-success');
                var textError = this.message('case-members.add-role-failure');
                var textAlreadyAssigned = this.message('case-members.add-role-already-assigned');

                this.alfPublish(_this.CaseMembersAddToRoleTopic, {
                    authorityNodeRefs: this._getUserNodeRefs(payload),
                    role: this.currentRole,
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
                        id: member.authority + "-" + member.role,
                        authorityType: member.authorityType,
                        authority: member.authority,
                        displayName: member.displayName,
                        authorityRole: member.role,
                        roleTypes: _this.roleTypes,
                        isReadOnly: _this.isReadOnly
                    });
                    _this.widgets.push(memberRoleWidget);
                    memberRoleWidget.placeAt(_this.containerNode);
                });

            },

            //In the dialog the contents of the dialog are returned as objects for each
            _getUserNodeRefs: function(payload){
                var nodeRefs = [];
                for(var n = 0; n < payload.length; n++){
                    nodeRefs.push(payload[n].nodeRef);
                }
                return nodeRefs;
            }
        });
    });
