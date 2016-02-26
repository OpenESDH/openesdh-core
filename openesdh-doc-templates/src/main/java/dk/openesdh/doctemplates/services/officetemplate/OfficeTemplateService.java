package dk.openesdh.doctemplates.services.officetemplate;

import java.io.InputStream;
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
     * save template
     *
     * @param title
     * @param description
     * @param fileName
     * @param contentInputStream
     * @param mimetype
     * @return
     */
    NodeRef saveTemplate(String title, String description, String fileName, InputStream contentInputStream, String mimetype);

    /**
     * delete template
     *
     * @param nodeRef
     */
    void deleteTemplate(NodeRef nodeRef);

    /**
     * Get the available templates for the current user.
     *
     * @return
     */
    List<OfficeTemplate> getTemplates();

    /**
     * Get the template including detailed information about its fields.
     *
     * @param templateNodeRef
     * @return
     */
    OfficeTemplate getTemplate(NodeRef templateNodeRef);

    /**
     * Render the template, given the map of fields/values and saved case/user/recipient values
     *
     * @param templateNodeRef
     * @param caseId
     * @param receiver
     * @param model
     * @return
     * @throws java.lang.Exception
     */
    ContentReader renderTemplate(NodeRef templateNodeRef, String caseId, NodeRef receiver, Map<String, Serializable> model) throws Exception;
}
