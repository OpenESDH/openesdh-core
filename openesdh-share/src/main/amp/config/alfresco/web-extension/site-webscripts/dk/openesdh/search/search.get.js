<import resource="classpath:/alfresco/web-extension/utils/case.js">//</import>

function getListFromConfigElement(configElement, type) {
    var String = java.lang.String;
    var configSection = configElement;
    if (typeof(type) !== "undefined") {
        configSection = configElement.getChild(type);
    }
    var list = [];
    if (configSection != null) {
        var iter = configSection.getChildren().iterator();
        while(iter.hasNext()) {
            var column = iter.next();
            var columnId = column.getAttribute(new String('id'));
            list.push(columnId);
        }
    }
    return list;
}

function getFilterList(configElement) {
    var String = java.lang.String;
    var list = [];
    var iter = configElement.getChildren().iterator();
    while(iter.hasNext()) {
        var filter = iter.next();
        var id = filter.getAttribute(new String('id'));
        var control = filter.getAttribute(new String('control'));
        list.push(id);
    }
    return list;
}

function mergeArrays(map, key, array) {
    if (key in map) {
        map[key] = map[key].concat(array);
    } else {
        map[key] = array;
    }
}

function getSearchConfig() {
    var String = java.lang.String;

    var searches = null,
        searchesMain = config.scoped["SearchModel"];
    if (searchesMain != null) {
        searches = searchesMain["searches"];
    }
    var searchTypes = {};
    var iter = searches.getChildren().iterator();
    while (iter.hasNext()) {
        var search = {};
        var searchConfig = iter.next();
        var type = searchConfig.getAttribute(new String('type'));
        if (type in searchTypes) {
            search = searchTypes[type];
        }
        mergeArrays(search, 'availableColumns',
            getListFromConfigElement(searchConfig, "column-availability"));
        mergeArrays(search, 'visibleColumns',
            getListFromConfigElement(searchConfig, "column-visibility"));
        var filtersAvailability = searchConfig.getChild(new String("filters-availability"));
        if (filtersAvailability != null) {
            mergeArrays(search, 'availableFilters', getFilterList(filtersAvailability));
        }
        logger.warn(type + ": " + jsonUtils.toJSONString(search));
        searchTypes[type] = search;
    }

    var defaultControlsConfig = searchesMain["default-controls"];
    iter = defaultControlsConfig.getChildren().iterator();
    var defaultControls = {};
    while (iter.hasNext()) {
        var defaultControl = iter.next();
        var type = defaultControl.getAttribute(new String('name'));
        var control = defaultControl.getAttribute(new String('control'));
        defaultControls[type] = control;
    }

    return {"searches": searchTypes, "defaultControls": defaultControls};
}

function getTypeModel(type, subclasses) {
    var connector = remote.connect("alfresco");
//    var model = connector.get("/api/openesdh/model?type=" + encodeURIComponent(type));
    var fetchSubclasses = false;
    if (typeof subclasses !== 'undefined') {
        fetchSubclasses = subclasses;
    }
    var model = connector.get("/api/classes/" + type.replace(":", "_") + (fetchSubclasses ? "/subclasses" : ""));
    model = eval('(' + model + ')');
    return model;
}

function getSearchModel(baseType) {
    var typeModels = getTypeModel(baseType, true);
    var model = {};
    model.types = {};
    model.properties = {};
    typeModels.forEach(function (typeModel) {
        var type = typeModel.name;
        model.types[type] = {
            "name": typeModel.name,
            "title": typeModel.title
        };
        if ("defaultAspects" in typeModel) {
            // Gather properties from default aspects
            var defaultAspects = Object.keys(typeModel.defaultAspects);
            defaultAspects.push("cm:titled");
            defaultAspects.forEach(function (aspect) {
                var aspectModel = getTypeModel(aspect);
                for (property in aspectModel.properties) {
                    if (!aspectModel.properties.hasOwnProperty(property)) {
                        continue;
                    }
                    if (property in model.properties) {
                        // TODO: Merge constraints?
                    } else {
                        model.properties[property] = aspectModel.properties[property];
                    }
                }
            });
        }
        for (property in typeModel.properties) {
            if (!typeModel.properties.hasOwnProperty(property)) {
                continue;
            }
            if (property in model.properties) {
                // TODO: Merge constraints
            } else {
                model.properties[property] = typeModel.properties[property];
            }
        }
        for (association in typeModel.associations) {
            if (!typeModel.associations.hasOwnProperty(association)) {
                continue;
            }
            if (!(association in model.properties)) {
                var metadata = typeModel.associations[association];
                metadata.dataType = metadata.target.class;
                metadata.multiValued = metadata.target.many;
                model.properties[association] = metadata;
            }
        }
    });
    return model;
}

