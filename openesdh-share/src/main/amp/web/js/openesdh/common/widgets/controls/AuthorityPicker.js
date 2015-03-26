/**
 * Based on alfresco/forms/controls/ContainerPicker
 *
 * @module openesdh/common/widgets/controls/AuthorityPicker.js
 * @extends module:alfresco/forms/controls/Picker
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "dojo/_base/lang",
        "alfresco/forms/controls/Picker",
        "openesdh/common/widgets/picker/PickerWithHeader"
    ],
        function(declare, lang, Picker, PickerWithHeader) {

   return declare([Picker], {

       authorityType: "cm:authority",

       /**
        * This should be overridden to define the widget model for rendering the picker that appears within the
        * dialog.
        *
        * @instance
        * @type {object}
        * @default []
        */
       configForPickedItems: {
           itemKey: "nodeRef",
           widgets: [{
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
                                           iconClass: "delete-16",
                                           publishTopic: "ALF_ITEM_REMOVED",
                                           publishPayloadType: "CURRENT_ITEM"
                                       }
                                   }
                               ]
                           }
                       }
                   ]
               }
           }]
       },

       /**
        * This should be overridden to define the widget model for rendering the picker that appears within the
        * dialog.
        *
        * @instance
        * @type {object}
        * @default []
        */
       configForPicker: {
           generatePubSubScope: true,
           widgetsForPickerHeader:[
               {
                   name: "alfresco/layout/HorizontalWidgets",
                   config: {
                       widgets: [
                           {
                               name: "openesdh/common/widgets/forms/SingleTextFieldForm",
                               config: {
                                   useHash: false,
                                   showOkButton: true,
                                   okButtonLabel: "Search", //this.message("button.label.search"),
                                   showCancelButton: false,
                                   okButtonPublishTopic: "OE_UPDATE_SEARCH_TERM",
                                   okButtonPublishGlobal: false,
                                   textBoxLabel: "Search",
                                   textFieldName: "searchTerm",
                                   okButtonIconClass: "alf-white-search-icon",
                                   okButtonClass: "call-to-action",
                                   textBoxIconClass: "alf-search-icon",
                                   textBoxRequirementConfig: {
                                       initialValue: false
                                   }
                               }
                           }
                       ]
                   }
               }
           ],
           widgetsForPickedItems: [
               {
                   name: "alfresco/pickers/PickedItems",
                   config:{
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
                                                           iconClass: "delete-16",
                                                           publishTopic: "ALF_ITEM_REMOVED",
                                                           publishPayloadType: "CURRENT_ITEM"
                                                       }
                                                   }
                                               ]
                                           }
                                       }
                                   ]
                               }
                           }
                       ]
                   },
                   assignTo: "pickedItemsWidget"
               }
           ],
           widgetsForRootPicker: [
               {
                   name: "alfresco/layout/HorizontalWidgets",
                   config: {
                       widgets: [
                           {
                               name: "openesdh/common/widgets/picker/AuthorityListPicker",
                               config: {
                                   authorityType: "{authorityType}"
                               }
                           }
                       ]
                   }
               }
           ]
       },


       /**
        * Updates the model to set a value of the currently selected items. It is necessary to do this because
        * the model cannot contain variable information (at least none that can be defined when the widget is
        * instantiated) so it is necessary to perform this before the [processWidgets]{@link module:alfresco/core/Core#processWidgets}
        * function is called.
        *
        * @instance
        * @param {object} value The value to set
        */
       setModelPickerConfig: function alfresco_forms_controls_Picker__setModelPickerConfig(value, widgetsForControl) {
           this.inherited(arguments);
           lang.setObject("0.config.widgets.1.config.publishPayload.widgetsContent.0.name", "openesdh/common/widgets/picker/PickerWithHeader", widgetsForControl);
           console.log("widgetsForControl: "+ widgetsForControl);
           this.processObject(["processInstanceTokens"], widgetsForControl);
       }

   });
});