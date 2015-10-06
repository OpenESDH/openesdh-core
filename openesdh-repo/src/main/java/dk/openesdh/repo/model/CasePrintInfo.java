package dk.openesdh.repo.model;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class CasePrintInfo {

    private boolean caseDetails;
    private boolean caseHistoryLog;
    private boolean comments;
    private List<NodeRef> documents = new ArrayList<NodeRef>();

    public boolean isCaseDetails() {
        return caseDetails;
    }

    public void setCaseDetails(boolean caseDetails) {
        this.caseDetails = caseDetails;
    }

    public boolean isCaseHistoryLog() {
        return caseHistoryLog;
    }

    public void setCaseHistoryLog(boolean caseHistoryLog) {
        this.caseHistoryLog = caseHistoryLog;
    }

    public boolean isComments() {
        return comments;
    }

    public void setComments(boolean comments) {
        this.comments = comments;
    }

    public List<NodeRef> getDocuments() {
        return documents;
    }

    public void setDocuments(List<NodeRef> documents) {
        this.documents = documents;
    }

}
