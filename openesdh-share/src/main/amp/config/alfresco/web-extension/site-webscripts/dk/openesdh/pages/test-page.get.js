var form = {
    name: "alfresco/forms/Form",
    config: {
        showOkButton: true,
        okButtonLabel: "Save",
        showCancelButton: false,
        cancelButtonLabel: "Doesn't Matter",
        okButtonPublishTopic: "PUBLISH_TOPIC",
        okButtonPublishGlobal: true,
        widgets: []
    }
};

var textBox = {
    name: "alfresco/forms/controls/DojoValidationTextBox",
    config: {
        fieldId: "EMAIL",
        name: "email",
        label: "Contact",
        description: "Your e-mail address",
        placeHolder: "e-mail",
        validationConfig: [
            {
                validation: "regex",
                regex: "^([0-9a-zA-Z]([-.w]*[0-9a-zA-Z])*@([0-9a-zA-Z][-w]*[0-9a-zA-Z].)+[a-zA-Z]{2,9})$",
                errorMessage: "Valid E-mail Address Required"
            }
        ]
    }
};
form.config.widgets.push(textBox);

var checkbox = {
    name: "alfresco/forms/controls/DojoCheckBox",
    config: {
        fieldId: "SHOW",
        name: "showEmail",
        label: "Show E-mail",
        description: "Uncheck to hide the e-mail field",
        value: true
    }
};
form.config.widgets.push(checkbox);

textBox.config.visibilityConfig = {
    initialValue: true,
    rules: [{
            targetId: "SHOW",
            is: [true]
    }]
};

model.jsonModel = {
    widgets: [ form ]
};