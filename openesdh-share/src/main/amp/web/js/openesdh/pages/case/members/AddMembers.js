define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/AddMembers.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin",
        "dijit/form/DropDownButton",
        "alfresco/menus/AlfDropDownMenu",
        "alfresco/menus/AlfMenuItem",
        "alfresco/dialogs/AlfDialog"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, array, _CaseMembersServiceTopicsMixin, DropDownButton, AlfDropDownMenu, AlfMenuItem, AlfDialog) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _CaseMembersServiceTopicsMixin], {
            templateString: template,

            cssRequirements: [
                {cssFile: "./css/AddMembers.css"},
                {cssFile: "./css/AlfrescoStyle.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/AddMembers.properties"}
            ],

            postCreate: function () {
                this.inherited(arguments);

                this.widgets = [];

                var menu = new AlfDropDownMenu({
                });
                var menuItem1 = new AlfMenuItem({
                    label: "Test"
                });
                menu.addChild(menuItem1);

                var addButton = new DropDownButton({
                    label: this.message("case-members.add"),
                    dropDown: menu
//                    onClick: lang.hitch(this, '_onAddMemberClick')
                });
                addButton.placeAt(this.addButtonNode);
            },

            _onAddMemberClick: function (payload) {
                var _this = this;
                var options = array.map(this.roleTypes, function (roleType) {
                        return {label: _this.message("roles." + roleType.toLowerCase()), value: roleType};
                });
                var dialog = new AlfDialog({
                    pubSubScope: this.pubSubScope,
                    title: this.message("xsearch.save_search_as.dialog.title"),
                    widgetsContent: [
                        {
                            name: "openesdh/xsearch/MagentaSingleUserSelect",
                            config: {
                                label: this.message("asdasd")
                            }
                        },
                        {
                            name: "alfresco/forms/controls/DojoSelect",
                            config: {
                                label: this.message("case-members.role-select"),
                                options: options
                            }
                        }
                    ],
                    widgetsButtons: [
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: this.message("xsearch.button.ok"),
                                publishTopic: "XSEARCH_SAVE_SEARCH_AS_OK",
                                publishPayload: {}
                            }
                        },
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: this.message("xsearch.button.cancel"),
                                publishTopic: "XSEARCH_SAVE_SEARCH_AS_CANCEL",
                                publishPayload: {}
                            }
                        }
                    ]

                });
                dialog.show();
            }
        });
    });
