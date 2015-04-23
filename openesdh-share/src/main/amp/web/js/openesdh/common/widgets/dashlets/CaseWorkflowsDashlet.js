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
 * CaseWorkflowsDashlet
 *
 * @module openesdh/common/widgets/dashlets/CaseWorkflowsDashlet
 * @extends alfresco/dashlet/Dashlet
 * @author Lanre
 */
define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/dashlets/Dashlet",
        "dojo/_base/lang",
        "dojo/on",
        "dijit/registry"
    ],
    function (declare, AlfCore, Dashlet, lang, on, dijitRegistry) {

        return declare([Dashlet], {

            cssRequirements: [{cssFile: "./css/CaseWorkflowsDashlet.css"}],

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.case.workflows",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/CaseWorkflowsDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/CaseWorkflowsDashlet.properties"}],

            caseNodeRef: null,

            widgetsForTitleBarActions: [
                {
                    name: "alfresco/buttons/AlfButton",
                    id: "new_workflow_button",
                    config: {
                        iconClass: "add-icon-16",
                        // TODO: i18n
                        label: "Start ny proces",
                        publishTopic: "OE_NAVIGATE_WORKFLOW_PAGE_TOPIC",
                        publishPayload: {caseNodeRef: this.caseNodeRef}
                    }
                }
            ],

            /*widgetsForToolbar: [
                {
                    name: "openesdh/common/widgets/controls/Select",
                    id: "caseWorkflowSelectControl",
                    config: {
                        fieldId: "CASE_DASHLET_WORKFLOW_OPTIONS",
                        optionsConfig: {
                            publishTopic: "OE_GET_CASE_WORKFLOW_OPTIONS"
                        }
                    }
                }
            ],*/

            widgetsForBody: [ ],

            postCreate: function () {
                this.inherited(arguments);
                var startCaseWorkflowButton = dijitRegistry.byId("new_workflow_button");
                startCaseWorkflowButton.publishPayload.caseNodeRef = this.caseNodeRef;
            }/*,

            onDropdownChanged: function (payload) {
                console.log("CaseWorkflowDashlet.js (86) Navigating to form....");
                this.alfPublish("ALF_NAVIGATE_TO_PAGE", {
                    type: "CONTEXT_RELATIVE",
                    target: "CURRENT",
                    url: "service/components/form?htmlid=template_x002e_start-workflow_x002e_start-workflow_x0023_" +
                    "default-startWorkflowForm-alf-id1&itemKind=workflow&itemId="+payload.value+"&mode=create&submitType" +
                    "=json&showCaption=true&formUI=true&showCancelButton=true"
                })
            }*/
        });
    });