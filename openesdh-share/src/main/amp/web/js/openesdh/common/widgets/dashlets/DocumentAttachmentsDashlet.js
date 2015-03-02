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

/**
 * SiteContentReportDashlet
 *
 * @module alfresco/dashlet/SiteContentReportDashlet
 * @extends alfresco/dashlet/Dashlet
 * @author Torben Lauritzen
 */
define(["dojo/_base/declare",
        "alfresco/core/Core",
        "openesdh/common/widgets/dashlets/Dashlet",
        "dojo/_base/lang",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"
    ],
    function (declare, AlfCore, Dashlet, lang, _DocumentTopicsMixin) {

        return declare([Dashlet, _DocumentTopicsMixin], {

            cssRequirements: [{cssFile: "./css/CaseDocumentsDashlet.css"}],

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.case.DocumentsDashlet",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/MyCasesDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/CaseDocumentsDashlet.properties"}],

            widgetsForTitleBarActions: [
                {
                    name: "alfresco/buttons/AlfButton",
                    config: {
                        // TODO: Add icon class
                        iconClass: "add-icon-16",
                        // TODO: i18n
                        label: "Tilføj Dokument",
                        publishTopic: "OE_SHOW_UPLOADER"
                    }
                }
            ],

            widgetsForBody: [
                {
                    name: "openesdh/pages/case/widgets/DocumentAttachmentsGrid",
                    config: {
                        sort: [
                            { attribute: 'cm:name', descending: true }
                        ]
                    }
                },
                // This widget is required to handle ALF_UPLOAD_REQUEST topics
                {
                    name: "alfresco/upload/AlfUpload"
                }
            ],

            constructor: function (args) {
                lang.mixin(this, args);
                // Pass in the nodeRef to the widget
                //Set the row selection topic
                this.widgetsForBody[0].config.rowSelectionTopic = this.AttachmentRowSelect;
                this.widgetsForBody[0].config.rowDeselectionTopic = this.AttachmentRowDeselect;
            },

            postCreate: function () {
                this.inherited(arguments);
                this.alfSubscribe(this.ReloadAttachmentsTopic, lang.hitch(this, "onReloadDocuments"));
            },

            onReloadDocuments: function (payload) {
                console.log("DocumentAttachmentsDashlet(97) NodeRef:"+ payload);
                lang.setObject("config.nodeRef", payload.nodeRef, this.widgetsForBody[0]);
                /*this.alfPublish("GRID_SORT", {
                    sort: [
                        { attribute: 'cm:modified', descending: true }
                    ]
                });*/
                this.alfPublish(this.AttachmentGridRefresh);
            }
        });
    });