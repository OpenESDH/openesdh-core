define(["dojo/_base/declare",
        "alfresco/core/Core",
        "dojo/_base/lang",
        "alfresco/core/CoreXhr",
        "service/constants/Default",
        "dojo/_base/array",
        "dojo/dom-construct"],
        function(declare, Core, lang, CoreXhr, AlfConstants, array, domConstruct) {

	return declare([Core, CoreXhr], {

    constructor: function tutorial_UserAndGroupService__constructor(args) {
    	
      lang.mixin(this, args);
      this.alfSubscribe("GET_HISTORY_ROWS", lang.hitch(this, this.getGroups));
    },

    getGroups: function(payload) {

    	  var alfTopic = (payload.alfResponseTopic != null) ? payload.alfResponseTopic : "";
    	  	
    	  var pageNo = (payload.page != null) ? payload.page : 1;
    	  var pageSize = (payload.pageSize != null) ? payload.pageSize: 25;
    	  var skipCount = (pageNo - 1) * pageSize;
    	
    	  this.serviceXhr({
    		  url: AlfConstants.PROXY_URI + payload.url,
    		  method: "GET",
    		  headers: { "X-Range": "items="+ skipCount + "-" + pageSize },
    	      alfTopic: alfTopic,
    	      successCallback: this.onSuccess
    	  });
    	},
    	
    	onSuccess: function(response, originalRequestConfig) {
               this.alfPublish("GET_HISTORY_ROWS_SUCCESS", { data: response } );
    	}
    	
  });
});