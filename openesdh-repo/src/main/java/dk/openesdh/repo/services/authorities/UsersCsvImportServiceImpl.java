package dk.openesdh.repo.services.authorities;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.TransactionRunner;

@Service("UsersCsvImportService")
public class UsersCsvImportServiceImpl implements UsersCsvImportService {

    @Autowired
    @Qualifier("UsersService")
    private UsersService usersService;
    @Autowired
    private TransactionRunner transactionRunner;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Override
    public JSONObject uploadUsersCsv(InputStream usersCsv) {
        UsersCsvParser parser = new UsersCsvParser();
        List<UsersCsvParser.User> users = parser.parse(usersCsv);
        if (users.isEmpty()) {
            JSONObject json = new JSONObject();
            json.put("totalUsers", 0);
            json.put("users", Collections.emptyList());
            return json;
        }
        return addUsers(users);
    }

    private JSONObject addUsers(List<UsersCsvParser.User> users) {
        return transactionRunner.runInTransaction(() -> {
            JSONArray importResult = new JSONArray();
            int createdUsers = 0;
            for (UsersCsvParser.User user : users) {
                String username = user.getProperties().get(ContentModel.PROP_USERNAME);
                try {
                    usersService.createUser((Map<QName, Serializable>) (Map) user.getProperties(), true, null);
                    importResult.add(importResultJSON(username, null));
                    manageUserMemberships(user);
                    createdUsers++;
                } catch (UsernameExistsDomainException e) {
                    importResult.add(importResultJSON(username, "USER.ERRORS.CSV_DUPLICATE"));
                } catch (UserEmailExistsDomainException e) {
                    importResult.add(importResultJSON(username, "USER.ERRORS.CSV_DUPLICATE_EMAIL"));
                }
            }
            JSONObject json = new JSONObject();
            json.put("totalUsers", users.size());
            json.put("createdUsers", createdUsers);
            json.put("users", importResult);
            return json;
        });
    }

    private JSONObject importResultJSON(String username, String errorCode) {
        JSONObject json = new JSONObject();
        json.put("username", username);
        if (errorCode != null) {
            json.put("error", errorCode);
        }
        return json;
    }

    private void manageUserMemberships(UsersCsvParser.User user) {
        String userName = user.getProperties().get(ContentModel.PROP_USERNAME);
        Set<String> currentGroups = authorityService.getContainingAuthorities(AuthorityType.GROUP, userName, true);
        user.getGroups()
                .stream()
                .map(group -> PermissionService.GROUP_PREFIX + group)
                .filter(group -> !currentGroups.contains(group))
                .forEach(group -> authorityService.addAuthority(group, userName));
    }

}
