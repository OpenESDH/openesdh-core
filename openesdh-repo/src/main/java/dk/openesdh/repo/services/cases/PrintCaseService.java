package dk.openesdh.repo.services.cases;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;

import dk.openesdh.repo.model.CasePrintInfo;
import fr.opensagres.xdocreport.core.XDocReportException;
import freemarker.template.TemplateModelException;

public interface PrintCaseService {

    Optional<InputStream> getCaseInfoPdfToPrint(String caseId, CasePrintInfo printInfo) throws IOException,
            XDocReportException, JSONException, TemplateModelException;

    List<InputStream> getDocumentsToPrint(CasePrintInfo printInfo);
}
