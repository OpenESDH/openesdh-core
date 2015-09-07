<import resource="classpath:/alfresco/extension/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">//
    function main()
    {
        /**
         * Original lines are left here for awareness:
         * 3 - repo: (args.repo !== null) ? (args.repo == "true") : false,
         */
        var params = {
            siteId: args.site,
            containerId: args.container,
            repo: true,
            term: args.term,
            tag: args.tag,
            query: args.query,
            rootNode: args.rootNode,
            sort: args.sort,
            maxResults: (args.maxResults !== null) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS,
            pageSize: (args.pageSize !== null) ? parseInt(args.pageSize, 10) : DEFAULT_PAGE_SIZE,
            startIndex: (args.startIndex !== null) ? parseInt(args.startIndex, 10) : 0,
            facetFields: args.facetFields,
            filters: args.filters,
            spell: (args.spellcheck !== null) ? (args.spellcheck == "true") : false
        };
        model.data = getSearchResults(params);
    };

main();