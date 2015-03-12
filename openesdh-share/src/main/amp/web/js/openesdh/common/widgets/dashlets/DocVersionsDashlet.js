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
 * DocVersionsDashlet
 *
 * @module openesdh/common/widgets/dashlets/DocVersionsDashlet
 * @extends openesdh/common/widgets/dashlets/Dashlet
 * @author Lanre Abiwon
 */

define(["dojo/_base/declare", "alfresco/core/Core",
        "openesdh/common/widgets/dashlets/Dashlet",
        "dojo/_base/lang", "alfresco/core/NodeUtils",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"],
      function(declare, AlfCore, Dashlet, lang, NodeUtils,_DocumentTopicsMixin) {

         return declare([Dashlet,_DocumentTopicsMixin], {

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.dashlet.DocVersionsDashlet",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/DocVersionsDashlet.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/DocVersionsDashlet.properties"}],

             /*widgetsForTitleBarActions : [{}],*/

             /**
              * The widgets in the body
              */
             widgetsForBody : [
                 {
                     name: "openesdh/pages/case/widgets/DocumentVersionsGrid",
                     config: {
                         showPagination: false,
                         sort: [
                             { attribute: 'label', descending: true }
                         ],
                         showColumnHider: false
                     }
                 }
             ],

            constructor: function (args) {
                lang.mixin(this, args);
                    this.widgetsForBody[0].config.gridRefreshTopic = this.VersionsGridRefresh;
            }
         });
      });