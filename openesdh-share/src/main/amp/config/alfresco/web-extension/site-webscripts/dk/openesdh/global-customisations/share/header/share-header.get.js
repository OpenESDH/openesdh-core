<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">
<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/oe.js">

//Rearrange the menu
var shareHeaderMenuBar = widgetUtils.findObject(model.jsonModel, "id", "SHARE_HEADER");
var shareHeaderWidgets = shareHeaderMenuBar.config.widgets;

var tmpArray = [shareHeaderWidgets[0],shareHeaderWidgets[2],shareHeaderWidgets[1]];
shareHeaderMenuBar.config.widgets = tmpArray;


var args = page.url.args;
var caseId = url.templateArgs.caseId;
//In the case of some pages (e.g. document details) we need to get the caseId in reverse
// i.e. from the nodeRef
if (caseId == null)
    caseId = getCaseIdFromNodeRef(args.nodeRef);

var nodeRef = getCaseNodeRefFromId(caseId);

//try {null.ex;} catch(e) {logger.log("\n\n(global-customisations/share/header/share-header.get.js) Linenumber: " + e.lineNumber)+"\n\n";}

var caseTypes = getCaseTypesForCaseCreator();

//This object is initialised when the above variable is also initialised in the getCaseTypes() function.
var createFormsArray = {};

/**
 * Return an array of create case menu items.
 * @param caseTypes An array of case types to create menu items for
 * @returns {Array}
 */
function getCreateCaseMenuWidgets (caseTypes) {
    var widgets = [];
    caseTypes.forEach(function (c) {
        var label = c.label;
        widgets.push(
            {
                name: "alfresco/menus/AlfMenuBarItem",
                config: {
                    publishTopic: "OE_SHOW_CREATE_CASE_DIALOG",
                    publishPayload: {caseType : c.type.substr(0, c.type.indexOf(":")) },
                    id: "CASE_MENU_CREATE_" + c.type.replace(":", "_").toUpperCase(),
                    label: label
                }
            }
        );
        createFormsArray[c.typeName] = c.createForm;
    });
    return widgets;
}

var createCasesWidgets = getCreateCaseMenuWidgets(caseTypes);

var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
if (headerMenu != null) {

	var casesMenuWidgets = [{
        name: "alfresco/menus/AlfMenuGroup",
        config: {
            label: msg.get("header.cases.menu.search.group"),
            widgets: [
                {
                    name: "alfresco/menus/AlfMenuBarItem",
                    config: {
                        id: "CASE_MENU_SEARCH_LINK",
                        label: msg.get("header.cases.menu.search"),
                        targetUrl: "oe/case/search"
                    }
                },
                {
                    name: "alfresco/menus/AlfMenuBarItem",
                    config: {
                        label: msg.get("header.cases.menu.savedsearch"),
                        disabled: true
                    }
                }
            ]
        }
    }];
	
	//show create case menu items only if user is permitted
	if(createCasesWidgets && createCasesWidgets.length > 0){
		casesMenuWidgets.push({
	        name: "alfresco/menus/AlfMenuGroup",
	        config: {
	            id: "CASE_MENU_CREATE_CASE_GROUP",
	            label: msg.get("header.cases.menu.create.group"),
	            widgets: createCasesWidgets
	        }
	    });
	}
    
    // This widget is required to handle ALF_UPLOAD_REQUEST topics
	casesMenuWidgets.push({
        name: "openesdh/common/services/UploadService"
    });
	
    headerMenu.config.widgets.splice(1,1, {
        id: "HEADER_CASES_DROPDOWN",
        name: "alfresco/header/AlfMenuBarPopup",
        config: {
            label: msg.get("header.cases.menu.label"),
            widgets: casesMenuWidgets
        }
    });

    headerMenu.config.widgets.push(
        {
            name: "alfresco/menus/AlfMenuBarItem",
            config: {
                label: "Dagsorden",
                disabled: true
            }
        }
    );
}

var caseService= {
    name: "openesdh/common/services/CaseService",
    config:{
        caseId: caseId,
        casesFolderNodeRef: getNewCaseFolderNodeRef(),
        createCaseWidgets: createFormsArray,
        nodeRef: (nodeRef != null) ? nodeRef : args.destination,
        currentUser: getCurrentUser(),
        caseConstraintsList: getCaseConstraints()
    }
};


/**
 * Attempt to build a URL for retrieving a logo image for the title bar.
 *
 * @returns {string}
 */
function getOpenEsdhHeaderLogoUrl() {
   // Generate the source for the logo...
   var logoSrc = context.getSiteConfiguration().getProperty("logo");
   if (logoSrc && logoSrc.length() > 0)
   {
      // Use the site configured logo as the source for the logo image.
      logoSrc = url.context + "/proxy/alfresco/api/node/" + logoSrc.replace("://", "/") + "/content";
   }
   else
   {
      // Use the message bundled configured logo as the logo source.
      // This is theme specific
	   
	  // Commented out the code to eliminate issues with overriding the header.logo message
//      var propsLogo = msg.get("header.logo");
//      if (propsLogo == "header.logo")
//      {
//         propsLogo = "openesdh-logo-48.png";
//      }
	   
      var propsLogo = "openesdh-logo-48.png";
      logoSrc = url.context + "/res/openesdh/images/" + propsLogo;
   }
   return logoSrc;
}

var headerLogo = widgetUtils.findObject(model.jsonModel, "id", "HEADER_LOGO");
headerLogo.config.logoSrc = getOpenEsdhHeaderLogoUrl();

model.jsonModel.services.push(caseService);
model.jsonModel.services.push("openesdh/common/services/AuthorityService");
model.jsonModel.services.push("alfresco/services/OptionsService");
model.jsonModel.services.push("alfresco/services/DialogService");


var verticalLayout = widgetUtils.findObject(model.jsonModel, "id", "SHARE_VERTICAL_LAYOUT");
if(verticalLayout){
    var HEADER_TITLE_BAR = widgetUtils.findObject(model.jsonModel, "id", "HEADER_TITLE_BAR");
    
    
    var HEADER_TITLE = widgetUtils.findObject(model.jsonModel, "id", "HEADER_TITLE"),
    	HEADER_NAVIGATION_MENU_BAR = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR"),
    	HEADER_TITLE_MENU = widgetUtils.findObject(model.jsonModel, "id", "HEADER_TITLE_MENU");
    
    HEADER_TITLE_BAR.config.widgets.length = 0;
    HEADER_TITLE_BAR.config.widgets.push({
    	  name: "alfresco/layout/LeftAndRight",
    	  config: {
    	    widgets: [
    	      HEADER_TITLE,
    	      HEADER_TITLE_MENU
    	    ]
    	  }
    	});
    
    HEADER_TITLE_BAR.config.widgets.push(HEADER_NAVIGATION_MENU_BAR);
    
    
    verticalLayout.config.widgets.pop();
    verticalLayout.config.widgets.push({
        id: "HEADER_TITLE_BAR",
        name: "openesdh/common/widgets/layout/BootstrapContainer",
        config: {
            additionalCssClasses: "share-header-title",
            widgets: HEADER_TITLE_BAR.config.widgets
        }
    });
    
    var SHARE_HEADER = widgetUtils.findObject(model.jsonModel, "id", "SHARE_HEADER");
    verticalLayout.config.widgets.shift();
    verticalLayout.config.widgets.unshift({
                name: "openesdh/common/widgets/layout/BootstrapContainer",
                config: {
                    additionalCssClasses: "alfrescoHeader",
                    widgets: [SHARE_HEADER]
                }
    });
}