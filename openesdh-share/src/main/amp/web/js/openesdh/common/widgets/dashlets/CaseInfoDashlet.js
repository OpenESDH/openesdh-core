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
//"alfresco/dashlets/Dashlet"
define(["dojo/_base/declare", 
        "alfresco/core/Core", 
        "alfresco/core/I18nUtils", 
        "openesdh/common/widgets/dashlets/Dashlet"],
      function(declare, AlfCore, I18nUtils, Dashlet) {
        
        var i18nScope = "openesdh.case.CaseInfoDashlet";

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
             * @default [{i18nFile: "./i18n/CaseInfoDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/CaseInfoDashlet.properties"}],

            /**
             * The widgets to be processed to generate each item in the rendered view.
             *
             * @instance
             * @type {object[]}
             * @default null
             */
            widgetsForBody: [
               {
                  name: "openesdh/pages/case/widgets/InfoWidget"
               }
            ]
            
            ,widgetsForFooterBarActions: [
                 {
                     name: "alfresco/buttons/AlfButton",
                     config: {
                         label: I18nUtils.msg(i18nScope, "case.dashlet.edit.case.info"),
                         publishTopic: "OPENESDH_CASE_INFO_EDIT"
                     }
                 },
                 {
                     name: "alfresco/buttons/AlfButton",
                     config: {
                         label: I18nUtils.msg(i18nScope, "case.dashlet.print.case"),
                         publishTopic: "OPENESDH_CASE_INFO_PRINT"
                     }
                 },
                 {
                     name: "alfresco/buttons/AlfButton",
                     config: {
                         label: I18nUtils.msg(i18nScope, "case.dashlet.start.workflow"),
                         publishTopic: "OPENESDH_CASE_INFO_START_WORKFLOW"
                     }
                 },
                 {
                     name: "alfresco/buttons/AlfButton",
                     config: {
                         label: I18nUtils.msg(i18nScope, "case.dashlet.create.site"),
                         publishTopic: "OPENESDH_CASE_INFO_CREATE_SITE"
                     }
                 }
            ],
            
            widgetsForTitleBarActions: [
                {
                    name: "alfresco/renderers/Favourite",
                    config: {
                        //iconClass: "fa fa-star fa-2x",
                        //label: "favorite",
                        //publishTopic: "OPENESDH_CASE_INFO_TOGGLE_FAVORITE",
                    	offLabel: ""
                    }
                },
            ]

         });
      });