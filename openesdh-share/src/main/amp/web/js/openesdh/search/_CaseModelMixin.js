define(["dojo/_base/declare"],
    function(declare) {
        
    return declare(null, {
        // Columns which the user can add
        availableColumns: [
            'TYPE',
            'oe:id',
            'oe:title',
            'case:owners',
            'cm:modified',
            'cm:created',
            'oe:status',
            'case:startDate',
            'case:endDate',
        ],
        
        // Columns which are visible by default (others will be hidden)
        defaultColumns: [
            'TYPE',
            'oe:id',
            'oe:title',
            'case:owners',
            'oe:status',
            'case:startDate',
            'case:endDate',
            'cm:modified',
            'cm:created'
        ],
        
        // Properties that the user can filter on
        availableFilters: [
            'ALL',
            'TYPE',
//            'role',
            'oe:id',
            'oe:title',
            'case:owners',
            'cm:modified',
            'cm:created',
            'oe:status',
            'case:startDate',
            'case:endDate'
//            'isFavourite'
        ],
                   
        // Definitions of different sets of operators that filters can have
        operatorSets: {
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
        },
           
        propertyDefinitions: {
            // TODO: Don't hard-code
            'TYPE': {
                'title': 'Sagstype',
                'dataType': 'd:text',
                'widgetType': 'CaseFilterSelectWidget',
                'operatorSets': ['equality'],
                'options': [
                    { label: 'Alle Sager', value: '', selected: true },
                    { label: 'Simple Case', value: 'case:simple'}
                ]
            },

            'ALL': {
                'title': 'Søg',
                'dataType': 'd:text',
                'widgetType': 'CaseFilterTextWidget',
                'operatorSets': ['equality']
            },

//            'role': {
//                'title': 'Rolle',
//                'dataType': 'd:text',
//                'widgetType': 'role',
//                'widgetOptions': {
//                    authorityPicker: {
//                        itemType: 'cm:object',
//                        multiple: true
//                    }
//                },
//                'operatorSets': ['equality']
//            },
            
            
            'case:owners': {
                'title': 'Sagsejer',
                'dataType': 'authority',
                'widgetType': 'CaseFilterAuthorityWidget',
                'widgetOptions': {
                    itemType: 'cm:object',
                    multiple: true
                },
                'operatorSets': ['equality']
            },
            

            'oe:title': {
                'title': 'Titel',
                'dataType': 'd:text',
                'widgetType': 'CaseFilterTextWidget',
                'operatorSets': ['equality']
            },
            'oe:id': {
                'title': 'ID',
                'dataType': 'd:long',
                'widgetType': 'CaseFilterTextWidget',
                'operatorSets': ['equality']
            },
            'cm:modifier': {
                'title': 'Ændret af',
                'dataType': 'd:text',
                'widgetType': 'CaseFilterTextWidget',
//                'widgetOptions': {
//                    itemType: 'cm:person',
//                    multiple: true
//                },
                'operatorSets': ['equality']
            },
            'cm:modified': {
                'title': 'Ændret',
                'dataType': 'd:datetime',
                'widgetType': 'CaseFilterDateRangeWidget',
                'operatorSets': ['equality']
            },
            'cm:creator': {
                'title': 'Oprettet af',
                'dataType': 'd:text',
                'widgetType': 'CaseFilterTextWidget',
//                'widgetOptions': {
//                    itemType: 'cm:person',
//                    multiple: true
//                },
                'operatorSets': ['equality']
            },
            'cm:created': {
                'title': 'Oprettet',
                'dataType': 'd:datetime',
                'widgetType': 'CaseFilterDateRangeWidget',
                'operatorSets': ['equality']
            },

            'oe:status': {
                title: 'Status',
                dataType: 'd:text',
                widgetType: 'CaseFilterSelectWidget',
                options: [
                    { label: 'Ikke startet endnu', value: 'Ikke startet endnu', selected: true},
                    { label: 'Igangværende', value: 'Igangværende' },
                    { label: 'Afsluttet', value: 'Afsluttet' }
                ]
            },
            'case:startDate': {
                title: 'Startdato',
                dataType: 'd:datetime',
                widgetType: 'CaseFilterDateRangeWidget'
            },
            
            'case:endDate': {
                title: 'Slutdato',
                dataType: 'd:datetime',
                widgetType: 'CaseFilterDateRangeWidget'
            }

//            'isFavourite': {
//                title: 'Favorit',
//                dataType: 'd:boolean',
//                widgetType: 'select',
//                options: [
//                    { label: 'Ja', value: 'true' },
//                    { label: 'Nej', value: 'false' }
//                ]
//            }
        }
   });
});