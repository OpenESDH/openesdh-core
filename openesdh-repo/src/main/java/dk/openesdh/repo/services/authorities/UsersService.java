package dk.openesdh.repo.services.authorities;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;

public interface UsersService {

    String ERROR_REQUIRED = "REQUIRED";
    String ERROR_EMAIL_EXISTS = "USER.ERRORS.EMAIL_EXIST";
    String ERROR_USERNAME_EXISTS = "USER.ERRORS.USERNAME_EXISTS";

    NodeRef createUser(Map<QName, Serializable> userProps, boolean accountEnabled);

    NodeRef updateUser(Map<QName, Serializable> userProps, boolean accountEnabled);

    JSONObject uploadUsersCsv(InputStream usersCsv) throws Exception;

    void registerUserJsonDecorator(Consumer<JSONObject> decorator);

    void registerUserValidator(Consumer<UserSavingContext> validator);

    void registerBeforeSaveAction(Consumer<UserSavingContext> beforeSaveAction);

    void registerAfterSaveAction(Consumer<UserSavingContext> afterSaveAction);

    JSONObject getUserJson(NodeRef nodeRef);

}
