define(["dojo/_base/declare"],
    function(declare) {
        
    return declare(null, {
        // Columns which the user can add
        availableColumns: [
            'type',
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
            'type',
            'type',
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
            'query',
            'type',
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
        
        // Filters to show up by default (currently buggy)
        defaultFilters: [],
                   
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
            'type': {
                'title': 'Sagstype',
                'dataType': 'd:text',
                'widgetType': 'select',
                'operatorSets': ['equality'],
                'options': [
                    { label: 'Alle Sager', value: 'all', selected: true },
                    { label: 'Sag', value: 'esdh:case'},
                    { label: 'Jobsag', value: 'esdh:jobCase' },
                    { label: 'Driftssag', value: 'esdh:operationsCase' },
                    { label: 'Kundesag', value: 'esdh:customerCase' },
                    { label: 'PASsag', value: 'esdh:pasCase' },
                    { label: 'Personalesag', value: 'esdh:personnelCase' }
                ]
            },

            'query': {
                'title': 'Søg',
                'dataType': 'd:text',
                'widgetType': 'text',
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
                'widgetType': 'authorityPicker',
                'widgetOptions': {
                    itemType: 'cm:object',
                    multiple: true
                },
                'operatorSets': ['equality']
            },
            

            'oe:title': {
                'title': 'Titel',
                'dataType': 'd:text',
                'widgetType': 'text',
                'operatorSets': ['equality']
            },
            'oe:id': {
                'title': 'ID',
                'dataType': 'd:long',
                'widgetType': 'text',
                'operatorSets': ['equality']
            },
            'cm:modifier': {
                'title': 'Ændret af',
                'dataType': 'd:text',
                'widgetType': 'singleUserSelect',
//                'widgetOptions': {
//                    itemType: 'cm:person',
//                    multiple: true
//                },
                'operatorSets': ['equality']
            },
            'cm:modified': {
                'title': 'Ændret',
                'dataType': 'd:datetime',
                'widgetType': 'dateRange',
                'operatorSets': ['equality']
            },
            'cm:creator': {
                'title': 'Oprettet af',
                'dataType': 'd:text',
                'widgetType': 'singleUserSelect',
//                'widgetOptions': {
//                    itemType: 'cm:person',
//                    multiple: true
//                },
                'operatorSets': ['equality']
            },
            'cm:created': {
                'title': 'Oprettet',
                'dataType': 'd:datetime',
                'widgetType': 'dateRange',
                'operatorSets': ['equality']
            },

            'oe:status': {
                title: 'Status',
                dataType: 'd:text',
                widgetType: 'select',
                options: [
                    { label: 'Ikke startet endnu', value: 'Ikke startet endnu', selected: true},
                    { label: 'Igangværende', value: 'Igangværende' },
                    { label: 'Afsluttet', value: 'Afsluttet' }
                ]
            },
            'case:startDate': {
                title: 'Startdato',
                dataType: 'd:datetime',
                widgetType: 'dateRange'
            },
            
            'case:endDate': {
                title: 'Slutdato',
                dataType: 'd:datetime',
                widgetType: 'dateRange'
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