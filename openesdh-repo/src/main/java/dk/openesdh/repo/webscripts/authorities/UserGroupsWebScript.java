package dk.openesdh.repo.webscripts.authorities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.authorities.GroupsService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves case owners groups the current user is member of", families = "Authorities")
public class UserGroupsWebScript {

    @Autowired
    @Qualifier("GroupsService")
    private GroupsService groupsService;

    @Uri(value = "/api/openesdh/user/case/owners/groups", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getCurrentUserCaseOwnersGroups() {
        return WebScriptUtils.jsonResolution(groupsService.getCurrentUserGroups());
    }
}
