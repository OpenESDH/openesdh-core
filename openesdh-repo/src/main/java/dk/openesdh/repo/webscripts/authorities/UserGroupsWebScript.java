package dk.openesdh.repo.webscripts.authorities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves case owners groups the current user is member of", families = "User Groups")
public class UserGroupsWebScript {

    private static final String SITE_GROUP_PREFIX = "GROUP_site_";
    
    private static final String SITE_GROUP_PREFIX2 = "GROUP_SITE_";

    private static final String ALFRESCO_GROUP_PREFIX = "GROUP_ALFRESCO_";

    private static final String EMAIL_GROUP_PREFIX = "GROUP_EMAIL_";

    private static final List<String> nonCaseOwnersGroupPrefixes = new ArrayList<String>();

    static {
        nonCaseOwnersGroupPrefixes.addAll(Arrays.asList(
                CaseService.CASE_ROLE_GROUP_NAME_PREFIX, 
                SITE_GROUP_PREFIX,
                SITE_GROUP_PREFIX2, 
                ALFRESCO_GROUP_PREFIX, 
                EMAIL_GROUP_PREFIX
        ));
    }

    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Uri(value = "/api/openesdh/user/case/owners/groups", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getCurrentUserCaseOwnersGroups() {
        String name = AuthenticationUtil.getFullyAuthenticatedUser();
        Set<String> groups = authorityService.getContainingAuthorities(AuthorityType.GROUP, name, false)
                .stream()
                .filter(this::isCaseOwnersGroup)
                .collect(Collectors.toSet());
        return WebScriptUtils.jsonResolution(groups);
    }

    private boolean isCaseOwnersGroup(String groupName) {
        return !nonCaseOwnersGroupPrefixes.stream()
                .filter(groupName::startsWith)
                .findAny()
                .isPresent();
    }
}
