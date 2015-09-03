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

define(["dojo/_base/declare", 
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "alfresco/core/I18nUtils",
        "openesdh/common/widgets/dashlets/Dashlet",
        "dojo/_base/lang",
        "dojo/_base/array",
        "alfresco/core/NodeUtils"],
    function (declare, AlfCore, CoreXhr, I18nUtils, Dashlet, lang, array, NodeUtils) {

        var i18nScope = "openesdh.dashlet.NotesDashlet";
        
        return declare([Dashlet, CoreXhr], {

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: i18nScope,
            
            caseId: null,

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/NotesDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/NotesDashlet.properties"}],

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
                this.alfSubscribe("OPENESDH_CASE_COMMENT_ADD", lang.hitch(this, "_NewCaseCommentSubmit"));
                
                this.alfSubscribe("OPENESDH_CASE_NOTE_CONCERNED_PARTIES_RETRIEVE", lang.hitch(this, "_PopulateCaseNoteParties"));
                
            },
            
            _NewCaseComment: function(){
                this.alfPublish("ALF_CREATE_FORM_DIALOG_REQUEST", {
                    i18nScope: "openesdh.dashlet.NotesDashlet",
                    dialogTitle: I18nUtils.msg(i18nScope, "notes.add.dialog.title"),
                    dialogConfirmationButtonTitle: I18nUtils.msg(i18nScope, "notes.form.add.label"),
                    dialogCancellationButtonTitle: I18nUtils.msg(i18nScope, "notes.form.cancel.label"),
                    formSubmissionTopic: "OPENESDH_CASE_COMMENT_ADD",
                    formSubmissionPayloadMixin: {
                        // Refresh the notes list
                        url: "api/openesdh/node/" + NodeUtils.processNodeRef(this.nodeRef).uri + "/notes",
                        pubSubScope: "OPENESDH_NOTES_DASHLET_",
                        alfResponseTopic: "OPENESDH_NOTES_DASHLET_ALF_CRUD_CREATE"
                    },
                    widgets: [{
                            name: "openesdh/common/widgets/controls/form/ValidationTextBox",
                            config: {
                                //label: I18nUtils.msg(i18nScope, "comments.form.headline.label"),
                                name: "headline",
                                placeHolder: I18nUtils.msg(i18nScope, "comments.form.headline.label"),
                                validationConfig: {
                                    validation: "regex",
                                    regex: ".+",
                                    errorMessage: I18nUtils.msg(i18nScope, "comments.form.headline.required.validation.error")
                                }
                            }
                        },
                        {
                            name: "openesdh/common/widgets/controls/form/MutiSelectInput",
                            config: {
                                name: "concernedParties",
                                placeHolder: I18nUtils.msg(i18nScope, "comments.form.parties.label"),
                                optionsConfig:{
                                    labelAttribute: "displayName",
                                    queryAttribute: "displayName",
                                    valueAttribute: "nodeRef",
                                    publishTopic: "OPENESDH_CASE_NOTE_CONCERNED_PARTIES_RETRIEVE",
                                    publishPayload: {
                                        resultsProperty: "items"
                                    }
                                }
                            }
                        },
                        {
                            name: "openesdh/common/widgets/controls/form/TextArea",
                            config: {
                                //label: I18nUtils.msg(i18nScope, "comments.form.content.label"),
                                name: "content",
                                placeHolder: I18nUtils.msg(i18nScope, "comments.form.content.label"),
                                validationConfig: {
                                    validation: "regex",
                                    regex: ".+",
                                    errorMessage: I18nUtils.msg(i18nScope, "comments.form.content.required.validation.error")
                                }
                            }
                        }
                    ]
                });
            },
            
            _NewCaseCommentSubmit: function(payload){
                var _this = this;
                var concernedParties = array.map(payload.concernedParties, function(party){
                    return party.nodeRef;
                });
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/node/" + NodeUtils.processNodeRef(this.nodeRef).uri + "/notes",
                    method: "POST",
                    handleAs: "json",
                    data: {
                        parent: this.nodeRef,
                        headline: payload.headline,
                        content: payload.content,
                        concernedParties: concernedParties
                    },
                    successCallback: function(response){
                        _this.alfPublish(payload.alfResponseTopic + "_SUCCESS", response);
                    },
                    callbackScope: this
                });
            },
            
            _PopulateCaseNoteParties: function(payload){
                var _this = this;
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "/api/openesdh/case/" + this.caseId + "/parties",
                    method: "GET",
                    handleAs: "json",
                    successCallback: function(response){
                        _this.alfPublish(payload.alfResponseTopic + "_SUCCESS", {"items" : response});
                    },
                    callbackScope: this
                });
            }
        });
    });