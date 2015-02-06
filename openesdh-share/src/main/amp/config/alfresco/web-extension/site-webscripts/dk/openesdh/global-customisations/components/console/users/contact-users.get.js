<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/global-customisations/components/console/users/contact-users.lib.js">

var services = [],
    widgets = [];

// Append required services...
services.push("openesdh/common/services/CrudService",
    "alfresco/dialogs/AlfDialogService"
);

widgets.push(generateContactPageWidgets());
checkUserCanCreateAccount();

model.jsonModel = {
    rootNodeId: args.htmlid,
    widgets: widgets,
    services: services
};
