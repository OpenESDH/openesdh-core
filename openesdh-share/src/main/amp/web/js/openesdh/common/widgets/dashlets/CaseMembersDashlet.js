/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * CaseMembers dashlet
 *
 * @module openesdh/common/widgets/dashlets/CaseMembersDashlet
 * @extends alfresco/dashlet/Dashlet
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/dialogs/AlfDialog",
        "dojo/_base/lang",
        "alfresco/dashlets/Dashlet" ,
        "openesdh/common/services/_CaseMembersServiceTopicsMixin"],
    function(declare, AlfCore, AlfDialog, lang, Dashlet, CaseMembersServiceTopicsMixin) {

        return declare([Dashlet, CaseMembersServiceTopicsMixin], {

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.case.CaseMembersDashlet",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/CaseInfoDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/CaseMembersDashlet.properties"}],

            caseNodeRef: null,

            caseId: null,

            statusDialog: null,

            constructor: function(args) {
                this.inherited(arguments);
                this.caseNodeRef = args.caseNodeRef;
                this.caseId = args.caseId;
                this.widgetsForBody[0].config.caseNodeRef = this.caseNodeRef;
                this.widgetsForBody[0].config.caseId = this.caseId;

                this.alfSubscribe(this.CaseMembersGroupRetrieved , lang.hitch(this, "showGroupMembersDialog"));
            },

            showGroupMembersDialog : function  openesdh_generate_group_members_link(payload) {
                //Seeing as the tableview is pased the data as opposed to subscribing to a data return topic,
                //we push the payload into the expected data structure that AlfList#processLoadedData requires
                var data = { items: payload.members};
                if (this.statusDialog == null) {
                    //TODO perhaps hitch this to a topic that provides a function to teardown the dialog and remove it on close
                    //this.alfSubscribe(this.postNewUserStatusTopic, lang.hitch(this, "postStatus"));
                    this.statusDialog = new AlfDialog({
                        title: this.message("Group Members"),
                        widgetsContent: [ {
                            name: "openesdh/common/widgets/dashlets/views/UserInfoTableView",
                            config: {
                                subscribeToDocRequests: false,
                                showRoles: false,
                                currentData: data
                            }
                        } ],
                        widgetsButtons: [{
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: this.message("Ok")
                            }
                        }]
                    });
                }
                this.statusDialog.show();
            },

            /**
             * The widgets to be processed to generate each item in the rendered view.
             *
             * @instance
             * @type {object[]}
             * @default null
             */
            widgetsForBody: [{
                    name: "openesdh/pages/case/widgets/CaseMembersWidget",
                    config:{
                        sortField : "role",
                        pubSubScope: "OPENESDH_CASE_MEMBERS_DASHLET"
                    }
            }]

        });
    });