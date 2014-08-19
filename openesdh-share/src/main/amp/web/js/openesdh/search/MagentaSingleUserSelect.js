/**
 * Fixes bugs in SingleUserSelect.
 * 
 * @extends alfresco/users/SingleUserSelect
 */
define(["dojo/_base/declare",
        "dijit/form/FilteringSelect", 
        "alfresco/core/Core",
        "dojo/store/JsonRest",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/string",
        "dojo/dom-class",
        "alfresco/users/SingleUserSelect"], 
        function(declare, FilteringSelect, AlfCore, JsonRest, lang, array, stringUtil, domClass, SingleUserSelect) {
   
   return declare([SingleUserSelect,  AlfCore], {
      postCreate: function () {
         this.inherited(arguments);
         this.searchAttr = 'filter';
      },
      
      postMixInProperties: function () {
          this.inherited(arguments);
          this.store.idProperty = 'userName';
      },
      
      /**
       * Returns the URL for the standard Alfresco people REST API.
       * 
       * @instance 
       */
      getRestUrl: function () {
         // Override Alfresco's function, adding a slash at the end of the URL
         // Otherwise, we get a 404 when trying to programmatically
         // set the value of this widget
         return Alfresco.constants.PROXY_URI + "api/people/";
      }
   });
});