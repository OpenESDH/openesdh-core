/**
 * <p>This extends the standard [document list]{@link module:alfresco/documentlibrary/AlfDocumentList} to
 * define an authority list specifically for selecting authorities. The skeleton code is based on
 * alfresco/pickers/DocumentListPicker.</p>
 *
 * @module openesdh/common/widgets/picker/AuthorityListPicker.js
 * @extends module:alfresco/documentlibrary/AlfDocumentList
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "alfresco/documentlibrary/AlfDocumentList",
        "dojo/_base/lang",
        "alfresco/core/ArrayUtils"],
        function(declare, AlfDocumentList, lang, arrayUtils) {

   return declare([AlfDocumentList], {

      /**
       * Overrides the [inherited value]{@link module:alfresco/lists/AlfList#waitForPageWidgets} to ensure that pickers
       * don't wait for the page to be loaded (as typically the page will be loaded long before the picker is opened).
       * This can still be overridden again in configuration when creating a new picker.
       *
       * @instance
       * @type {boolean}
       * @default false
       */
      waitForPageWidgets: false,

      /**
       * Change the topic we use to load the data
       */
      loadDataPublishTopic: "ALF_GET_AUTHORITY",

      /**
       * This topic that is published when a document is picked. By default it is the topic that indicates
       * that the document has been selected.
       *
       * @instance
       * @type {string}
       * @default "ALF_ITEM_SELECTED"
       */
      publishTopic: "ALF_ITEM_SELECTED",

      /**
       * This is the type of payload published when a document is picked. By default it is the current
       * item type.
       *
       * @instance
       * @type {string}
       * @default "CURRENT_ITEM"
       */
      publishPayloadType: "CURRENT_ITEM",

      /**
       * This is the configured payload published when a document is picked. By default it is null
       *
       * @instance
       * @type {object}
       * @default null
       */
      publishPayload: null,

      /**
       * This indicates whether the current item should be mixed into the published payload. By
       * default this is false (because the default type is to just publish the current item)
       *
       * @instance
       * @type {boolean}
       * @default false
       */
      publishPayloadItemMixin: false,

      /**
       * The default authority type to search for
       */
      authorityType : "cm:authority",

      /**
       * The modifiers to apply to the publish payload. This should only be set if the
       * [publishPayloadType]{@link module:alfresco/pickers/DocumentListPicker#publishPayloadType}
       * is set to "PROCESS".
       *
       * @instance
       * @type {array}
       * @default null
       */
      publishPayloadModifiers: null,

      /**
        * This topic is used to request that the List reloads data using its current parameters.
        *
        * @instance
        * @type {string}
        * @default "ALF_DOCLIST_RELOAD_DATA"
        */
      reloadDataTopic: "OE_RELOAD_AUTHORITIES",

       /**
        * The search term to use to filter the list. By default we retrieve all users (*)
        */
      searchTerm:"*",

       /**
        * This function sets up the subscriptions that the  List relies upon to manage its
        * internal state and request data.
        *
        * @instance
        */
       setupSubscriptions: function alfrescdo_documentlibrary_AlfDocumentList__setupSubscriptions() {
           this.inherited(arguments);
           this.alfSubscribe("OE_UPDATE_SEARCH_TERM", lang.hitch(this, this._updateSearchTerm));
       },

      /**
       * Overrides the [inherited function]{@link module:alfresco/lists/AlfList#postCreate} to create the picker
       * view for selecting documents.
       *
       * @instance
       */
      postCreate: function openESDH_authorityPicker__postCreate(payload) {
         var config = [{
            name: "alfresco/documentlibrary/views/AlfDocumentListView",
            config: {
               widgets: [
                  {
                     name: "alfresco/documentlibrary/views/layouts/Row",
                     config: {
                        widgets: [
                           {
                              name: "alfresco/documentlibrary/views/layouts/Cell",
                              config: {
                                 width: "20px",
                                 widgets: [
                                    { name: "openesdh/common/widgets/renderers/PersonThumbnail" }
                                 ]
                              }
                           },
                           {
                              name: "alfresco/documentlibrary/views/layouts/Cell",
                              config: {
                                 widgets: [
                                    {
                                       name: "alfresco/renderers/PropertyLink",
                                       config: {
                                          propertyToRender: "name",
                                          renderAsLink: false,
                                          publishTopic: ""
                                       }
                                    }
                                 ]
                              }
                           },
                           {
                              name: "alfresco/documentlibrary/views/layouts/Cell",
                              config: {
                                 width: "20px",
                                 widgets: [
                                    {
                                       name: "alfresco/renderers/PublishAction",
                                       config: {
                                          publishTopic: this.publishTopic,
                                          publishPayloadType: this.publishPayloadType,
                                          publishPayload: this.publishPayload,
                                          publishPayloadItemMixin: this.publishPayloadItemMixin,
                                          publishPayloadModifiers: this.publishPayloadModifiers,
                                          renderFilter: [
                                              {
                                                  property: "selectable",
                                                  values: [true]
                                              }
                                          ]
                                       }
                                    }
                                 ]
                              }
                           }
                        ]
                     }
                  }
               ]
            }
         }];
         this.processWidgets(config, this.itemsNode);
      },


      /**
       * Override the default implementation to call [loadData]{@link module:alfresco/documentlibrary/AlfDocumentList#loadData}
       * with the currently selected folder node.
       *
       * @instance
       * @param {object} payload
       */
      onItemLinkClick: function openESDH_authorityPicker__onItemLinkClick(payload) {
          var node = lang.getObject("item.nodeRef", false, payload) || payload.nodeRef;
          this.onDocumentClick(payload);
      },

      /**
       * Overrides inherited function to do a no-op. The pick action should be handled by a
       * [PublishAction widget]{@link module:alfresco/renderers/PublishAction}.
       *
       * @instance
       * @param {object} payload
       */
      _updateSearchTerm: function openESDH_authorityPicker__onDocumentClick(payload) {
         this.searchTerm = payload.searchTerm;
         this.loadData();
      },

       /**
        * Overrides [onDataLoadSuccess]{@link module:alfresco/documentlibrary/AlfList#onDataLoadSuccess}.
        *
        * @param payload
        */
       onDataLoadSuccess: function alfresco_pickers_DocumentListPicker__onDataLoadSuccess(payload) {
           this.alfLog("log", "Data Loaded", payload, this);
           var foundItems = false;
           if (this.itemsProperty == null)
           {
               this.currentData = {};
               this.currentData.items = payload.response;
               foundItems = true;
           }
           else
           {
               var items = lang.getObject(this.itemsProperty, false, payload.data);
               if (items == null)
               {
                   // As a fallback we're going to check the actual payload object...
                   // It would be reasonable to ask why we don't just look in payload initially and
                   // expect the "itemsProperty" to include "response", however that is not the most common
                   // scenario and this approach catches the edge cases...
                   items = lang.getObject(this.itemsProperty, false, payload);
               }

               if (items)
               {
                   this.currentData = {};
                   this.currentData.items = items;
                   foundItems = true;
               }
               else
               {
                   this.showDataLoadFailure();
               }
           }

           if (foundItems)
           {
               /*if (payload.data)
               {
                   this.processLoadedData(payload.data);
               }
               else
               {
                   this.processLoadedData(this.currentData);
               }*/
               this.renderView();
           }

           // This request has finished, allow another one to be triggered.
           this.alfPublish(this.requestFinishedTopic, {});
       },

       /**
        * Extends [loadData]{@link module:alfresco/documentlibrary/AlfSearchList#loadData} to store the rootNodeRef.
        * @instance
        */
       loadData: function alfresco_pickers_DocumentListPicker__loadData() {
           if (!this.requestInProgress)
           {
               this.showLoadingMessage();

               // Clear the previous data only when not configured to use infinite scroll...
               if (!this.useInfiniteScroll)
               {
                   this.clearViews();
               }

               var payload;
               if (this.loadDataPublishPayload)
               {
                   payload = lang.clone(this.loadDataPublishPayload);
               }
               else
               {
                   payload = {};
               }

               payload.alfResponseTopic = this.pubSubScope + this.loadDataPublishTopic;
               this.updateLoadDataPayload(payload);
               this.alfPublish(this.loadDataPublishTopic, payload, true);
           }
           else
           {
               // Let the user know that we're still waiting on the last data load?
               this.alfLog("warn", "Waiting for previous data load request to complete", this);
           }
       },

       /**
        * Extends the [inherited function]{@link module:alfresco/lists/AlfSortablePaginatedList#updateLoadDataPayload} to
        * add the additional document library related data.
        *
        * @instance
        * @param {object} payload The payload object to update
        */
       updateLoadDataPayload: function alfresco_lists_AlfSortablePaginatedList__updateLoadDataPayload(payload) {
           payload.searchTerm = this.searchTerm;
           payload.authorityType = this.authorityType;
           console.log("AuthorityListPicker (254): "+this.searchTerm);
       },

      /**
       * The default widgets for the picker. This can be overridden at instantiation based on what is required to be
       * displayed in the picker.
       *
       * @instance
       * @type {object}
       */
      widgets:[]
   });
});