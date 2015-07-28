/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

define(["dojo/_base/declare", "alfresco/core/Core",
        "alfresco/core/I18nUtils",
        "openesdh/common/widgets/dashlets/Dashlet",
        "dojo/_base/lang",
        "alfresco/core/NodeUtils"],
    function (declare, AlfCore, I18nUtils, Dashlet, lang, NodeUtils) {

        var i18nScope = "openesdh.dashlet.NotesDashlet";

        return declare([Dashlet], {

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: i18nScope,

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/NotesDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/NotesDashlet.properties"}],
            
//            widgetsForFooterBarActions: [
//              {
//                  name: "alfresco/buttons/AlfButton",
//                  config: {
//                      label: I18nUtils.msg(i18nScope, "comments.button.label.new"),
//                      publishTopic: "OPENESDH_NOTES_DASHLET_OPENESDH_CASE_COMMENTS_NEW",
//                      visibilityConfig: {
//                          initialValue: false,
//                          rules: [
//                              {
//                                  topic: "CASE_INFO",
//                                  attribute: "isJournalized",
//                                  is: [false]
//                              }
//                          ]
//                      }
//                  }
//              },
//              {
//                  name: "alfresco/buttons/AlfButton",
//                  config: {
//                      label: I18nUtils.msg(i18nScope, "comments.button.label.print.all"),
//                      publishTopic: "OPENESDH_CASE_COMMENTS_PRINT_ALL"
//                  }
//              }
//            ],

            widgetsForBody: [
                {
                    name: "openesdh/pages/case/widgets/NotesGrid",
                    config: {
                        pubSubScope: "OPENESDH_NOTES_DASHLET_",
                        gridRefreshTopic: "ALF_CRUD_CREATE_SUCCESS",
                    }
                }
            ],

            constructor: function (args) {
                lang.mixin(this, args);
                //this.widgetsForFooterBarActions[0].config.publishPayload.formSubmissionPayloadMixin.url = "api/openesdh/node/" + NodeUtils.processNodeRef(this.nodeRef).uri + "/notes";
                this.widgetsForBody[0].config.nodeRef = this.nodeRef;
                this.alfSubscribe("OPENESDH_NOTES_DASHLET_OPENESDH_CASE_COMMENTS_NEW", lang.hitch(this, "_NewCaseComment"));
            },
            
            _NewCaseComment: function(){
                this.alfPublish("ALF_CREATE_FORM_DIALOG_REQUEST", {
                    i18nScope: "openesdh.dashlet.NotesDashlet",
                    dialogTitle: I18nUtils.msg(i18nScope, "notes.add.dialog.title"),
                    dialogConfirmationButtonTitle: I18nUtils.msg(i18nScope, "notes.form.add.label"),
                    dialogCancellationButtonTitle: I18nUtils.msg(i18nScope, "notes.form.cancel.label"),
                    formSubmissionTopic: "ALF_CRUD_CREATE",
                    formSubmissionPayloadMixin: {
                        // Refresh the notes list
                        url: NodeUtils.processNodeRef(this.nodeRef).uri + "/notes",
                        pubSubScope: "OPENESDH_NOTES_DASHLET",
                        alfResponseTopic: "OPENESDH_NOTES_DASHLETALF_CRUD_CREATE"
                    },
                    widgets: [{
                            name: "alfresco/forms/controls/TextBox",
                            config: {
                                label: I18nUtils.msg(i18nScope, "comments.form.headline.label"),
                                name: "headline" 
                            }
                        },
                        {
                            name: "alfresco/forms/controls/TextBox",
                            config: {
                                label: I18nUtils.msg(i18nScope, "comments.form.parties.label"),
                                name: "relatedParties" 
                            }
                        },
                        {
                            name: "alfresco/forms/controls/DojoTextarea",
                            config: {
                                label: I18nUtils.msg(i18nScope, "comments.form.content.label"),
                                name: "content"
                            }
                        }
                    ]
                });
            }
        });
    });