function getSearchDefinition(type) {
    var defaultWidget = "CaseFilterTextWidget";

    var model = getSearchModel(type);
    var config = getSearchConfig();

    // Assign default controls
    for (var key in model.properties) {
        if (!model.properties.hasOwnProperty(key)) {
            continue;
        }
        var property = model.properties[key];
        if (property.dataType in config.defaultControls) {
            property.control = config.defaultControls[property.dataType];
        } else {
            property.control = defaultWidget;
        }
    }

    var availableFilters = Object.keys(model.properties);
    // Remove ALL column from default columns
    var visibleColumns = Object.keys(model.properties).filter(function (item) {
        return item !== "ALL";
    });
    var availableColumns = visibleColumns;

    // TODO: Make customizable, or at least localize
    model.properties["TYPE"] = {
        "name": "TYPE",
        "title": "Type",
        // TODO: Make select
        "dataType": "d:text"
    };
    model.properties["ALL"] = {
        "name": "ALL",
        "title": "Søg",
        "dataType": "d:text"
    };

    // TODO: Load from config
    var operatorSets = {
        'equality': [
            {
                name: 'er',
                value: '='
            },
            {
                name: 'er ikke',
                value: '!='
            }
        ]
    };

    // Apply overrides from search configuration
    // For example, which filters should be available
    // Which columns should be visible/available...
    var searchConfig = config["searches"][type];
    if (typeof searchConfig.availableFilters !== 'undefined' &&
        searchConfig.availableFilters.length > 0) {
        availableFilters = searchConfig.availableFilters;
    }
    if (typeof searchConfig.visibleColumns !== 'undefined' &&
        searchConfig.visibleColumns.length > 0) {
        visibleColumns = searchConfig.visibleColumns;
    }
    if (typeof searchConfig.availableColumns !== 'undefined' &&
        searchConfig.availableColumns.length > 0) {
        availableColumns = searchConfig.availableColumns;
    }

    return {
        "model": model,
        "availableFilters": availableFilters,
        "visibleColumns": visibleColumns,
        "availableColumns": availableColumns,
        "operatorSets": operatorSets
    };
}

// TODO: Make this a parameter
var baseType = "case:base";

var searchDefinition = getSearchDefinition(baseType);

var operatorSets = searchDefinition["operatorSets"];
var searchModel = searchDefinition["model"];

// TODO: Make configurable?
// We don't want the base type as one of the types available.
delete searchModel.types[baseType];

logger.warn(jsonUtils.toJSONString(searchDefinition));

model.jsonModel = {
    services: [
        {
            name: "openesdh/search/CaseNavigationService"
        }
    ],
    widgets: [
        {
            id: "SET_PAGE_TITLE",
            name: "alfresco/header/SetTitle",
            config: {
                title: msg.get("openesdh.page.search.label")
            }
        },
        {
            id: "SHARE_VERTICAL_LAYOUT",
            name: "alfresco/layout/VerticalWidgets",
            config: {
                widgets: [
                    {
                        name: 'alfresco/layout/VerticalWidgets',
                        config: {
                            widgets: [
                                {
                                    name: "openesdh/search/CaseFilterPane",
                                    config: {
                                        baseType: baseType,
                                        types: searchModel.types,
                                        properties: searchModel.properties,
                                        availableFilters: searchDefinition.availableFilters,
                                        operatorSets: operatorSets
                                    }
                                },
                                {
                                    name: "openesdh/search/CaseGrid",
                                    config: {
                                        baseType: baseType,
                                        types: searchModel.types,
                                        properties: searchModel.properties,
                                        visibleColumns: searchDefinition.visibleColumns,
                                        availableColumns: searchDefinition.availableColumns
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    ]

};


