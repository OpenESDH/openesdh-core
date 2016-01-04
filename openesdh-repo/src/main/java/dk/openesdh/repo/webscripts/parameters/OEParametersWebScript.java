package dk.openesdh.repo.webscripts.parameters;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import java.io.IOException;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.model.OEParameter;
import dk.openesdh.repo.services.parameters.OEParametersService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage OE Parameters webscript", families = {"Administration tools"})
public class OEParametersWebScript {

    @Autowired
    private OEParametersService parametersService;

    @Uri(value = "/api/openesdh/parameters", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getParameters() {
        JSONArray oeParameters = new JSONArray();
        oeParameters.addAll(parametersService
                .getOEParameters()
                .stream()
                .map(OEParameter::toJSONObject)
                .collect(Collectors.toList()));
        return WebScriptUtils.jsonResolution(oeParameters);
    }

    @Uri(value = "/api/openesdh/parameter/{name}", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(@UriVariable final String name) {
        return WebScriptUtils.jsonResolution(
                parametersService
                .getOEParameter(name)
                .toJSONObject());
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/parameters", method = HttpMethod.POST, defaultFormat = "json")
    public void saveParameters(WebScriptRequest req) {
        JSONArray parsedRequest;
        try {
            parsedRequest = (JSONArray) new JSONParser().parse(req.getContent().getContent());
        } catch (IOException | ParseException io) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
        }
        parsedRequest.forEach(this::parseAndSaveParameter);
    }

    private void parseAndSaveParameter(Object item) {
        JSONObject json = (JSONObject) item;
        parametersService.saveOEParameter(
                json.containsKey("nodeRef") ? new NodeRef((String) json.get("nodeRef")) : null,
                (String) json.get("name"),
                json.get("value")
        );
    }
}
