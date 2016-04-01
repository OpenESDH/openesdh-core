package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.CasePrintInfo;
import dk.openesdh.repo.services.cases.PrintCaseService;
import dk.openesdh.repo.webscripts.WebScriptParams;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import fr.opensagres.xdocreport.core.XDocReportException;
import freemarker.template.TemplateModelException;

@Component
@WebScript(description = "Prints case and selected documents into PDF file.", families = "Case Tools")
public class CasePrintWebScript {

    @Autowired
    private PrintCaseService printCaseService;

    @Attribute
    public CasePrintInfo getPrintInfo(WebScriptRequest req) throws IOException {
        return (CasePrintInfo) WebScriptUtils.readJson(CasePrintInfo.class, req);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/print", method = HttpMethod.POST, defaultFormat = "application/pdf")
    public Resolution printCase(
            @UriVariable(WebScriptParams.CASE_ID) final String caseId,
            @Attribute CasePrintInfo printInfo) throws JSONException, XDocReportException, IOException, COSVisitorException, TemplateModelException {

        PDFMergerUtility mergeUtility = new PDFMergerUtility();
        
        printCaseService.getCaseInfoPdfToPrint(caseId, printInfo)
            .ifPresent(caseInfo -> mergeUtility.addSource(caseInfo));
        
        mergeUtility.addSources(printCaseService.getDocumentsToPrint(printInfo));

        return (request, response, params) -> {
            response.setContentType(MimetypeMap.MIMETYPE_PDF);
            response.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
            mergeUtility.setDestinationStream(response.getOutputStream());
            mergeUtility.mergeDocuments();
        };
    }
}
