/**
 * Single authority select.
 */
define(["dojo/_base/declare",
        "dijit/form/FilteringSelect",
        "alfresco/core/Core",
        "dojo/store/JsonRest",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/string",
        "dojo/dom-class",],
        function(declare, FilteringSelect, AlfCore, JsonRest, lang, array, stringUtil, domClass) {
   
   return declare([FilteringSelect,  AlfCore], {
      postCreate: function () {
         this.inherited(arguments);
         this.searchAttr = 'authority';
         this.labelAttr = 'displayName';
      },
      
      postMixInProperties: function () {
          if (this.store == null) {
              this.store = new JsonRest({target: this.getRestUrl()});
          }
          this.inherited(arguments);
          this.store.idProperty = 'authority';
      },

      getRestUrl: function () {
         return Alfresco.constants.PROXY_URI + "api/openesdh/authorities/";
      }
   });
});