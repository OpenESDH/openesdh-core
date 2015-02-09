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

define(["dojo/_base/declare", "alfresco/core/Core", "alfresco/dashlets/Dashlet", "dojo/_base/lang",
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
                this.widgetsForBody = [
                    {
                        name: "alfresco/buttons/AlfDynamicPayloadButton",
                        config: {
                            label: this.message("notes.add"),
                            publishTopic: "ALF_CREATE_FORM_DIALOG_REQUEST",
                            publishPayload: {
                                dialogTitle: this.message("notes.add.dialog.title"),
                                dialogConfirmationButtonTitle: this.message("notes.form.add.label"),
                                dialogCancellationButtonTitle: this.message("notes.form.cancel.label"),
                                formSubmissionTopic: "ALF_CRUD_CREATE",
                                formSubmissionPayloadMixin: {
                                    url: "api/openesdh/node/" + NodeUtils.processNodeRef(this.nodeRef).uri + "/notes",
                                    // Refresh the notes list
                                    pubSubScope: "OPENESDH_NOTES_DASHLET"
                                },
                                widgets: [
                                    {
                                        name: "alfresco/forms/controls/DojoValidationTextBox",
                                        config: {
                                            label: "Author",
                                            name: "author"
                                        }
                                    },
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
                    },
                    {
                        name: "openesdh/common/widgets/lists/NoteList",
                        config: {
                            pubSubScope: "OPENESDH_NOTES_DASHLET",
                            nodeRef: this.nodeRef
                        }
                    }
                ];
            }
         });
      });