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
        "alfresco/core/NotificationUtils"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, array, domAttr, _CaseMembersServiceTopicsMixin, Select, AlfButton, NotificationUtils) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin, NotificationUtils], {
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
            authorityName: "",

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
                // TODO: Use Torben's User widget
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
                    var authorityUrl = Alfresco.constants.URL_PAGECONTEXT + "user/" + this.authority + "/profile";
                    domAttr.set(this.authorityNameNode, "href", authorityUrl);
                }

                var options = array.map(this.roleTypes, function (roleType) {
                    return {label: _this.message("roles." + roleType.toLowerCase()), value: roleType};
                });
                this.roleSelectWidget = new Select({ options: options });
                this.roleSelectWidget.set('value', this.authorityRole, false);
                this.roleSelectWidget.placeAt(this.roleNode);

                this.roleSelectWidget.on("change", lang.hitch(this, "_onRoleChanged"));

                var removeButton = new AlfButton({
                    label: this.message("case-members.remove"),
                    onClick: lang.hitch(this, '_onRemoveRoleClick')
                });
                removeButton.placeAt(this.removeButtonNode);
            },

            _onRoleChanged: function () {
                var _this = this;
                var newRole = this.roleSelectWidget.get("value");
                var oldRole = this.authorityRole;

                var textResult = this.message('case-members.' + this.authorityType + '-change-role-success', [this.authorityName, this._getRoleName(newRole)]);
                var textError = this.message('case-members.' + this.authorityType + '-change-role-failure', [this.authorityName]);
                var textAlreadyAssigned = this.message('case-members.' + this.authorityType + '-change-role-already-assigned', [this.authorityName]);

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
                            // HACK: We don't get a JSON object back for some reason,
                            // so just find the word "duplicate" in the response
                            if (response.response.data.indexOf("duplicate") != -1) {
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
                        this.authorityName,
                        this._getRoleName(this.authorityRole)
                    ]
                );
                if (confirm(text)) {
                    var textResult = this.message('case-members.' + this.authorityType + '-remove-success', [this.authorityName]);
                    var textError = this.message('case-members.' + this.authorityType + '-remove-failure', [this.authorityName]);

                    this.alfPublish(this.CaseMembersRemoveRoleTopic,
                        {
                            authority: this.authority,
                            role: this.authorityRole,
                            successCallback: function () {
                                _this.displayMessage(textResult);
                                // Reload the members list
                                _this.alfPublish(_this.CaseMembersGet);
                            },
                            failureCallback: function () {
                                _this.displayMessage(textError);
                            }
                        });
                }
            }
        });
    });
