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
            var columnName = column.getAttribute(new String('name'));
            list.push(columnName);
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
        var name = filter.getAttribute(new String('name'));
        var control = filter.getAttribute(new String('control'));
        var title = filter.getAttribute(new String('title'));
        list.push({"name": name, "control": control, "title": title});
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
        var columnsConfig = searchConfig.getChild("columns");
        if (columnsConfig != null) {
            mergeArrays(search, 'availableColumns',
                getListFromConfigElement(columnsConfig, "available"));
            mergeArrays(search, 'visibleColumns',
                getListFromConfigElement(columnsConfig, "visible"));
        }
        var filtersConfig = searchConfig.getChild("filters");
        if (filtersConfig != null) {
            mergeArrays(search, 'filters', getFilterList(filtersConfig));
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

function getTypeModel(type) {
    var connector = remote.connect("alfresco");
//    var model = connector.get("/api/openesdh/model?type=" + encodeURIComponent(type));
    var model = connector.get("/api/classes/" + type.replace(":", "_") + "/subclasses");
    model = eval('(' + model + ')');
    return model;
}

function getSearchModel(baseType) {
    var typeModels = getTypeModel(baseType);
    var model = {};
    model.types = {};
    model.properties = {};
    typeModels.forEach(function (typeModel) {
        var type = typeModel.name;
        model.types[type] = {
            "name": typeModel.name,
            "title": typeModel.title
        };
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
    });
    return model;
}

function getSearchDefinition(type) {
    var defaultWidget = "CaseFilterTextWidget";

    var model = getSearchModel(type);
    var config = getSearchConfig();

    // TODO: Make customizable, or at least localize
    model.properties["TYPE"] = {
        "name": "TYPE",
        "title": "Typ",
        // TODO: Make select
        "dataType": "d:text"
    };
    model.properties["ALL"] = {
        "name": "ALL",
        "title": "Søg",
        "dataType": "d:text"
    };

    // Assign default controls
    for (key in model.properties) {
        var property = model.properties[key]
        if (property.dataType in config.defaultControls) {
            property.control = config.defaultControls[property.dataType];
        } else {
            property.control = defaultWidget;
        }
    }

    var visibleFilters = Object.keys(model.properties);
    var availableFilters = Object.keys(model.properties);
    // Remove ALL column from default columns
    var defaultColumns = Object.keys(model.properties).filter(function (item) {
        return item !== "ALL";
    });
    var availableColumns = Object.keys(model.properties);

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

    // TODO: Apply overrides from search configuration
    // For example, which filters should be visible, which available,
    // Which columns should be default...
//    var typeConfig = config["searches"][type];

    return {
        "model": model,
        "visibleFilters": visibleFilters,
        "availableFilters": availableFilters,
        "defaultColumns": defaultColumns,
        "availableColumns": availableColumns,
        "operatorSets": operatorSets
    };
}

// TODO: Make this a parameter
var baseType = "case:base";

var searchDefinition = getSearchDefinition(baseType);

var availableFilters = searchDefinition["availableFilters"];
var visibleFilters = searchDefinition["visibleFilters"];
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
                                        visibleFilters: searchDefinition.visibleFilters,
                                        operatorSets: operatorSets
                                    }
                                },
                                {
                                    name: "openesdh/search/CaseGrid",
                                    config: {
                                        baseType: baseType,
                                        types: searchModel.types,
                                        properties: searchModel.properties,
                                        defaultColumns: searchDefinition.defaultColumns,
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


