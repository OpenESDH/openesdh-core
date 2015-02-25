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
 * @module openesdh/common/widgets/dashlets/CasePartiesDashlet
 * @extends alfresco/dashlet/Dashlet
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
   "alfresco/core/Core",
   "alfresco/dashlets/Dashlet"],
      function(declare, AlfCore, Dashlet) {

         return declare([Dashlet], {

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.case.CasePartiesDashlet",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/CaseInfoDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/CasePartiesDashlet.properties"}],

             /**
              * The case id for which to query for the parties (groups)
              */
             caseId:"",

             widgetsForTitleBarActions: [{
                 name: "alfresco/menus/AlfMenuItem",
                 config: {
                     iconImage: url.context + "/res/components/images/add-icon-16.png",
                     label: msg.get("party.dashlet.add.action"),
                     publishTopic: "CASE_ADD_PARTY",
                     publishPayload: {
                         caseId: caseId
                     },
                     publishGlobal: true
                 }
             }],

            /**
             * The widgets to be processed to generate each item in the rendered view.
             *
             * @instance
             * @type {object[]}
             * @default null
             */
            widgetsForBody: [
               {
                   name: "openesdh/pages/case/widgets/CaseHistoryWidget"
               }
            ]

         });
      });