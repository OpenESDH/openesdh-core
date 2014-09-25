define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/MembersList.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin",
        "openesdh/pages/case/members/MemberRoleWidget"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, array, _CaseMembersServiceTopicsMixin, MemberRoleWidget) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin], {
            templateString: template,

            cssRequirements: [
                {cssFile: "./css/MembersList.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/MembersList.properties"}
            ],

            postCreate: function () {
                this.inherited(arguments);

                this.widgets = [];

                // TODO: Load
                this.roleTypes = ["CaseSimpleReader", "CaseSimpleWriter"];
                this.alfSubscribe(this.CaseMembersTopic, lang.hitch(this, "_onCaseMembers"));
                this.alfPublish(this.CaseMembersGet);
            },

            _onCaseMembers: function (payload) {
                var _this = this;

                array.forEach(this.widgets, function (widget, i) {
                    widget.destroyRecursive();
                });

                this.widgets = [];

                array.forEach(payload.members, function (member) {
                    var memberRoleWidget = new MemberRoleWidget({
                        authorityType: member.authorityType,
                        authority: member.authority,
                        authorityName: member.authorityName,
                        authorityRole: member.role,
                        roleTypes: _this.roleTypes
                    });
                    _this.widgets.push(memberRoleWidget);
                    memberRoleWidget.placeAt(_this.containerNode);
                });
            }
        });
    });
