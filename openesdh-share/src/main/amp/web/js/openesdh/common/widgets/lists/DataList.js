/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
 * This is an example extension for a blog post.
 *
 * @module blog/DataList
 * @extends module:alfresco/lists/AlfList
 * @author Dave Draper
 */
define(["dojo/_base/declare",
        "alfresco/lists/AlfList",
        "dojo/_base/lang"],
        function(declare, AlfList, lang) {

   return declare([AlfList], {

      /**
       * Extend this Dojo widget lifecycle function to create a new subscription for handling Data List
       * selections.
       *
       * @instance
       */
      postMixInProperties: function blog_DataList__postMixInProperties() {
         this.inherited(arguments);
         this.alfSubscribe("BLOG_LOAD_DATA_LIST", lang.hitch(this, this.loadDataList))
      },

      /**
       * This is the variable that will be used to determine what the next Data List to load will be.
       *
       * @instance
       * @type {object}
       * @default null
       */
      dataListToLoad: null,

      /**
       * Set the appropriate view, set the Data List to load and load the data for it.
       *
       * @instance
       * @param {object} payload The payload containing the details of the Data List to load
       */
      loadDataList: function blog_DataList__loadDataList(payload) {
         this.dataListToLoad = payload;
         this.onViewSelected({
            value: payload.itemType
         });
         this.loadData();
      },

      /**
       * Only load data when there is a Data List to load.
       *
       * @instance
       */
      loadData: function alfresco_lists_AlfList__loadData() {
         if (this.dataListToLoad != null)
         {
            this.inherited(arguments);
         }
      },

      /**
       * Update the payload used to load data to set the specific Data List URL
       *
       * @instance
       * @param {object} payload The payload containing the details of the Data List to load
       */
      updateLoadDataPayload: function blog_DataList__updateLoadDataPayload(payload) {
         if (this.dataListToLoad != null)
         {
            payload.url = "slingshot/datalists/data/node/" + this.dataListToLoad.nodeRef.replace("://", "/");
         }
      }
   });
});