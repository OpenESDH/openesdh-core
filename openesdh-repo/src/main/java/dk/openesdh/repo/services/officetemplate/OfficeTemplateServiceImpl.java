package dk.openesdh.repo.services.officetemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.dom.element.text.TextUserFieldDeclElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

/**
 * Created by syastrov on 9/23/15.
 */
@Service("OfficeTemplateService")
public class OfficeTemplateServiceImpl implements OfficeTemplateService {

    private static final Logger LOGGER = Logger.getLogger(OfficeTemplateServiceImpl.class);
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
    public NodeRef getTemplateDirectory() {
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
            LOGGER.error("\n=====> OpenESDH Error (" + errorLocator + ") <=====\n\t\t");
            LOGGER.error("Attempting to locate document template root resulted in the following error:\n" + e.getMessage() + "\n\n");
            throw new AlfrescoRuntimeException("Unable to locate the folder where templates are stored.\nPlease contact your administrator and pass on the code: " + errorLocator);
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

        //The option is needed for transformation to work on windows platform.
        //Seems like OpenOffice on windows requires file extension to be present.
        //The OOoContentTransformer uses sourceNodeRef to retrieve file name with extension.
        TransformationOptions options = new TransformationOptions();
        options.setSourceNodeRef(templateNodeRef);

        InputStream inputStream = templateReader.getContentInputStream();

        Map<String, Serializable> model = new HashMap<>();
        model.putAll(values);

        File file = File.createTempFile("office-template-renderer-", null);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            renderODFTemplate(inputStream, outputStream, model);

            ContentReader reader = new FileContentReader(file);
            reader.setMimetype(mimetypeService.guessMimetype(null, templateReader));
            ContentReader transformedReader = transformContent(reader, DEFAULT_TARGET_MIME_TYPE, options);
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
                    LOGGER.warn("Supposed to fill in field " + fieldName
                            + " but it does not exist in the template");
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
            LOGGER.error("Transformer to " + targetMimetype + " unavailable for "
                    + sourceReader.getMimetype());
            return null;
        }

        // Transform the document to PDF
        try {
            contentService.transform(sourceReader, writer, options);
        } catch (ContentIOException e) {
            LOGGER.error("Exception during transformation to " + targetMimetype, e);
            return null;
        }

        return writer.getReader();
    }
}
