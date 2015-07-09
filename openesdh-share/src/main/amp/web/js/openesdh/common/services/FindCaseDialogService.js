define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin"
    ],
    function (declare, AlfCore, CoreXhr, lang, _TopicsMixin) {

        return declare([AlfCore, CoreXhr, _TopicsMixin], {
        	
        	constructor: function(args) {
                lang.mixin(this, args);
                this.retrieveSearchDefinition();
                this.alfSubscribe(this.FindCaseDialogTopic, lang.hitch(this, this.onFindCase));
        	},
        	
        	retrieveSearchDefinition: function() {
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/case/searchDefinition/simple_case",
                    method: "GET",
                    handleAs: "json",
                    successCallback: function (response, config) {
                        this.searchDefinition = response;
                    },
                    failureCallback: function(response, config){
                        alert("failure: " + JSON.stringify(response));
                    },
                    callbackScope: this
                });
            },
        	
        	onFindCase : function(payload){
        		var rowSelectionTopic = "GRID_ROW_SELECTED";
        		var dialogHideTopic = rowSelectionTopic;
        		if(payload != null){
        			if(payload.gridRowSelectedTopic != null){
        				rowSelectionTopic = payload.gridRowSelectedTopic;
        			}
        			if(payload.dialogHideTopic != null){
        				dialogHideTopic = payload.dialogHideTopic;
        			}
        		}
        		var dialog = {
                        contentWidth: "800px",
                        dialogTitle: "This is my Find Case",
                        handleOverflow: false,
                        hideTopic: dialogHideTopic,
                        widgetsContent: [
                            {
                                name: 'alfresco/layout/VerticalWidgets',
                                config: {
                                    widgets: [
                                        {
                                            name: "openesdh/xsearch/FilterPane",
                                            config: {
                                                baseType: "base:case",
                                                types: this.searchDefinition.model.types,
                                                properties: this.searchDefinition.model.properties,
                                                availableFilters: ["cm:title", "oe:status", "cm:created"],
                                                operatorSets: this.searchDefinition.operatorSets
                                            }
                                        },
                                        {
                                            name: "openesdh/xsearch/Grid",
                                            config: {
                                                baseType: "base:case",
                                                types: this.searchDefinition.model.types,
                                                properties: this.searchDefinition.model.properties,
                                                visibleColumns: ["oe:id", "cm:title", "oe:status", "cm:created"],
                                                availableColumns: ["oe:id", "cm:title", "oe:status", "cm:created"],
                                                actions: [],
                                                rowsPerPage: 15,
                                                pageSizeOptions: [],
                                                rowSelectionTopic : rowSelectionTopic
                                            }
                                        }
                                    ]
                                }
                            }
                        ],
                        widgetsButtons: [
                            {
                                name: "alfresco/buttons/AlfButton",
                                config: {
                                    label: "Cancel",
                                    publishTopic: "NO_OP"
                                }
                            }
                        ]
                    };
        		
        		this.alfPublish("ALF_CREATE_DIALOG_REQUEST", dialog);
        	}
        });        
});
