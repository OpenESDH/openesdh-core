define([
    "alfresco/forms/controls/Picker",
    "dojo/_base/declare"],
    function(Picker, declare) {

   return declare([Picker], {

      configForPicker: {
         generatePubSubScope: true,
         widgetsForPickedItems: [
            {
               name: "alfresco/pickers/PickedItems",
               assignTo: "pickedItemsWidget"
            }
         ],
         widgetsForRootPicker: [
            {
               name: "alfresco/menus/AlfVerticalMenuBar",
               config: {
                  widgets: [
                     {
                        name: "alfresco/menus/AlfMenuBarItem",
                        config: {
                           label: "Cases",
                           publishTopic: "ALF_ADD_PICKER",
                           publishPayload: {
                              currentPickerDepth: 0,
                              picker: [
                                 {
                                    name: "alfresco/pickers/ContainerListPicker",
                                    config: {
                                       nodeRef: "alfresco://company/home",
                                       filter: {
                                          path: "/openesdh_cases"
                                       }
                                    }
                                 }
                              ]
                           }
                        }
                     }
                  ]
               }
            }
         ]
      }
   });
});