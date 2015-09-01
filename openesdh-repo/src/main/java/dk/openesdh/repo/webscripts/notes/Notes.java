package dk.openesdh.repo.webscripts.notes;

import java.io.IOException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.model.Note;
import dk.openesdh.repo.services.notes.NoteService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import dk.openesdh.repo.webscripts.PageableWebScript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class Notes extends AbstractRESTWebscript {

    protected static Logger logger = Logger.getLogger(Notes.class);

    private NodeService nodeService;
    private NoteService noteService;
    private PersonService personService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    protected void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse
            res) throws IOException {
        PageableWebScript<Note> ws = (int startIndex, int pageSize) -> noteService.getNotes(nodeRef, startIndex,
                pageSize);
        PageableWebScript.getItemsPage(req, res, ws);
    }

    @Override
    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        Note note = (Note) WebScriptUtils.readJson(Note.class, req);
        note.setAuthor(AuthenticationUtil.getFullyAuthenticatedUser());
        NodeRef noteNodeRef = noteService.createNote(note);

        note.setNodeRef(noteNodeRef);
        res.setContentEncoding("UTF-8");
        WebScriptUtils.writeJson(note, res);
    }

    @Override
    protected void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        noteService.deleteNote(nodeRef);
    }

    @Override
    protected void put(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        Note note = (Note) WebScriptUtils.readJson(Note.class, req);
        noteService.updateNote(note);
        res.setContentEncoding("UTF-8");
        WebScriptUtils.writeJson(note, res);
    }
}
