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
        "openesdh/pages/case/members/PartyRoleWidget",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin"
    ],
    function (declare, lang, array, template, _Widget, _Templated, Core, AlfFormDialog, NotificationUtils,
              CoreWidgetProcessing, PartyRoleWidget, _CaseMembersServiceTopicsMixin) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin, NotificationUtils], {
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
                this.alfSubscribe(this.PartiesSelectedTopic, lang.hitch(this, "_addPartyToRole"));

                this.alfSubscribe("ALF_WIDGETS_READY", lang.hitch(this, "onAllWidgetsReady"));
            },

            onAllWidgetsReady: function (payload) {
                this.alfPublish(this.GetCaseParties, this.caseId);
            },

            _onAddContacts: function (payload) {
                this.role = payload.role;

                if(this.authorityPickerDialog){
                    this.authorityPickerDialog.destroy();
                }
                this.authorityPickerDialog = new AlfFormDialog({
                    id:"contact_picker_dialog",
                    dialogTitle: this.message("contact.list.dialog.title"),
                    dialogConfirmationButtonTitle: this.message("contact.list.dialog.button.label.ok"),
                    dialogCancellationButtonTitle: this.message("contact.list.dialog.button.label.cancel"),
                    formSubmissionTopic: this.PartiesSelectedTopic,
                    fixedWidth: true,
                    widgetsContent: [
                        {
                            name: "openesdh/common/widgets/picker/PickerWithHeader",
                            config: {
                                widgetsForPickerHeader: [
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
                                        },
                                        widthPc: "50"
                                    }
                                ],
                                widgetsForPickedItems: [
                                    {
                                        name: "alfresco/pickers/PickedItems",
                                        config: {
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
                                                                        {name: "openesdh/common/widgets/renderers/PersonThumbnail"}
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
                                                        authorityType: "contact:base"
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

            _addPartyToRole: function (payload) {
                var _this = this;

                var textResult = this.message('case-party.add-role-success');
                var textError = this.message('case-party.add-role-failure');
                var textAlreadyAssigned = this.message('case-party.add-role-already-assigned');

                this.alfPublish(_this.AddContactsToPartyRoleTopic, {
                    contactNodeRefs: this._getUserNodeRefs(payload),
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
                        streetName: party.streetName,
                        houseNumber: party.houseNumber,
                        cityName: party.cityName,
                        countryCode: party.countryCode,
                        postCode: party.postCode,
                        postBox: party.postBox,
                        contactType: party.contactType,
                        roleTypes: _this.roleTypes,
                        isReadOnly: _this.isReadOnly
                    });
                    _this.widgets.push(partyRoleWidget);
                    partyRoleWidget.placeAt(_this.containerNode);
                });
            },

            //In the dialog the contents of the dialog are returned as objects for each
            _getUserNodeRefs: function (payload) {
                var nodeRefs = [];
                for (var n = 0; n < payload.length; n++) {
                    nodeRefs.push(payload[n].nodeRef);
                }
                return nodeRefs;
            }
        });
    });
