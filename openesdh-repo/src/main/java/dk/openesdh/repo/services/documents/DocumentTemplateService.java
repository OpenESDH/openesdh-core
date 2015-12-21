package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.model.DocumentTemplateInfo;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * Created by Arnas on 21/09/15.
 */
public interface DocumentTemplateService {

    /**
     * get available document types
     *
     * @return
     */
    public List<DocumentTemplateInfo> findTemplates(String filter, int size);

    public DocumentTemplateInfo getTemplateInfo(NodeRef templateNodeRef);

}
