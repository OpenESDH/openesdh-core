package dk.openesdh.repo.services.officetemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by syastrov on 9/23/15.
 */
public interface OfficeTemplateService {

    /**
     * The default path for where the templates are to be located.
     */
    String OPENESDH_DOC_TEMPLATES_DEFAULT_PATH = "OpenESDH/subsystems/officeTemplates";

    /**
     * Returns the a nodeRef representing the templates directory root.
     * @return
     * @throws org.alfresco.error.AlfrescoRuntimeException
     */
    NodeRef getTemplateDirectory();

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
