package dk.openesdh.repo.services.officetemplate;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.dom.element.text.TextUserFieldDeclElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;
import org.springframework.util.StringUtils;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by syastrov on 9/23/15.
 */
public class OfficeTemplateServiceImpl implements OfficeTemplateService {
    private static Logger LOGGER = Logger.getLogger(OfficeTemplateServiceImpl.class);

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private Repository repositoryHelper;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private Properties properties;


    private static final String DEFAULT_TARGET_MIME_TYPE = MimetypeMap.MIMETYPE_PDF;
    private List<String> templateMimeTypes;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void init() {
        // TODO: Behaviour for extracting fields from templates when they
        // are added
    }

    @Override
    public NodeRef getTemplateDirectory(){
        NodeRef rootNode = repositoryHelper.getCompanyHome();
        FileInfo folderInfo;
        try {
            folderInfo = fileFolderService.resolveNamePath(rootNode, Arrays.asList(OPENESDH_DOC_TEMPLATES_DEFAULT_PATH.split("/")));
            return folderInfo.getNodeRef();
        } catch (FileNotFoundException e) {
            //This should be bootstrapped by default, ideally we should create it if it doesn't exist but that would hide
            //The problem that there was something wrong with the bootstrapping so we throw an error here attached with
            //an error locator id number to help the admin locate the issue in the logs.
            String errorLocator = RandomStringUtils.randomAlphanumeric(10);
            LOGGER.error("\n=====> OpenESDH Error ("+errorLocator+") <=====\n\t\t");
            LOGGER.error("Attempting to locate document template root resulted in the following error:\n" + e.getMessage() + "\n\n");
            throw new AlfrescoRuntimeException("Unable to locate the folder where templates are stored.\nPlease contact your administrator and pass on the code: "+errorLocator);
        }
    }

    @Override
    public List<OfficeTemplate> getTemplates() {
        NodeRef templateDirNode = getTemplateDirectory();

        // TODO: Lookup templates recursively
        return fileFolderService.listFiles(templateDirNode).stream().map(fileInfo -> {
            try {
                return getTemplate(fileInfo.getNodeRef(), false);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public OfficeTemplate getTemplate(NodeRef templateNodeRef) throws Exception {
        return getTemplate(templateNodeRef, true);
    }

    public OfficeTemplate getTemplate(NodeRef templateNodeRef, boolean withFields) throws Exception {
        Map<QName, Serializable> properties = nodeService.getProperties(templateNodeRef);
        OfficeTemplate template = new OfficeTemplate();
        template.setName((String) properties.get(ContentModel.PROP_NAME));
        template.setTitle((String) properties.get(ContentModel.PROP_TITLE));
        template.setNodeRef(templateNodeRef.toString());

        if (withFields) {
            List<OfficeTemplateField> templateFields = getTemplateFields(templateNodeRef);
            template.setFields(templateFields);
        }
        return template;
    }

    private List<OfficeTemplateField> getTemplateFields(NodeRef templateNodeRef) throws Exception {
        List<OfficeTemplateField> templateFields = new ArrayList<>();
        ContentReader templateReader = contentService.getReader(templateNodeRef, ContentModel.PROP_CONTENT);
        InputStream inputStream = templateReader.getContentInputStream();
        OdfDocument doc = OdfDocument.loadDocument(inputStream);
        NodeList nodes = doc.getContentRoot().getElementsByTagName(TextUserFieldDeclElement.ELEMENT_NAME.getQName());
        for (int i = 0; i < nodes.getLength(); i++) {
            TextUserFieldDeclElement element = (TextUserFieldDeclElement) nodes.item(i);
            OfficeTemplateField field = new OfficeTemplateField();
            field.setName(element.getTextNameAttribute());
            field.setType(element.getOfficeValueTypeAttribute());
            // TODO: Implement field mappings. For now, the name is also the
            // mapping.
            field.setMappedFieldName(element.getTextNameAttribute());

            // TODO: Support getting values of different value types
            field.setValue(element.getOfficeStringValueAttribute());
            templateFields.add(field);
        }
        return templateFields;
    }

    @Override
    public ContentReader renderTemplate(NodeRef templateNodeRef, Map<String, Serializable> values) throws Exception {
        ContentReader templateReader = contentService.getReader(templateNodeRef, ContentModel.PROP_CONTENT);
        InputStream inputStream = templateReader.getContentInputStream();

        Map<String, Serializable> model = new HashMap<>();
        model.putAll(values);

        File file = File.createTempFile("office-template-renderer-", null);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            renderODFTemplate(inputStream, outputStream, model);

            ContentReader reader = new FileContentReader(file);
            reader.setMimetype(mimetypeService.guessMimetype(null, templateReader));
            ContentReader transformedReader = transformContent(reader, DEFAULT_TARGET_MIME_TYPE);
            reader.setMimetype(DEFAULT_TARGET_MIME_TYPE);
            if (transformedReader != null) {
                return transformedReader;
            } else {
                return null;
            }
        } finally {
            boolean delete = file.delete();
            if (!delete) {
                file.deleteOnExit();
            }
        }
    }

    private void renderODFTemplate(InputStream inputStream, OutputStream outputStream,
            Map<String, Serializable> model) throws Exception {
        TextDocument doc = TextDocument.loadDocument(inputStream);
        model.forEach((fieldName, value) -> {
            if (value != null) {
                VariableField variableField = doc.getVariableFieldByName(fieldName);
                if (variableField != null) {
                    variableField.updateField(value.toString(), null);
                } else {
                    LOGGER.warn("Supposed to fill in field " + fieldName +
                            " but it does not exist in the template");
                }
            }
        });
        doc.save(outputStream);
    }

    private ContentReader transformContent(ContentReader sourceReader, String targetMimetype) {
        // Create a temporary writer
        ContentWriter writer = contentService.getTempWriter();
        writer.setMimetype(DEFAULT_TARGET_MIME_TYPE);

        ContentTransformer transformer = contentService.getTransformer(
                sourceReader.getMimetype(), targetMimetype);

        if (transformer == null) {
            LOGGER.error("Transformer to " + targetMimetype + " unavailable for " +
                    sourceReader.getMimetype());
            return null;
        }

        // Transform the document to PDF
        try {
            transformer.transform(sourceReader, writer);
        } catch (ContentIOException e) {
            LOGGER.error("Exception during transformation to " + targetMimetype, e);
            return null;
        }

        return writer.getReader();
    }

    public void setTemplateMimeTypes(List<String> templateMimeTypes) {
        this.templateMimeTypes = templateMimeTypes;
    }
}
