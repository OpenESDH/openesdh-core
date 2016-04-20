package dk.openesdh.repo.services.files;

import java.io.InputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;

public interface OeAuthorityFilesService {

    public List<JSONObject> getFiles(String authorityName);

    public NodeRef addFile(NodeRef owner, String fileName, String mimetype, InputStream fileInputStream, String comment);

    public void move(NodeRef file, NodeRef newOwner, String comment);
}
