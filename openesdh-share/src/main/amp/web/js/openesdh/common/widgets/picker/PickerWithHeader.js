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
 * <p>The default picker widget for use in [picker form controls]{@link module:alfresco/forms/controls/Picker} and can be
 * extended as necessary to customize the initial set of "root pickers" by overriding the [widgetsForRootPicker attribute]
 * {@link module:alfresco/pickers/Picker#widgetsForRootPicker}. The picked items display can also be customized by
 * overriding the [widgetsForPickedItems attribute]{@link module:alfresco/pickers/Picker#widgetsForPickedItems}.</p>
 *
 * @module alfresco/pickers/Picker
 * @extends dijit/_WidgetBase
 * @mixes dijit/_TemplatedMixin
 * @mixes module:alfresco/core/Core
 * @mixes module:alfresco/core/CoreWidgetProcessing
 * @author Dave Draper & David Webster
 */
define(["dojo/_base/declare",
        "alfresco/pickers/Picker",
        "dojo/text!./templates/PickerWithHeader.html",
        "dojo/_base/lang"],
        function(declare, Picker, template, lang) {

   return declare([Picker], {

      /**
       * An array of the CSS files to use with this widget.
       *
       * @instance
       * @type {object[]}
       * @default [{cssFile:"./css/Picker.css"}]
       */
      cssRequirements: [{cssFile:"./css/PickerWithHeader.css"}],

      /**
       * The HTML template to use for the widget.
       * @instance
       * @type {String}
       */
      templateString: template,

      /**
       *
       *
       * @instance
       */
      postCreate: function alfresco_pickers_Picker__postCreate() {
         this.inherited(arguments);

         if (this.widgetsForPickerHeader != null)
         {
            this.processWidgets(lang.clone(this.widgetsForPickerHeader), this.pickerHeaderControls);
         }
      }
   });
});