define(["dojo/_base/declare",
        "alfresco/core/Core",
        "dojo/_base/lang",
        "dojo/request",
        "dojo/json",
        "alfresco/core/CoreXhr",
        "service/constants/Default",
        "dojo/_base/array",
        "dojo/dom-construct"],
        function(declare, Core, lang, request, JSON, CoreXhr, AlfConstants, array, domConstruct) {

	return declare([Core, CoreXhr], {
	    
	    loadItemsTopic: null,

        constructor: function PaginationService__constructor(args) {
          lang.mixin(this, args);
          this.alfSubscribe(this.loadItemsTopic, lang.hitch(this, this.getItems));
        },

        getItems: function(payload) {
          var _this = this;
    	  var alfTopic = (payload.alfResponseTopic != null) ? payload.alfResponseTopic : "";
    	  	
    	  var pageNo = (payload.page != null) ? payload.page : 1;
    	  var pageSize = (payload.pageSize != null) ? payload.pageSize: 1;
    	  var skipCount = (pageNo - 1) * pageSize;
    	  var endIndex = pageNo * pageSize;
    	  
    	  var url = AlfConstants.PROXY_URI + payload.url;
    	  var promise = request.get(url, {
    	      method: "GET",
              headers: { "X-Range": "items="+ skipCount + "-" + endIndex }
    	  });
    	  
    	  promise.response.then(function(response){
    	      _this.onSuccess(response);
    	  });
    	},
    	
    	onSuccess: function(response) {
    	    var items = JSON.parse(this.cleanupJSONResponse(response.data));
    	    var rangeHeader = response.getHeader("Content-Range");
            var matches = /items (\d+)-(\d+)\/(\d+)/.exec(rangeHeader);
    	    this.alfPublish(this.loadItemsTopic + "_SUCCESS", { response: {items: items, totalRecords: matches[3], startIndex: matches[1]}} );
    	}
    	
  });
});