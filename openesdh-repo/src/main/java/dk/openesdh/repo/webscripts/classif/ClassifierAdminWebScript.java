package dk.openesdh.repo.webscripts.classif;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.services.classification.ClassifierAdminRegistry;
import dk.openesdh.repo.services.classification.ClassifierAdminService;
import dk.openesdh.repo.webscripts.WebScriptParams;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Provides API for classifiers administration", families = "classifiers")
public class ClassifierAdminWebScript {

    @Autowired
    @Qualifier("ClassifierAdminRegistry")
    private ClassifierAdminRegistry classifAdminRegistry;

    @Uri(value = "/api/openesdh/classifer/{classifType}", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getAllTypes(@UriVariable("classifType") String classifType)
            throws IOException, JSONException {
        return WebScriptUtils.jsonResolution(classifAdminRegistry.getService(classifType).getAdminClassifValues());
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/classifer/{classifType}", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution post(@UriVariable("classifType") String classifType, WebScriptRequest req) throws IOException, JSONException {
        ClassifierAdminService service = classifAdminRegistry.getService(classifType);
        ClassifValue value = getClassifValue(service.getClassifValueClass(), req);
        return WebScriptUtils.jsonResolution(service.createOrUpdateClassifValue(value));
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/classifer/{classifType}/{storeType}/{storeId}/{id}", method = HttpMethod.DELETE, defaultFormat = "json")
    public Resolution delete(
            @UriVariable("classifType") String classifType,
            @UriVariable(WebScriptParams.STORE_TYPE) String storeType,
            @UriVariable(WebScriptParams.STORE_ID) String storeId, 
            @UriVariable(WebScriptParams.ID) String id)
            throws IOException, JSONException {
        NodeRef valueRef = new NodeRef(storeType, storeId, id);
        ClassifierAdminService service = classifAdminRegistry.getService(classifType);
        service.deleteClassifValue(valueRef);
        return WebScriptUtils.jsonResolution("Deleted succesfully");
    }

    private ClassifValue getClassifValue(Class<? extends ClassifValue> clazz, WebScriptRequest req)
            throws IOException {
        return (ClassifValue) WebScriptUtils.readJson(clazz, req);
    }
}
