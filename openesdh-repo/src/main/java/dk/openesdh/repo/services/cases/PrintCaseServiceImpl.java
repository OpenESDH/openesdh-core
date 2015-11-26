package dk.openesdh.repo.services.cases;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.CasePrintInfo;
import dk.openesdh.repo.services.audit.AuditSearchService;
import dk.openesdh.repo.services.notes.NoteService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

@Service
public class PrintCaseServiceImpl implements PrintCaseService {

    private static final String CASE_PRINT_TEMPLATE_PATH = WebScriptUtils.WEBSCRIPT_TEMPLATES_FOLDER_PATH
            + "/dk/openesdh/case/casePrint.odt";

    @Autowired
    private CaseService caseService;

    @Autowired
    private CaseOwnersService caseOwnersService;

    @Autowired
    private AuditSearchService auditSearchService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private RenditionService renditionService;

    public Optional<InputStream> getCaseInfoPdfToPrint(String caseId, CasePrintInfo printInfo) throws IOException,
            XDocReportException, JSONException, TemplateModelException {

        if (!(printInfo.isCaseDetails() || printInfo.isCaseHistoryLog() || printInfo.isComments())) {
            return Optional.empty();
        }

        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        try (InputStream casePrintTemplate = getCasePrintOdtTemplate()) {

            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(casePrintTemplate,
                    TemplateEngineKind.Freemarker);

            IContext context = report.createContext();
            context.put("printInfo", printInfo);

            TemplateHashModel i18n = (TemplateHashModel) BeansWrapper.getDefaultInstance().getStaticModels()
                    .get("org.springframework.extensions.surf.util.I18NUtil");
            context.put("i18n", i18n);

            if (printInfo.isCaseDetails()) {
                context.put("case", getCaseInfoPropertiesJson(caseNodeRef));
            }

            if (printInfo.isCaseHistoryLog()) {
                JSONArray caseHistory = auditSearchService.getAuditLogByCaseNodeRef(caseNodeRef, 1000);
                context.put("caseHistory", caseHistory);

                FieldsMetadata metadata = new FieldsMetadata();
                metadata.addFieldAsList("caseHistory.action");
                metadata.addFieldAsList("caseHistory.type");
                metadata.addFieldAsList("caseHistory.user");
                metadata.addFieldAsList("caseHistory.time");
                report.setFieldsMetadata(metadata);
            }

            if (printInfo.isComments()) {
                context.put("notes", noteService.getNotes(caseNodeRef));
            }

            Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM);
            ContentWriter writer = contentService.getTempWriter();
            report.convert(context, options, writer.getContentOutputStream());

            return Optional.of(writer.getReader().getContentInputStream());
        }
    }

    private JSONObject getCaseInfoPropertiesJson(NodeRef caseNodeRef) throws JSONException {
        JSONObject properties = (JSONObject) caseService.getCaseInfoJson(caseNodeRef).get("properties");
        properties.put("owners", caseOwnersService.getCaseOwners(caseNodeRef));
        return properties;
    }

    public List<InputStream> getDocumentsToPrint(CasePrintInfo printInfo) {
        return printInfo.getDocuments()
                .stream()
                .map(docNodeRef -> getDocumentPdfThumbnailStream(docNodeRef))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private InputStream getCasePrintOdtTemplate() {
        return getClass().getClassLoader().getResourceAsStream(CASE_PRINT_TEMPLATE_PATH);
    }

    private Optional<InputStream> getDocumentPdfThumbnailStream(NodeRef documentNodeRef) {

        Optional<ContentReader> pdfContentOpt = renditionService.getRenditions(documentNodeRef).stream()
                .map(assoc -> getContentReader(assoc.getChildRef()))
                .filter(contentReader -> contentReader.getMimetype().startsWith(MimetypeMap.MIMETYPE_PDF))
                .findFirst();

        if (!pdfContentOpt.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(pdfContentOpt.get().getContentInputStream());
    }

    private ContentReader getContentReader(NodeRef nodeRef) {
        return contentService.getReader(nodeRef, getContentProperty(nodeRef));
    }

    private QName getContentProperty(NodeRef nodeRef) {
        Serializable contentPropertyName = nodeService
                .getProperty(nodeRef, ContentModel.PROP_CONTENT_PROPERTY_NAME);
        return contentPropertyName != null ? (QName) contentPropertyName : ContentModel.PROP_CONTENT;
    }
}
