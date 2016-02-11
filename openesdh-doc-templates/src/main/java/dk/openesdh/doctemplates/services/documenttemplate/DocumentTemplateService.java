package dk.openesdh.doctemplates.services.documenttemplate;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.doctemplates.model.DocumentTemplateInfo;

/**
 * Created by Arnas on 21/09/15.
 */
public interface DocumentTemplateService {

    /**
     * @param filter
     * @param size
     * @return
     */
    public List<DocumentTemplateInfo> findTemplates(String filter, int size);

    public DocumentTemplateInfo getTemplateInfo(NodeRef templateNodeRef);

}
