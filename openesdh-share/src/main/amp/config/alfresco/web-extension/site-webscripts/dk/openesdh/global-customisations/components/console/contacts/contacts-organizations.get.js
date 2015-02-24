<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/global-customisations/components/console/contacts/contacts.lib.js">

var contactType = "contact:organization";

var services = [],
    widgets = [];

// Append required services...
services.push("openesdh/common/services/CrudService",
    "openesdh/common/services/ContactsActionService",
    "alfresco/services/OptionsService",
    "alfresco/dialogs/AlfDialogService",
    "alfresco/services/NotificationService",
    "openesdh/common/services/LegacyFormService"
);

widgets.push(generateContactPageWidgets(contactType));

model.jsonModel = {
    rootNodeId: args.htmlid,

    widgets: widgets,
    services: services
};