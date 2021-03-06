/**
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * This mixin provides functions that allow files to be uploaded by dragging and dropping them
 * onto the widget. It also provides functions that control highlighting the widget when 
 * files are dragged over the widget.
 *
 * @module openesdh/pages/case/widgets/_DocumentGridUploadMixin
 * @basedOn module:alfresco/documentlibrary/_AlfDndDocumentUploadMixin
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/PathUtils",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/mouse",
        "dojo/on",
        "dijit/registry",
        "dojo/dom-class",
        "dojo/dom",
        "dojo/_base/window"], 
        function(declare, AlfCore, PathUtils, lang, array, mouse, on, registry, domClass, dom, win) {
   
   return declare([AlfCore, PathUtils], {

       /**
        * a payload topic to specify for the DnD topic to publish to.
        */
       dndPublishTopic: null,

      /**
       * Indicates whether drag and drop is enabled. 
       * 
       * @instance
       * @type {boolean} 
       * @default false
       */
      dndUploadEnabled: false,
      
      /**
       * Indicates whether or not the browser is capable of drag and drop file upload. This is set in the constructor.
       * 
       * @instance
       * @type {boolean} 
       * @default false
       */
      dndUploadCapable: false, 
    
      /**
       * Determines whether or not the browser supports drag and drop file upload.
       * 
       * @instance
       */
      constructor: function oe_caseLibrary__DndDocumentUploadMixin__constructor() {
         this.dndUploadCapable = ('draggable' in document.createElement('span'));
      },
      
      /**
       * Removes HTML5 drag and drop listeners from the supplied DOM node
       *
       * @instance
       * @param {object} domNode The DOM node to remove drag and drop capabilities from
       */
      removeUploadDragAndDrop: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__removeUploadDragAndDrop(domNode) {
         this.alfLog("log", "Removing drag and drop upload handlers");
         
         // Clean up any previously created event handlers...
         if (this.dndUploadEventHandlers != null)
         {
            array.forEach(this.dndUploadEventHandlers, function(handle){ handle.remove(); });
         }
         this.dndUploadEventHandlers = [];
      },

      /**
       * Keeps track of the DOM node that the drag-and-drop events are listened on.
       * 
       * @instance
       * @type {object}
       * @default null
       */
      dragAndDropNode: null,
      
      /**
       * @instance
       * @type {object[]}
       * @default null
       */
      dndUploadEventHandlers: null,
      
      /**
       * Adds subscriptions to topics providing information on the changes to the current node being represented. This 
       * has been primarily added to support widgets that change the displayed view.
       * 
       * @instance
       */
      subscribeToCurrentNodeChanges: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__subscribeToCurrentNodeChanges(domNode) {
         if (this.dndUploadCapable)
         {
            // Handle updates to the metadata (this is required in order for the view to know what
            // root Node it represents is)
            this.dragAndDropNode = domNode;
            //this.alfSubscribe(this.metadataChangeTopic, lang.hitch(this, this.onCurrentNodeChange));
         }
      },

      /**
       * Handles changes to the the current node that is represented by the widget that mixes in this module. For example
       * when the path that a view is displaying changes.
       * 
       * @instance
       * @param {object} payload The published payload
       */
      onCurrentNodeChange: function alfresco_documentlibrary___AlfDndDocumentUploadMixin__onCurrentNodeChange(payload) {
         if (payload && payload.node)
         {
            this.alfLog("log", "Updating current nodeRef to: ", payload.node);
            this._currentNode = payload.node;

            // Check that the current user can create children on the current node to determine
            // whether or not DND upload should be supported...
            var canCreateChildren = lang.getObject("node.parent.permissions.user.CreateChildren", false, payload);
            if (canCreateChildren === true)
            {
               this.addUploadDragAndDrop(this.dragAndDropNode);
            }
            else
            {
               this.removeUploadDragAndDrop(this.dragAndDropNode);
            }
         }
         else
         {
            this.alfLog("error", "A request was made to update the current NodeRef, but no 'node' property was provided in the payload: ", payload);
         }
      },

      /**
       * Handles changes the current filter. If the filter isn't path based then drag and drop uploading is disabled.
       * 
       * @instance
       * @param {object} payload The published payload
       */
      onFilterChange: function alfresco_documentlibrary___AlfDndDocumentUploadMixin__onFilterChange(payload) {
         var path = lang.getObject("path", false, payload);
         if (path == null)
         {
            this.removeUploadDragAndDrop(this.dragAndDropNode);
         }
         else
         {
            this.addUploadDragAndDrop(this.dragAndDropNode);
         }
      },


      /**
       * Adds HTML5 drag and drop listeners to the supplied DOM node
       *
       * @instance
       * @param {object} domNode The DOM node to add drag and drop listeners to
       */
      addUploadDragAndDrop: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__addUploadDragAndDrop(domNode) {
         if (this.dndUploadCapable)
         {
            console.log("(_DocumentGridUploadMixin#addUploadDragAndDrop) Adding DND upload capabilities"+ this);
            try
            {
               // Add listeners to the HTML5 drag and drop events
               this.dndUploadEnabled = true;
               this.dragAndDropNode = domNode;
               
               // Clean up any previously created event handlers...
               if (this.dndUploadEventHandlers != null)
               {
                  array.forEach(this.dndUploadEventHandlers, function(handle){ handle.remove(); });
               }
               this.dndUploadEventHandlers = [];
               
               // Add listeners for the mouse entering (these handlers allow us to normalise browser events that
               // are "broken" due to firing across all child nodes...
               this.dndUploadEventHandlers.push(on(win.body(), "dragenter", lang.hitch(this, "onDndUploadDragEnter")));
               this.dndUploadEventHandlers.push(on(this.dragAndDropNode, "dragover", lang.hitch(this, "onDndUploadDragOver")));
               this.dndUploadEventHandlers.push(on(this.dragAndDropNode, "drop", lang.hitch(this, "onDndUploadDrop")));
               this.alfLog("log", "Handlers: ", this.dndUploadEventHandlers);
            }
            catch(exception)
            {
               this.alfLog("error", "The following exception occurred adding Drag and Drop event handlers: ", exception);
            }
         }
         else
         {
            this.alfLog("log", "Cannot add DND upload capabilities because the browser does not have the required capabilities", this);
         }
      },

      /**
       * Fired when an object starts getting dragged. The event is swallowed because we only want to 
       * allow drag and drop events that begin outside the browser window (e.g. for files). This prevents
       * users attempting to drag and drop the document and folder images as if they could re-arrange
       * the document lib structure.
       *
       * @instance
       * @param {object} e HTML5 drag and drop event
       */
      swallowDragStart: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__swallowDragStart(e) {
         e.stopPropagation();
         e.preventDefault();
      },  
      
      /**
       * Fired when an object is dragged onto any node in the document body (unless the node has
       * been explicitly overridden to invoke another function). Swallows the event.
       *
       * @instance
       * @param {object} e HTML5 drag and drop event
       * 
       */
      swallowDragEnter: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__swallowDragEnter(e) {
         e.stopPropagation();
         e.preventDefault();
      },

      /**
       * Fired when an object is dragged over any node in the document body (unless the node has
       * been explicitly overridden to invoke another function). Updates the drag behaviour to
       * indicate that drops are not allowed and then swallows the event.
       *
       * @instance
       * @param {object} e HTML5 drag and drop event
       */
      swallowDragOver: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__swallowDragOver(e) {
         e.dataTransfer.dropEffect = "none";
         e.stopPropagation();
         e.preventDefault();
      },

      /**
       * Fired when an object is dropped onto any node in the document body (unless the node has
       * been explicitly overridden to invoke another function). Swallows the event to prevent
       * default browser behaviour (i.e. attempting to open the file).
       *
       * @instance
       * @param e {object} HTML5 drag and drop event
       */
      swallowDrop: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__swallowDrop(e) {
         this.alfLog("log", "Swallowing drop");
         e.stopPropagation();
         e.preventDefault();
      },

      /**
       * 
       *
       * @instance
       * @param {object} e HTML5 drag and drop event
       */
      onDndUploadDragEnter: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__onDndUploadDragEnter(e) {
         if (dom.isDescendant(e.target, this.dragAndDropNode) &&
             this.checkApplicable(e.target, "onDndUploadDragEnter"))
         {
            this.alfLog("log", "Adding DND highlight", this);
            this.addDndHighlight();
         }
         else
         {
            this.alfLog("log", "Removing DND highlight", this);
            this.removeDndHighlight();
         }
      },
      
      /**
       * It's important that the drag over event is handled and that "preventDefault" is called on it. If this is 
       * not done then the "drop" event will not be processed.
       *
       * @instance
       * @param {object} e HTML5 drag and drop event
       */
      onDndUploadDragOver: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__onDndUploadDragOver(e) {
         // Firefox 3.6 set effectAllowed = "move" for files, however the "copy" effect is more accurate for uploads
//         e.dataTransfer.dropEffect = Math.floor(YAHOO.env.ua.gecko) === 1 ? "move" : "copy";
         e.stopPropagation();
         e.preventDefault();
      },
      
      /**
       * This should be overridden to add highlighting when an item is dragged over the target.
       * 
       * @instance
       */
      addDndHighlight: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__addDragEnterHighlight() {
         if (this.domNode != null)
         {
            domClass.add(this.domNode, "dndHighlight");
         }
      },
      
      /**
       * This should be overridden to remove highlighting when an item is dragged out of the target
       * 
       * @instance
       */
      removeDndHighlight: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__addDragEnterHighlight() {
         if (this.domNode != null)
         {
            domClass.remove(this.domNode, "dndHighlight");
         }
      },
      
      /**
       * This function is used to check that the event to be handled relates directly to the current widget. This check is needed
       * because it is possible that a widget that handles drag and drop could be a child of another widget that handles drag and 
       * drop.
       * 
       * It returns true if the supplied DOM node belongs to the current widget (e.g. "this") and that the widget has the same
       * function. This isn't a perfect solution as there is a possibility that another widget could have an identical function 
       * name but this should be unlikely. It would have been preferable to use the "isInstanceOf" function, but that would require
       * a reference to the class that this function is being declared as part of!
       * 
       * As long as the function stops the event then this should not be necessary.
       * 
       * @instance
       * @param {object} domNode The DOM node that the event has occurred on
       * @param {string} currentFunctionName The name of the function being processed
       * @returns true if the current function should be executed
       */
      checkApplicable: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__checkApplicable(domNode, currentFunctionName) {
         var applicable = false;
         var widget = registry.getEnclosingWidget(domNode);
         if (widget == null)
         {
            // Something odd has happened. This should never really occur since in order for this function to be
            // called a widget must have DnD capabilities added!
            this.alfLog("log", "No widget found - unexpected behaviour: ", this);
         }
         else if (widget != this && 
                  typeof widget[currentFunctionName] === "function" &&
                  widget.dndUploadEnabled != null && 
                  widget.dndUploadEnabled === true)
         {
            // The event relates to a different widget
            this.alfLog("log", "This event does NOT relate to me: ", this);
         }
         else
         {
            // The event relates to the current instance
            this.alfLog("log", "This event relates to me: ", this);
            applicable = true;
         }
         return applicable;
      },
      
      /**
       * Fired when an object is dropped onto the DocumentList DOM element.
       * Checks that files are present for upload, determines the target (either the current document list or
       * a specific folder rendered in the document list and then calls on the DNDUpload singleton component
       * to perform the upload.
       *
       * @instance
       * @param {object} evt HTML5 drag and drop event
       */
      onDndUploadDrop: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__onDndUploadDrop(evt) {
         try
         {
            // Only perform a file upload if the user has *actually* dropped some files!
            this.alfLog("log", "Upload drop detected", evt);
            if (evt.dataTransfer.files !== undefined && evt.dataTransfer.files !== null && evt.dataTransfer.files.length > 0)
            {
               this.removeDndHighlight();
               this.alfPublish(this.dndPublishTopic, {files: evt.dataTransfer.files});
            }
            else
            {
               this.alfLog("error", "A drop event was detected, but no files were present for upload: ", evt.dataTransfer);
            }
         }
         catch(exception)
         {
            this.alfLog("error", "The following error occurred when files were dropped onto the Document List: ", exception);
         }
         // Remove the drag highlight...
         this.removeDndHighlight();
         evt.stopPropagation();
         evt.preventDefault();
      }
   });
});