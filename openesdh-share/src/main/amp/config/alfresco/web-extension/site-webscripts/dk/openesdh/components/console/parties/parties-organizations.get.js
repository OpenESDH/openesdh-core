<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/components/console/parties/parties.lib.js">//</import>

var partyType = "party:organization";

var services = [],
    widgets = [];

// Append required services...
services.push("alfresco/services/CrudService",
    "alfresco/services/OptionsService",
    "alfresco/dialogs/AlfDialogService",
    "alfresco/services/NotificationService",
    "openesdh/common/services/LegacyFormService"
);

widgets.push(generatePartyPageWidgets(partyType));

model.jsonModel = {
    rootNodeId: args.htmlid,

    widgets: widgets,
    services: services
};
