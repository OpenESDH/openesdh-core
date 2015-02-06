define(["dojo/_base/declare",
        "dojo/_base/lang",
        "alfresco/core/PathUtils",
        "alfresco/core/NodeUtils",
        "service/constants/Default",
        "alfresco/services/DocumentService"],
    function (declare, lang, PathUtils, NodeUtils, AlfConstants, DocService) {

        return declare([DocService], {
            /**
             * We override the service method to hook it to our share webscript path instead of the ootb defaults
             * The reason for this is that we customise the returned webscript to include additional objects
             * (details/main document) which in turn are used to populate other widgets in the view.
             * @param payload
             */
            onRetrieveMainDocumentRequest: function openesdh_services_DocumentService__onRetrieveMainDocumentRequest(payload) {
                var targetNode = "alfresco://company/home",
                    targetNodeUri = "alfresco/company/home";
                if (payload.nodeRef != null && payload.nodeRef !== "") {
                    var nodeRef = NodeUtils.processNodeRef(payload.nodeRef);
                    targetNode = payload.nodeRef;
                    targetNodeUri = nodeRef.uri;
                }

                // Construct the URI for the request...
                var uriPart = "{type}/node/" + targetNodeUri;
                if (payload.filter != null && payload.filter.path != null) {
                    // If a path has been provided in the filter then it is necessary to perform some special
                    // encoding. We need to ensure that the data is URI encoded, but we want to preserve the
                    // forward slashes. We also need to "double encode" all % characters because FireFox has
                    // a nasty habit of decoding them *before* they've actually been posted back... this
                    // guarantees that the user will be able to bookmark valid URLs...
                    var encodedPath = encodeURIComponent(payload.filter.path).replace(/%2F/g, "/").replace(/%25/g, "%2525");
                    uriPart += this.combinePaths("/", encodedPath);
                }

                // Unbelievably it is necessary to remove any trailing forward slashes otherwise the location
                // data set for each item will duplicate first element in the path !!!
                if (uriPart.lastIndexOf("/") === uriPart.length - 1) {
                    uriPart = uriPart.substring(0, uriPart.length - 1);
                }

                // Build the URI stem
                var params = lang.replace(uriPart, {
                    type: encodeURIComponent("main"),
                    site: encodeURIComponent(payload.site),
                    container: encodeURIComponent(payload.container)
                });

                params +="?filter=main";

                if (payload.pageSize != null && payload.page != null) {
                    params += "&size=" + payload.pageSize + "&pos=" + payload.page;
                }

                // Sort parameters
                params += "&sortAsc=" + payload.sortAscending + "&sortField=" + encodeURIComponent(payload.sortField);
                if (payload.site == null) {
                    if (payload.libraryRoot != null) {
                        params += "&libraryRoot=" + encodeURIComponent(payload.libraryRoot);
                    }
                    else {
                        // Repository mode (don't resolve Site-based folders)
                        params += "&libraryRoot=" + encodeURIComponent(targetNode);
                    }
                }

                // View mode and No-cache
                params += "&view=browse&noCache=" + new Date().getTime();

                var alfTopic = (payload.alfResponseTopic != null) ? payload.alfResponseTopic : "ALF_RETRIEVE_DOCUMENTS_REQUEST";
                var url = AlfConstants.URL_SERVICECONTEXT + "components/caselibrary/data/doclist/" + params;
                var config = {
                    alfTopic: alfTopic,
                    url: url,
                    method: "GET",
                    callbackScope: this
                };
                this.serviceXhr(config);
            },

            onRetrieveDocumentsRequest: function alfresco_services_DocumentService__onRetrieveDocumentsRequest(payload) {

                var targetNode = "alfresco://company/home",
                    targetNodeUri = "alfresco/company/home";
                if (payload.nodeRef != null && payload.nodeRef !== "") {
                    var nodeRef = NodeUtils.processNodeRef(payload.nodeRef);
                    targetNode = payload.nodeRef;
                    targetNodeUri = nodeRef.uri;
                }

                // Construct the URI for the request...
                var uriPart = "{type}/node/" + targetNodeUri;
                if (payload.filter != null && payload.filter.path != null) {
                    // If a path has been provided in the filter then it is necessary to perform some special
                    // encoding. We need to ensure that the data is URI encoded, but we want to preserve the
                    // forward slashes. We also need to "double encode" all % characters because FireFox has
                    // a nasty habit of decoding them *before* they've actually been posted back... this
                    // guarantees that the user will be able to bookmark valid URLs...
                    var encodedPath = encodeURIComponent(payload.filter.path).replace(/%2F/g, "/").replace(/%25/g, "%2525");
                    uriPart += this.combinePaths("/", encodedPath);
                }

                // Unbelievably it is necessary to remove any trailing forward slashes otherwise the location
                // data set for each item will duplicate first element in the path !!!
                if (uriPart.lastIndexOf("/") === uriPart.length - 1) {
                    uriPart = uriPart.substring(0, uriPart.length - 1);
                }

                // Build the URI stem
                var params = lang.replace(uriPart, {
                    type: encodeURIComponent(payload.type),
                    site: encodeURIComponent(payload.site),
                    container: encodeURIComponent(payload.container)
                });

                if (payload.filter) {
                    if (payload.filter.filter != null) {
                        params += "?filter=" + payload.filter.filter;
                    }
                    else if (payload.filter.tag != null) {
                        params += "?filter=tag&filterData=" + payload.filter.tag;
                    }
                    else if (payload.filter.category != null) {
                        params += "?filter=category&filterData=" + payload.filter.category;
                    }
                    else {
                        params += "?filter=path";
                    }
                }

                if (payload.pageSize != null && payload.page != null) {
                    params += "&size=" + payload.pageSize + "&pos=" + payload.page;
                }

                // Sort parameters
                params += "&sortAsc=" + payload.sortAscending + "&sortField=" + encodeURIComponent(payload.sortField);
                if (payload.site == null) {
                    if (payload.libraryRoot != null) {
                        params += "&libraryRoot=" + encodeURIComponent(payload.libraryRoot);
                    }
                    else {
                        // Repository mode (don't resolve Site-based folders)
                        params += "&libraryRoot=" + encodeURIComponent(targetNode);
                    }
                }

                // View mode and No-cache
                params += "&view=browse&noCache=" + new Date().getTime();

                var alfTopic = (payload.alfResponseTopic != null) ? payload.alfResponseTopic : "ALF_RETRIEVE_DOCUMENTS_REQUEST";
                var url = AlfConstants.URL_SERVICECONTEXT + "components/caselibrary/data/doclist/" + params;
                var config = {
                    alfTopic: alfTopic,
                    url: url,
                    method: "GET",
                    callbackScope: this
                };
                this.serviceXhr(config);
            }

        });
    });