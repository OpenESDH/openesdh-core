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
        "openesdh/common/widgets/dashlets/Dashlet",
        "dojo/_base/lang",
        "alfresco/core/NodeUtils"],
      function(declare, AlfCore, Dashlet, lang, NodeUtils) {

         return declare([Dashlet], {

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.dashlet.NotesDashlet",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/NotesDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/NotesDashlet.properties"}],

            constructor: function (args) {
                lang.mixin(this, args);

                this.widgetsForTitleBarActions = [
                    {
                        name: "alfresco/buttons/AlfDynamicPayloadButton",
                        config: {
                            iconClass: "add-icon-16",
                            label: this.message("notes.button.label.add"),
                            publishTopic: "ALF_CREATE_FORM_DIALOG_REQUEST",
                            publishPayload: {
                                dialogTitle: this.message("notes.add.dialog.title"),
                                dialogConfirmationButtonTitle: this.message("notes.form.add.label"),
                                dialogCancellationButtonTitle: this.message("notes.form.cancel.label"),
                                formSubmissionTopic: "ALF_CRUD_CREATE",
                                formSubmissionPayloadMixin: {
                                    url: "api/openesdh/node/" + NodeUtils.processNodeRef(this.nodeRef).uri + "/notes",
                                    // Refresh the notes list
                                    pubSubScope: "OPENESDH_NOTES_DASHLET",
                                    alfResponseTopic: "OPENESDH_NOTES_DASHLETALF_CRUD_CREATE"
                                },
                                widgets: [
                                    {
                                        name: "alfresco/forms/controls/DojoTextarea",
                                        config: {
                                            label: "Content",
                                            name: "content"
                                        }
                                    }
                                ]
                            }
                        }
                    }
                ];

                this.widgetsForBody = [
                    {
                        name: "openesdh/pages/case/widgets/NotesGrid",
                        config: {
                            pubSubScope: "OPENESDH_NOTES_DASHLET",
                            gridRefreshTopic: "ALF_CRUD_CREATE_SUCCESS",
                            nodeRef: this.nodeRef
                        }
                    }
                ];
            }
         });
      });