define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/MemberRoleWidget.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-attr",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin",
        "dijit/form/Select",
        "alfresco/buttons/AlfButton",
        "alfresco/core/NotificationUtils",
        "alfresco/core/UrlUtils"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, array, domAttr, _CaseMembersServiceTopicsMixin, Select, AlfButton, NotificationUtils, UrlUtils) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin, NotificationUtils, UrlUtils], {
            templateString: template,

            cssRequirements: [
                {cssFile: "./css/MemberRoleWidget.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/MemberRoleWidget.properties"}
            ],

            /**
             * The type of authority to show.
             * Either "user" or "group".
             * @type {string}
             */
            authorityType: "user",

            /**
             * The authority internal name.
             * @type {string}
             */
            authority: "",

            /**
             * The display name of the authority.
             * @type {string}
             */
            displayName: "",

            /**
             * The role associated with the authority.
             * @type {string}
             */
            authorityRole: "",

            /**
             * The roles that can be chosen from.
             *
             * Array in the form [{name: "SimpleWriter", label:"Simple Writer"}].
             * @type {Object[]}
             */
            roleTypes: null,

            postCreate: function () {
                this.inherited(arguments);
                var _this = this;

                var photoUrl;
                var alt;
                if (this.authorityType == "user") {
                    photoUrl = Alfresco.constants.URL_CONTEXT + "res/components/images/no-user-photo-64.png";
                    alt = "avatar";
                } else {
                    photoUrl = Alfresco.constants.URL_CONTEXT + "res/components/images/group-64.png";
                    alt = this.message('case-members.group-img-alt');
                }
                domAttr.set(this.authorityPictureNode, "src", photoUrl);
                domAttr.set(this.authorityPictureNode, "alt", alt);

                if (this.authorityType == "user") {
                    domAttr.set(this.authorityDisplayNameNode, "innerHTML", this.userProfileLink(this.authority, this.displayName));
                } else {
                    domAttr.set(this.authorityUserNameNode, "innerHTML", "");
                }

                var options = array.map(this.roleTypes, function (roleType) {
                    return {label: _this.message("roles." + roleType.toLowerCase()), value: roleType};
                });
                this.roleSelectWidget = new Select({ id: this.authority + "-" + this.authorityRole + "-select", options: options });
                this.roleSelectWidget.set('value', this.authorityRole, false);
                this.roleSelectWidget.placeAt(this.roleNode);

                this.roleSelectWidget.on("change", lang.hitch(this, "_onRoleChanged"));

                var removeButton = new AlfButton({
                    id: this.authority + "-" + this.authorityRole + "-remove",
                    label: this.message("case-members.remove"),
                    onClick: lang.hitch(this, '_onRemoveRoleClick')
                });
                removeButton.placeAt(this.removeButtonNode);
            },

            _onRoleChanged: function () {
                var _this = this;
                var newRole = this.roleSelectWidget.get("value");
                var oldRole = this.authorityRole;

                var textResult = this.message('case-members.' + this.authorityType + '-change-role-success', [this.displayName, this._getRoleName(newRole)]);
                var textError = this.message('case-members.' + this.authorityType + '-change-role-failure', [this.displayName]);
                var textAlreadyAssigned = this.message('case-members.' + this.authorityType + '-change-role-already-assigned', [this.displayName]);

                this.alfPublish(this.CaseMembersChangeRoleTopic,
                    {
                        authority: this.authority,
                        oldRole: oldRole,
                        newRole: newRole,
                        successCallback: function () {
                            _this.displayMessage(textResult);
                            // Set the internal role to the new role
                            _this.authorityRole = newRole;
                        },
                        failureCallback: function (response, config) {
                            // Handle different reasons for failure
                            if ("duplicate" in response.response.data) {
                                _this.displayMessage(textAlreadyAssigned);
                            } else {
                                _this.displayMessage(textError);
                            }
                            // Set the role back to the old role
                            _this.roleSelectWidget.set('value', _this.authorityRole, false);
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
                this.alfLog("debug", "Remove", this.authority, "from role", this.authorityRole);
                var text = this.message(
                        "case-members." + this.authorityType + "-remove",
                    [
                        this.displayName,
                        this._getRoleName(this.authorityRole)
                    ]
                );
                if (confirm(text)) {
                    var textResult = this.message('case-members.' + this.authorityType + '-remove-success', [this.displayName]);
                    var textError = this.message('case-members.' + this.authorityType + '-remove-failure', [this.displayName]);

                    this.alfPublish(this.CaseMembersRemoveRoleTopic,
                        {
                            authority: this.authority,
                            role: this.authorityRole,
                            successCallback: function () {
                                _this.displayMessage(textResult);
                                // Reload the members list
                                _this.alfPublish(_this.CaseMembersGet, {});
                            },
                            failureCallback: function () {
                                _this.displayMessage(textError);
                            }
                        });
                }
            }
        });
    });
