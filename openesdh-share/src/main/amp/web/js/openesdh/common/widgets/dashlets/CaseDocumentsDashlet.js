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
                    id: "add_document_record_button",
                    config: {
                        iconClass: "add-icon-16",
                        i18nRequirements: [{i18nFile: "./i18n/CaseDocumentsDashlet.properties"}],
                        i18nScope: "openesdh.case.DocumentsDashlet",
                        label: "dashlet.button.label.document.upload",
                        publishTopic: "OE_SHOW_UPLOADER",
                        visibilityConfig: {
                            initialValue: false,
                            rules: [
                                {
                                    topic: "CASE_INFO",
                                    attribute: "isJournalized",
                                    is: [false]
                                }
                            ]
                        }
                    }
                }
            ],

            widgetsForBody: [
                {
                    name: "openesdh/pages/case/widgets/DocumentGrid",
                    config: {
                        sort: [
                            { attribute: 'cm:modified', descending: true}
                        ],
                        showColumnHider: false
                    }
                }

            ],

            constructor: function (args) {
                lang.mixin(this, args);
                // Pass in the nodeRef to the widget
                lang.setObject("config.nodeRef", this.nodeRef, this.widgetsForBody[0]);
                //Set the row selection topic
                this.widgetsForBody[0].config.rowSelectionTopic = this.DocumentRowSelect;
                this.widgetsForBody[0].config.rowDeselectionTopic = this.DocumentRowDeselect;
            },

            postCreate: function () {
                this.inherited(arguments);
                this.alfSubscribe(this.ReloadDocumentsTopic, lang.hitch(this, "onReloadDocuments"));
                this.alfSubscribe(this.CaseDocumentMoved, lang.hitch(this, "onDocumentMoved"));
            },

            onReloadDocuments: function (payload) {
                this.alfPublish("GRID_SORT", {
                    sort: [
                        { attribute: 'cm:modified', descending: true }
                    ]
                });
                this.alfPublish("GRID_REFRESH");
            },
            
            onDocumentMoved: function(){
            	this.alfPublish("GRID_REFRESH");
            }
        });
    });