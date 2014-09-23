define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/MemberRoleWidget.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin",
        "dijit/form/Select",
        "alfresco/buttons/AlfButton"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, array, _CaseMembersServiceTopicsMixin, Select, AlfButton) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin], {
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
                var options = array.map(this.roleTypes, function (roleType) {
                    return {label: _this.message("roles." + roleType.toLowerCase()), value: roleType};
                });
                this.roleSelectWidget = new Select({ options: options });
                this.roleSelectWidget.set('value', this.authorityRole, false);
                this.roleSelectWidget.placeAt(this.roleNode);

                this.roleSelectWidget.on("change", lang.hitch(this, "_onRoleChanged"));

                var removeButton = new AlfButton({
                    label: this.message("remove.button.label"),
                    onClick: lang.hitch(this, '_onRemoveRoleClick')
                });
                removeButton.placeAt(this.removeButtonNode);
            },

            _onRoleChanged: function () {
                var newRole = this.roleSelectWidget.get("value");
                var oldRole = this.authorityRole;
                this.alfPublish(this.CaseMembersChangeRoleTopic,
                    {
                        authority: this.authority,
                        oldRole: oldRole,
                        newRole: newRole
                    });
                this.authorityRole = newRole;
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
                this.alfLog("debug", "Remove", this.authority, "from role", this.authorityRole);
                var text = this.message(
                        "member." + this.authorityType + ".remove",
                    [
                        this.authorityName,
                        this._getRoleName(this.authorityRole)
                    ]
                );
                if (confirm(text)) {
                    this.alfPublish(this.CaseMembersRemoveRoleTopic,
                        {
                            authority: this.authority,
                            authorityRole: this.authorityRole
                        });
                }
            }
        });
    });
