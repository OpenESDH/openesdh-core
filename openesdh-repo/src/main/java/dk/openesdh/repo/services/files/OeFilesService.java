package dk.openesdh.repo.services.files;

import java.io.InputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;

public interface OeFilesService {

    public JSONObject getFile(NodeRef nodeRef);

    public List<JSONObject> getFiles(NodeRef nodeRef);

    public NodeRef addFile(NodeRef parent, String fileName, String mimetype, InputStream fileInputStream, String comment);

    public void delete(NodeRef nodeRef);

    public void addToCase(String caseId, NodeRef file, String title, NodeRef docType, NodeRef docCategory, String description);

}
