function getSearchModel() {
    var String = java.lang.String;

    var types = null,
        typesMain = config.scoped["SearchModel"];
    types = typesMain;
//    if (typesMain != null) {
//        types = typesMain["types"];
//    }

    var caseTypes = {};
    var iter = types.getChildren().iterator();
    while(iter.hasNext()) {
        var caseType = iter.next();
        logger.warn(caseType);
//
//        var caseTypeObj = {
//            'label': caseType.getAttribute(new String('label')),
//            'roles': []
//        }
//
//        var rolesIter = caseType.getChildren().iterator();
//        while(rolesIter.hasNext()) {
//            var role = rolesIter.next();
//            var groupsAllowed = role.getAttribute(new String('groupsAllowed'));
//            // If not explicitly set to false, groups are allowed
//            groupsAllowed = groupsAllowed == "false" ? false : true;
//            caseTypeObj.roles.push({
//                'name': role.getAttribute(new String('name')),
//                'label': role.getAttribute(new String('label')),
//                'groupsAllowed': groupsAllowed
//            });
//        }
//
//        caseTypes[caseType.getAttribute(new String('type'))] = caseTypeObj;
    }

    return caseTypes;
}
