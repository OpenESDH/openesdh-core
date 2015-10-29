package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import dk.openesdh.repo.services.documents.DocumentEmailService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Create an outgoing email", defaultFormat = "json", baseUri = "/api/openesdh/case", families = "Case Tools")
public class CaseEmailWebScript {

    @Autowired
    @Qualifier("DocumentEmailService")
    private DocumentEmailService documentEmailService;

    @Authentication(AuthenticationType.USER)
    @Uri(value = "/{caseId}/email", method = HttpMethod.POST)
    public void execute(WebScriptRequest webScriptRequest, @UriVariable(WebScriptUtils.CASE_ID) final String caseId) throws IOException {
        Set<NodeRef> toSet;
        String subject;
        String text;
        List<NodeRef> attachments;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(webScriptRequest.getContent().getContent());
            JSONArray to = (JSONArray) json.get("to");
            subject = (String) json.get("subject");
            text = (String) json.get("message");

            toSet = (Set<NodeRef>) to.stream().map(this::extractNodeRef).collect(Collectors.toSet());

            JSONArray docs = (JSONArray) json.get("documents");
            attachments = (List<NodeRef>) docs.stream().map(this::extractNodeRef).collect(Collectors.toList());
        } catch (ParseException pe) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
        }
        if (toSet.isEmpty()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No recipients");
        }
        documentEmailService.send(caseId, toSet, subject, text, attachments);
    }

    private NodeRef extractNodeRef(Object o) {
        return new NodeRef((String) ((JSONObject) o).get("nodeRef"));
    }

}
