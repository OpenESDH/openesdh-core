package dk.openesdh.repo.services.authorities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;

public interface UsersService {

    String ERROR_REQUIRED = "ERROR.REQUIRED";
    String ERROR_EMAIL_EXISTS = "USER.ERRORS.EMAIL_EXIST";
    String ERROR_USERNAME_EXISTS = "USER.ERRORS.USERNAME_EXISTS";

    NodeRef createUser(Map<QName, Serializable> userProps, boolean accountEnabled, List<UserSavingContext.Assoc> associations);

    NodeRef updateUser(Map<QName, Serializable> userProps, boolean accountEnabled, List<UserSavingContext.Assoc> associations);

    void registerUserJsonDecorator(Consumer<JSONObject> decorator);

    void registerUserValidator(Consumer<UserSavingContext> validator);

    void registerBeforeSaveAction(Consumer<UserSavingContext> beforeSaveAction);

    void registerAfterSaveAction(Consumer<UserSavingContext> afterSaveAction);

    JSONObject getUserJson(NodeRef nodeRef);

    /**
     * Retrieves names of the users the current user is a manager of
     * 
     * @return
     */
    Set<String> getCurrentUserSubordinateNames();
}
