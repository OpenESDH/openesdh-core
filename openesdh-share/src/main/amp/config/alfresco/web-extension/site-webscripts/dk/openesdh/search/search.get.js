<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

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
        list.push(id);
    }
    return list;
}

function getActionList(configElement) {
    var String = java.lang.String;
    var list = [];
    var iter = configElement.getChildren().iterator();
    while(iter.hasNext()) {
        var action = iter.next();
        var obj = {
            id: action.getAttribute(new String('id')),
            label: action.getAttribute(new String('label')),
            href: action.getAttribute(new String('href'))
        };
        var key = action.getAttribute(new String('key'));
        var shift = action.getAttribute(new String('shift'));
        if (key) {
            obj.key = key;
        }
        if (shift) {
            obj.shift = shift;
        }
        list.push(obj);
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
        var actionsConfig = searchConfig.getChild(new String("actions"));
        if (actionsConfig != null) {
            mergeArrays(search, 'actions', getActionList(actionsConfig));
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
    var fetchSubclasses = false;
    if (typeof subclasses !== 'undefined') {
        fetchSubclasses = subclasses;
    }
    var model = connector.get("/api/classes/" + type.replace(":", "_") + (fetchSubclasses ? "/subclasses" : ""));
    return eval('(' + model + ')');
}

function getPropertyConstraints(type, property) {
    var connector = remote.connect("alfresco");
    var model = connector.get("/api/classes/" + type.replace(":", "_") + "/property/" + property.replace(":", "_"));
    model = eval('(' + model + ')');
    return model.constraints;
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
                    aspectModel.properties[property].constraints = getPropertyConstraints(aspect, property);
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
            typeModel.properties[property].constraints = getPropertyConstraints(type, property);
            if (property in model.properties) {
                // Merge constraints
                mergeArrays(model.properties[property], "constraints", typeModel.properties[property].constraints);
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
    var model = getSearchModel(type);
    var config = getSearchConfig();

    var defaultWidget = config.defaultControls["*"];
    if (!defaultWidget) {
        defaultWidget = "FilterTextWidget";
    }
    var defaultSelectWidget = "FilterSelectWidget";
    try{
        // Assign default controls
        for (var key in model.properties) {
            if (!model.properties.hasOwnProperty(key)) {
                continue;
            }
            var property = model.properties[key];
            var control = null;
            if ("constraints" in property && property.constraints.length > 0) {
                // Check for default constraint control
                property.constraints.(function (constraint) {
                    var check = constraint.type + ":" + property.dataType;
                    if (check in config.defaultControls) {
                        property.control = config.defaultControls[check];
                    } else {
                        check = constraint.type + ":*";
                        if (check in config.defaultControls) {
                            property.control = config.defaultControls[check];
                        }
                    }
                });
            }
            if (!property.control) {
            if (property.dataType in config.defaultControls) {
                property.control = config.defaultControls[property.dataType];
            } else {
                property.control = defaultWidget;
            }
        }
        }
    }
    catch(error){
        logger.warn(error.message+"\n\t\t Line => "+ error.lineNumber);
    }

    var availableFilters = Object.keys(model.properties);
    // Remove ALL column from default columns
    var visibleColumns = Object.keys(model.properties).filter(function (item) {
        return item !== "ALL";
    });
    var availableColumns = visibleColumns;

    // TODO: Make configurable?
    // We don't want the base type as one of the types available.
    delete model.types[baseType];

    var typeOptions = Object.keys(model.types).map(function (id, i) {
        return { label: model.types[id].title, value: id };
    });

    // TODO: Make customizable, or at least localize
    model.properties["TYPE"] = {
        "name": "TYPE",
        "title": "Type",
        "dataType": "d:text",
        "control": defaultSelectWidget,
        "options": typeOptions
    };
    model.properties["ALL"] = {
        "name": "ALL",
        "title": "Søg",
        "dataType": "d:text",
        "control": defaultWidget
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

    // Apply overrides from xsearch configuration
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

    var actions = [];
    if (typeof searchConfig.actions !== 'undefined' &&
        searchConfig.actions.length > 0) {
        actions = searchConfig.actions;
    }

    return {
        "model": model,
        "availableFilters": availableFilters,
        "visibleColumns": visibleColumns,
        "availableColumns": availableColumns,
        "operatorSets": operatorSets,
        "actions": actions
    };
}

// TODO: Make this a parameter
var baseType = "base:case";

var searchDefinition = getSearchDefinition(baseType);

var searchModel = searchDefinition["model"];

logger.warn(jsonUtils.toJSONString(searchDefinition));

model.jsonModel = {
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
                                    name: "openesdh/xsearch/FilterPane",
                                    config: {
                                        baseType: baseType,
                                        types: searchModel.types,
                                        properties: searchModel.properties,
                                        availableFilters: searchDefinition.availableFilters,
                                        operatorSets: searchDefinition.operatorSets
                                    }
                                },
                                {
                                    name: "openesdh/xsearch/Grid",
                                    config: {
                                        baseType: baseType,
                                        types: searchModel.types,
                                        properties: searchModel.properties,
                                        visibleColumns: searchDefinition.visibleColumns,
                                        availableColumns: searchDefinition.availableColumns,
                                        actions: searchDefinition.actions
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


