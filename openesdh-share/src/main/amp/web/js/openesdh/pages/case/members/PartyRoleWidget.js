define(["dojo/_base/declare",
        "openesdh/pages/case/members/MemberRoleWidget",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-attr",
        "dijit/form/Select",
        "alfresco/buttons/AlfButton",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin",
        "alfresco/core/NotificationUtils",
        "alfresco/core/UrlUtils"
    ],
    function (declare, MembersRoleWidget, lang, array, domAttr, Select, AlfButton, _CaseMembersServiceTopicsMixin, NotificationUtils, UrlUtils) {
        return declare([MembersRoleWidget,_CaseMembersServiceTopicsMixin, NotificationUtils, UrlUtils], {

            cssRequirements: [
                {cssFile: "./css/PartyRoleWidget.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/PartyRoleWidget.properties"}
            ],

            /**
             * The type of authority to show.
             * Either "user" or "group".
             * @type {string}
             */
            contactType: "PERSON",

            /**
             * The authority internal name.
             * @type {string}
             */
            party: "",

            /**
             * The role associated with the authority.
             * @type {string}
             */
            partyRole: "",

            /**
             * The roles that can be chosen from.
             *
             * Array in the form [{name: "SimpleWriter", label:"Simple Writer"}].
             * @type {Object[]}
             */
            roleTypes: null,

            postCreate: function () {
                var _this = this;

                var photoUrl;
                var alt;
                if (this.contactType == "PERSON") {
                    photoUrl = Alfresco.constants.URL_CONTEXT + "res/components/images/no-user-photo-64.png";
                    alt = "avatar";
                } else {
                    photoUrl = Alfresco.constants.URL_CONTEXT + "res/components/images/group-64.png";
                    alt = this.message('case-party.group-img-alt');
                }
                
                domAttr.set(this.authorityPictureNode, "src", photoUrl);
                domAttr.set(this.authorityPictureNode, "alt", alt);

                domAttr.set(this.authorityUserNameNode, "innerHTML", "("+ this.party +")");

                domAttr.set(this.authorityExtraInfoNode, "innerHTML", this.getExtraInfo());

                var options = array.map(this.roleTypes, function (roleType) {
                    return {label: _this.message("roles." + roleType.toLowerCase()), value: roleType};
                });
                this.roleSelectWidget = new Select({ id: this.party + "-" + this.partyRole + "-select", options: options, disabled: _this.isReadOnly });
                this.roleSelectWidget.set('value', this.partyRole, false);
                this.roleSelectWidget.placeAt(this.roleNode);

                if(!_this.isReadOnly){
                    
                    this.roleSelectWidget.on("change", lang.hitch(this, "_onRoleChanged"));
                    
                    var removeButton = new AlfButton({
                        id: this.party + "-" + this.partyRole + "-remove",
                        label: this.message("case-party.remove"),
                        onClick: lang.hitch(this, '_onRemoveRoleClick')
                    });
                    removeButton.placeAt(this.removeButtonNode);
                }
            },
            
            getExtraInfo : function(){
            	var extraInfo = [];
            	if(this.postBox && this.postBox.length > 0){
            		extraInfo.push("PO box " + this.postBox);
            	}
            	extraInfo.push(this.streetName + " " + this.houseNumber);
            	extraInfo.push(this.cityName);            	
            	extraInfo.push(this.countryCode + " " + this.postCode);
            	return extraInfo.join(", ");
            },

            _onRoleChanged: function () {
                var _this = this;
                var newRole = this.roleSelectWidget.get("value");
                var oldRole = this.partyRole;

                var textResult = this.message('case-party.' + this.contactType.toLowerCase() + '-change-role-success', [this.displayName, this._getRoleName(newRole)]);
                var textError = this.message('case-party.' + this.contactType.toLowerCase() + '-change-role-failure', [this.displayName]);
                var textAlreadyAssigned = this.message('case-party.' + this.contactType.toLowerCase() + '-change-role-already-assigned', [this.displayName]);

                this.alfPublish(this.PartyChangeRoleTopic, {
                    partyId: this.party,
                    oldRole: oldRole,
                    newRole: newRole,
                    successCallback: function () {
                        _this.displayMessage(textResult);
                        // Set the internal role to the new role
                        _this.partyRole = newRole;
                    },
                    failureCallback: function (response, config) {
                        // Handle different reasons for failure
                        if ("duplicate" in response.response.data) {
                            _this.displayMessage(textAlreadyAssigned);
                        } else {
                            _this.displayMessage(textError);
                        }
                        // Set the role back to the old role
                        _this.roleSelectWidget.set('value', _this.partyRole, false);
                    }
                });
            },

            /**
             * Get the human-friendly role label for the given role name.
             * @param role
             * @returns {string}
             * @private
             */
            _getRoleName: function (role) {
                return this.roleSelectWidget.get("displayedValue");
            },

            _onRemoveRoleClick: function () {
                var _this = this;
                this.alfLog("debug", "Remove", this.party, "from role", this.partyRole);
                var text = this.message(
                        "case-party." + this.contactType.toLowerCase() + "-remove", [
                        this.displayName,
                        this._getRoleName(this.partyRole)
                    ]
                );
                if (confirm(text)) {
                    var textResult = this.message('case-party.' + this.contactType.toLowerCase() + '-remove-success', [this.displayName]);
                    var textError = this.message('case-party.' + this.contactType.toLowerCase() + '-remove-failure', [this.displayName]);

                    this.alfPublish(this.PartyRemoveRoleTopic,
                        {
                            partyId: this.party,
                            role: this.partyRole,
                            successCallback: function () {
                                _this.displayMessage(textResult);
                                // Reload the members list
                                _this.alfPublish(_this.GetCaseParties, {});
                            },
                            failureCallback: function () {
                                _this.displayMessage(textError);
                            }
                        });
                }
            }
        });
    });
