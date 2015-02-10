package dk.openesdh.repo.webscripts.notes;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.notes.NoteService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Notes extends AbstractRESTWebscript {
    private NodeService nodeService;
    private NoteService noteService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    protected void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse
            res) throws IOException {
        // process additional parameters
        boolean reverse = req.getParameter("reverse") != null ?
                Boolean.valueOf(req.getParameter("reverse")) : false;

        // TODO: Paging?
//        int startIndex = req.getParameter("startIndex") != null ?
//                Integer.valueOf(req.getParameter("startIndex")) : 0;
//        int pageSize = req.getParameter("pageSize ") != null ?
//                Integer.valueOf(req.getParameter("pageSize ")) : 10;

        List<NodeRef> nodeRefs = noteService.getNotes(nodeRef);

        if (reverse) {
            Collections.reverse(nodeRefs);
        }

        try {
            JSONArray json = buildJSON(nodeRefs);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        JSONObject jsonReq = new JSONObject(new JSONTokener(req.getContent()
                .getContent()));
        String content = jsonReq.getString("content");
        String author = jsonReq.getString("author");

        NodeRef noteNodeRef = noteService.createNote(nodeRef, content, author);
        try {
            JSONObject json = buildJSON(noteNodeRef);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        nodeService.deleteNode(nodeRef);
    }

    @Override
    protected void put(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        JSONObject jsonReq = new JSONObject(new JSONTokener(req.getContent().getContent()));
        String content = jsonReq.getString("content");
        String author = jsonReq.getString("author");

        noteService.updateNote(nodeRef, content, author);

        try {
            JSONObject json = buildJSON(nodeRef);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONObject buildJSON(NodeRef nodeRef) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("nodeRef", nodeRef);

        obj.put("content", nodeService.getProperty(nodeRef, OpenESDHModel.PROP_NOTE_CONTENT));

        obj.put("author", nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHOR));

        obj.put("creator", nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
        obj.put("created", nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED));
        obj.put("modified", nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
        return obj;
    }

    JSONArray buildJSON(List<NodeRef> nodeRefs) throws JSONException {
        JSONArray result = new JSONArray();

        for (NodeRef nodeRef : nodeRefs) {
            result.put(buildJSON(nodeRef));
        }

        return result;
    }


}
