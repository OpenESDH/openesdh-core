package dk.openesdh.doctemplates.services.officetemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.dom.element.text.TextUserFieldDeclElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

/**
 * Created by syastrov on 9/23/15.
 */
@Service("OfficeTemplateService")
public class OfficeTemplateServiceImpl implements OfficeTemplateService {

    private final Logger logger = LoggerFactory.getLogger(OfficeTemplateService.class);
    private static final String DEFAULT_TARGET_MIME_TYPE = MimetypeMap.MIMETYPE_PDF;

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("FileFolderService")
    private FileFolderService fileFolderService;
    @Autowired
    @Qualifier("repositoryHelper")
    private Repository repositoryHelper;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;
    @Autowired
    @Qualifier("mimetypeService")
    private MimetypeService mimetypeService;

    @Override
    public NodeRef saveTemplate(String title, String description, String filename, InputStream contentInputStream, String mimetype) {
        NodeRef templateStorageDir = getTemplateDirectory();
        FileInfo fileInfo = fileFolderService.create(templateStorageDir, filename, ContentModel.TYPE_CONTENT);
        ContentWriter writer = contentService.getWriter(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.setEncoding("UTF-8");
        writer.putContent(contentInputStream);

        nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_TITLE, title);
        if (StringUtils.isNotBlank(description)) {
            nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_DESCRIPTION, description);
        }
        return fileInfo.getNodeRef();
    }

    @Override
    public void deleteTemplate(NodeRef nodeRef) {
        nodeService.deleteNode(nodeRef);
    }

    /**
     * Returns the a nodeRef representing the templates directory root.
     *
     * @return
     */
    private NodeRef getTemplateDirectory() {
        NodeRef rootNode = repositoryHelper.getCompanyHome();
        try {
            FileInfo folderInfo = fileFolderService.resolveNamePath(rootNode, Arrays.asList(OPENESDH_DOC_TEMPLATES_DEFAULT_PATH.split("/")));
            return folderInfo.getNodeRef();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to locate the folder where templates are stored.", e);
        }
    }

    @Override
    public List<OfficeTemplate> getTemplates() {
        NodeRef templateDirNode = getTemplateDirectory();
        return fileFolderService.listFiles(templateDirNode).stream()
                .map(fileInfo -> getTemplate(fileInfo.getNodeRef(), false))
                .collect(Collectors.toList());
    }

    @Override
    public OfficeTemplate getTemplate(NodeRef templateNodeRef) {
        return getTemplate(templateNodeRef, true);
    }

    OfficeTemplate getTemplate(NodeRef templateNodeRef, boolean withFields) {
        Map<QName, Serializable> properties = nodeService.getProperties(templateNodeRef);
        OfficeTemplate template = new OfficeTemplate();
        template.setName((String) properties.get(ContentModel.PROP_NAME));
        template.setTitle((String) properties.get(ContentModel.PROP_TITLE));
        template.setDescription((String) properties.get(ContentModel.PROP_DESCRIPTION));
        template.setNodeRef(templateNodeRef.toString());
        if (withFields) {
            try {
                List<OfficeTemplateField> templateFields = getTemplateFields(templateNodeRef);
                template.setFields(templateFields);
            } catch (Exception ex) {
                throw new RuntimeException("Cannot read template \"" + template.getName() + "\" fields", ex);
            }
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
    public ContentReader renderTemplate(NodeRef templateNodeRef, Map<String, Serializable> model) throws Exception {
        ContentReader templateReader = contentService.getReader(templateNodeRef, ContentModel.PROP_CONTENT);

        //The option is needed for transformation to work on windows platform.
        //Seems like OpenOffice on windows requires file extension to be present.
        //The OOoContentTransformer uses sourceNodeRef to retrieve file name with extension.
        TransformationOptions options = new TransformationOptions();
        options.setSourceNodeRef(templateNodeRef);

        InputStream inputStream = templateReader.getContentInputStream();

        File file = File.createTempFile("office-template-renderer-", null);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            renderODFTemplate(inputStream, outputStream, model);

            ContentReader reader = new FileContentReader(file);
            reader.setMimetype(mimetypeService.guessMimetype(null, templateReader));
            ContentReader transformedReader = transformContent(reader, DEFAULT_TARGET_MIME_TYPE, options);
            reader.setMimetype(DEFAULT_TARGET_MIME_TYPE);
            return transformedReader;
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
                    logger.info("Supposed to fill in field \"{}\" but it does not exist in the template", fieldName);
                }
            }
        });
        doc.save(outputStream);
    }

    private ContentReader transformContent(ContentReader sourceReader, String targetMimetype,
            TransformationOptions options) {
        // Create a temporary writer
        ContentWriter writer = contentService.getTempWriter();
        writer.setMimetype(DEFAULT_TARGET_MIME_TYPE);

        ContentTransformer transformer = contentService.getTransformer(sourceReader.getContentUrl(),
                sourceReader.getMimetype(), sourceReader.getSize(), targetMimetype, options);

        if (transformer == null) {
            throw new RuntimeException("Transformer to " + targetMimetype + " unavailable for "
                    + sourceReader.getMimetype());
        }
        contentService.transform(sourceReader, writer, options);
        return writer.getReader();
    }
}
