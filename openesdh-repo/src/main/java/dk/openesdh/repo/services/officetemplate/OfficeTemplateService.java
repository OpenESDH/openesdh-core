package dk.openesdh.repo.services.officetemplate;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by syastrov on 9/23/15.
 */
public interface OfficeTemplateService {
    /**
     * Get the available templates for the current user.
     * @return
     */
    List<OfficeTemplate> getTemplates();

    /**
     * Get the template including detailed information about its fields.
     * @param templateNodeRef
     * @return
     */
    OfficeTemplate getTemplate(NodeRef templateNodeRef) throws Exception;

    /**
     * Render the template, given the map of fields/values.
     * @param templateNodeRef
     * @param model
     * @return
     */
    ContentReader renderTemplate(NodeRef templateNodeRef, Map<String, Serializable> model) throws Exception;
}
