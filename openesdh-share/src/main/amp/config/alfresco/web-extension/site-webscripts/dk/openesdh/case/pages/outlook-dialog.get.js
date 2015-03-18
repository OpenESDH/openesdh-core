model.jsonModel = {
    widgets: [
        {
            id: "outlookForm",
            name: "alfresco/forms/Form",
            config: {
                okButtonPublishTopic: "OFFICE_INTEGRATION_OK",
                okButtonPublishGlobal: true,
                cancelButtonPublishTopic: "OFFICE_INTEGRATION_CANCEL",
                cancelButtonPublishGlobal: true,
                widgets: [

                    {
                        name: "alfresco/buttons/AlfButton",
                        config: {
                            label: "Send test data",
                            publishTopic: "OFFICE_INTEGRATION_LOAD",
                            publishPayload: {"From":{"EMail":"torben.rasmussen@visma.com","Name":"torben.rasmussen@visma.com"},"To":[{"EMail":"Torben Nürnberg Rasmussen","Name":"Torben Nürnberg Rasmussen"}],"CC":[],"BCC":[],"MetaData":[{"Key":"Application","Value":"Microsoft.Office.Interop.Outlook.ApplicationClass"},{"Key":"Class","Value":"43"},{"Key":"Session","Value":"System.__ComObject"},{"Key":"Parent","Value":"System.__ComObject"},{"Key":"Actions","Value":"System.__ComObject"},{"Key":"Attachments","Value":"System.__ComObject"},{"Key":"BillingInformation","Value":""},{"Key":"Body","Value":"test with attachments"},{"Key":"Categories","Value":""},{"Key":"Companies","Value":""},{"Key":"ConversationIndex","Value":"01024F20E62464C874A26AC270B42C8297EC94840C8D"},{"Key":"ConversationTopic","Value":"test"},{"Key":"CreationTime","Value":"21-01-2015 16:03:22"},{"Key":"EntryID","Value":"00000000374095076FE4D34D8A27ABB175F7FD730700C3B68E10F77511CEB4CD00AA00BBB6E600000000000C0000D9539C2261A6BB45B9DAB62C7081B3C10100E10B00000000"},{"Key":"FormDescription","Value":"System.__ComObject"},{"Key":"GetInspector","Value":"System.__ComObject"},{"Key":"Importance","Value":"1"},{"Key":"LastModificationTime","Value":"21-01-2015 16:03:10"},{"Key":"MessageClass","Value":"IPM.Note"},{"Key":"Mileage","Value":""},{"Key":"NoAging","Value":"False"},{"Key":"OutlookInternalVersion","Value":"0"},{"Key":"OutlookVersion","Value":""},{"Key":"Saved","Value":"True"},{"Key":"Sensitivity","Value":"0"},{"Key":"Size","Value":"1425552"},{"Key":"Subject","Value":"test"},{"Key":"UnRead","Value":"False"},{"Key":"UserProperties","Value":"System.__ComObject"},{"Key":"AlternateRecipientAllowed","Value":"True"},{"Key":"AutoForwarded","Value":"False"},{"Key":"BCC","Value":""},{"Key":"CC","Value":""},{"Key":"DeferredDeliveryTime","Value":"01-01-4501 00:00:00"},{"Key":"DeleteAfterSubmit","Value":"False"},{"Key":"ExpiryTime","Value":"01-01-4501 00:00:00"},{"Key":"FlagRequest","Value":""},{"Key":"HTMLBody","Value":"\u003cdiv dir=\"ltr\"\u003etest with attachments\u003c/div\u003e\r\n"},{"Key":"OriginatorDeliveryReportRequested","Value":"False"},{"Key":"ReadReceiptRequested","Value":"False"},{"Key":"ReceivedByEntryID","Value":"\u0000\u0000????????\u0000?Torben Rasmussen\u0000SMTP\u0000torben.nurnberg@gmail.com\u0000"},{"Key":"ReceivedByName","Value":"Torben Rasmussen"},{"Key":"ReceivedOnBehalfOfEntryID","Value":"\u0000\u0000????????\u0000?Torben Rasmussen\u0000SMTP\u0000torben.nurnberg@gmail.com\u0000"},{"Key":"ReceivedOnBehalfOfName","Value":"Torben Rasmussen"},{"Key":"ReceivedTime","Value":"21-01-2015 16:03:10"},{"Key":"RecipientReassignmentProhibited","Value":"False"},{"Key":"Recipients","Value":"System.__ComObject"},{"Key":"ReminderOverrideDefault","Value":"False"},{"Key":"ReminderPlaySound","Value":"False"},{"Key":"ReminderSet","Value":"False"},{"Key":"ReminderSoundFile","Value":""},{"Key":"ReminderTime","Value":"01-01-4501 00:00:00"},{"Key":"RemoteStatus","Value":"0"},{"Key":"ReplyRecipientNames","Value":""},{"Key":"ReplyRecipients","Value":"System.__ComObject"},{"Key":"SenderName","Value":"Torben Rasmussen"},{"Key":"Sent","Value":"True"},{"Key":"SentOn","Value":"2/3/2015 8:33:26 PM"},{"Key":"SentOnBehalfOfName","Value":"Torben Rasmussen"},{"Key":"Submitted","Value":"False"},{"Key":"To","Value":"Torben N?rnberg Rasmussen"},{"Key":"VotingOptions","Value":""},{"Key":"VotingResponse","Value":""},{"Key":"ItemProperties","Value":"System.__ComObject"},{"Key":"BodyFormat","Value":"2"},{"Key":"DownloadState","Value":"1"},{"Key":"InternetCodepage","Value":"65001"},{"Key":"MarkForDownload","Value":"0"},{"Key":"IsConflict","Value":"False"},{"Key":"AutoResolvedWinner","Value":"False"},{"Key":"Conflicts","Value":"System.__ComObject"},{"Key":"SenderEmailAddress","Value":"torben.rasmussen@visma.com"},{"Key":"SenderEmailType","Value":"SMTP"},{"Key":"Permission","Value":"0"},{"Key":"PermissionService","Value":"0"},{"Key":"PropertyAccessor","Value":"System.__ComObject"},{"Key":"SendUsingAccount","Value":"System.__ComObject"},{"Key":"TaskSubject","Value":"test"},{"Key":"TaskDueDate","Value":"01-01-4501 00:00:00"},{"Key":"TaskStartDate","Value":"01-01-4501 00:00:00"},{"Key":"TaskCompletedDate","Value":"01-01-4501 00:00:00"},{"Key":"ToDoTaskOrdinal","Value":"01-01-4501 00:00:00"},{"Key":"IsMarkedAsTask","Value":"False"},{"Key":"ConversationID","Value":"64C874A26AC270B42C8297EC94840C8D"},{"Key":"Sender","Value":"System.__ComObject"},{"Key":"RTFBody","Value":"System.Byte[]"},{"Key":"RetentionPolicyName","Value":""},{"Key":"RetentionExpirationDate","Value":"01-01-4501 00:00:00"}],"Subject":"test","BodyText":"test with attachments","BodyHtml":"\u003cdiv dir=\"ltr\"\u003etest with attachments\u003c/div\u003e\r\n","Attachments":[{"Name":"test.txt","MimeType":"olByValue","ForceImport":false},{"Name":"OpenESDH Class Diagram.pdf","MimeType":"olByValue","ForceImport":false},{"Name":"Løsningsbeskrivelse.docx","MimeType":"olByValue","ForceImport":false}]},
                            publishGlobal: true
                        }
                    },

                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            name: "caseId",
                            assignTo: "pickedCaseWidget",
                            label: "Case"
                        }
                    },
                    {
                        name: "alfresco/buttons/AlfButton",
                        config: {
                            label: "Find Case",
                            publishTopic: "OE_FIND_CASE",
                            publishGlobal: true
                        },
                        assignTo: "formDialogButton",
                    },
                    {
                        name: "alfresco/buttons/AlfButton",
                        config: {
                            label: "Create Case",
                            publishTopic: "OE_CREATE_CASE",
                            publishGlobal: true
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            name: "subject",
                            label: msg.get("page.title.label"),
                            validationConfig: {
                                regex: "^[A-Za-z0-9-]+",
                                errorMessage: "Title must not be empty"
                            }
                        }
                    },
                    {
                        name: "aoi/common/widgets/controls/SelectionList",
                        config: {
                            name: "attachments",
                            label: msg.get("page.attachments.label"),
                            initiallySelected: true,
                            itemKey: "name",
                            widgets: [
                                {
                                    name: "alfresco/lists/AlfList",
                                    config: {
                                        waitForPageWidgets: true,
                                        loadDataPublishTopic: null,
                                        itemKey: "name",
                                        noDataMessage: msg.get("page.no-attachments.message"),
                                        widgets: [
                                            {
                                                name: "alfresco/documentlibrary/views/AlfDocumentListView",
                                                config: {
                                                    itemKey: "name",
                                                    widgets: [
                                                        {
                                                            name: "alfresco/documentlibrary/views/layouts/Row",
                                                            config: {
                                                                widgets: [
                                                                    {
                                                                        name: "alfresco/documentlibrary/views/layouts/Cell",
                                                                        config: {
                                                                            additionalCssClasses: "mediumpad",
                                                                            widgets: [
                                                                                {
                                                                                    name: "aoi/common/widgets/BetterSelector",
                                                                                    config: {
                                                                                        itemKey: "name"
                                                                                    }
                                                                                }
                                                                            ]
                                                                        }
                                                                    },
                                                                    {
                                                                        name: "alfresco/documentlibrary/views/layouts/Cell",
                                                                        config: {
                                                                            additionalCssClasses: "mediumpad",
                                                                            widgets: [
                                                                                {
                                                                                    name: "alfresco/renderers/Property",
                                                                                    config: {
                                                                                        propertyToRender: "name",
                                                                                        renderAsLink: false
                                                                                    }
                                                                                }
                                                                            ]
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    ],
    services: [
        {
            name: "openesdh/common/services/OutlookCaseService",
            config: {
                formId: "outlookForm"
            }
        },
        "alfresco/dialogs/AlfDialogService",
        "alfresco/services/DocumentService",
        {
            name: "openesdh/common/services/CaseService",
            config: {
                fieldId: "35cbd518-a7c1-44c8-89ba-7e3d781222be"
            }
        },
        {
            name: "alfresco/services/NavigationService",
            config: {
                fieldId: "35cbd518-a7c1-5432-89ba-7e3d781154be"
            }
        }
    ]
};