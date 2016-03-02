package dk.openesdh.doctemplates.api.services;

import dk.openesdh.doctemplates.api.model.OfficeTemplate;
import dk.openesdh.doctemplates.api.model.OfficeTemplateMerged;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;

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
     * @param docType
     * @param docCategory
     * @param fileName
     * @param contentInputStream
     * @param mimetype
     * @return
     */
    NodeRef saveTemplate(String title, String description, NodeRef docType, NodeRef docCategory,
            String fileName, InputStream contentInputStream, String mimetype);

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
     * Get the template including detailed information about its fields.
     *
     * @param templateNodeRef
     * @param withFields - read fields of template
     * @param skipAutoFilledfields - skip fields that will be filled automatically
     * @return
     */
    OfficeTemplate getTemplate(NodeRef templateNodeRef, boolean withFields, boolean skipAutoFilledfields);

    /**
     * Render the template, given the map of fields/values and saved case/user/recipient values
     *
     * @param template
     * @param caseId
     * @param receiver
     * @param model
     * @return
     * @throws java.lang.Exception
     */
    OfficeTemplateMerged renderTemplate(OfficeTemplate template, String caseId, NodeRef receiver, Map<String, Serializable> model) throws Exception;

    /**
     * saves filled templates to case
     *
     * @param caseId
     * @param merged
     * @return
     */
    void saveToCase(String caseId, List<OfficeTemplateMerged> merged);

    /**
     * sends filled templates to recipients by email
     *
     * @param caseId
     * @param merged
     * @param subject
     * @param message
     */
    void sendToEmail(String caseId, List<OfficeTemplateMerged> merged, String subject, String message);

    /**
     *
     * @param templateNodeRef
     * @param caseId
     * @param json
     * @return
     * @throws Exception
     */
    List<OfficeTemplateMerged> getMergedTemplates(NodeRef templateNodeRef, String caseId, JSONObject json) throws Exception;
}
