define(["dojo/_base/declare",
        "alfresco/renderers/Property"],
    function (declare, Property) {
        return declare([Property], {
            mapValueToDisplayValue: function(value){
                return this.message("document.state.constraint.label."+value);
            }
        });
    });