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
        "alfresco/dashlets/Dashlet",
        "dojo/_base/lang",
        "dojo/on"
    ],
    function (declare, AlfCore, Dashlet, lang, on) {

        return declare([Dashlet], {

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.case.MyCasesDashlet",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/MyCasesDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/MyCasesDashlet.properties"}],

            widgetsForToolbar: [
                {
                    name: "alfresco/forms/controls/DojoSelect",
                    id: "MY_CASES_DASHLET_OPTIONS",
                    config: {
                        fieldId: "MY_CASES_DASHLET_OPTIONS",
                        optionsConfig: {
                            fixed: [
                                {
                                    "label": "mycases.select.userinvolvedsearch",
                                    "value": "userinvolvedsearch"
                                },
                                {
                                    "label": "mycases.select.lastmodifiedcases",
                                    "value": "lastmodifiedbymesearch"
                                }
                            ]
                        }
                    }
                }
            ],

            widgetsForBody: [
                {
                    name: "openesdh/pages/case/widgets/MyCasesWidget",
                    config: {
                        showPagination: false
                    }
                }
            ],

            postCreate: function () {
                this.inherited(arguments);
                this.alfSubscribe("_valueChangeOf_MY_CASES_DASHLET_OPTIONS", lang.hitch(this, "onDropdownChanged"));
            },

            onDropdownChanged: function (payload) {
                this.alfPublish("GRID_SET_TARGET_URI", {
                    targetUri: "api/openesdh/" + payload.value
                });
            }
        });
    });