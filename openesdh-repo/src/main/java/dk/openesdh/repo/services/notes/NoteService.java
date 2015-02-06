package dk.openesdh.repo.services.notes;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by syastrov on 2/6/15.
 */
public interface NoteService {
    public NodeRef createNote(NodeRef nodeRef, String content, String author);

    public void updateNote(NodeRef nodeRef, String content, String author);

    List<NodeRef> getNotes(NodeRef nodeRef);
}
