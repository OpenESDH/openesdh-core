function main() {
    var shortName = url.templateArgs.shortName;
    var userName = url.templateArgs.userName;
    var groupFullName = "GROUP_" + shortName;

    var person = people.getPerson(userName);//TODO should we error check this?? Is it actually necessary??
    var userGroups = people.getContainerGroups(person);
    if (userGroups == null) {
        // Group cannot be found
        status.setCode(status.STATUS_NOT_FOUND, "The user :" + userName + ", does not seem to be part of any group.");
        return false;
    }
    else {
        for (var i = 0; i < userGroups.length; i++) {
            if (userGroups[i].name == groupFullName) return true;
        }
        return false;
    }
}

model.isMember = main();