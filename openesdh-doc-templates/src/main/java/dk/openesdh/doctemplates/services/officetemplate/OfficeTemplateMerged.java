package dk.openesdh.doctemplates.services.officetemplate;

import org.alfresco.service.cmr.repository.NodeRef;

public class OfficeTemplateMerged {

    private String fileName;
    private String mimetype;
    private byte[] content;
    private NodeRef documentType;
    private NodeRef documentCategory;
    private NodeRef receiver;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public NodeRef getDocumentType() {
        return documentType;
    }

    public void setDocumentType(NodeRef documentType) {
        this.documentType = documentType;
    }

    public NodeRef getDocumentCategory() {
        return documentCategory;
    }

    public void setDocumentCategory(NodeRef documentCategory) {
        this.documentCategory = documentCategory;
    }

    public NodeRef getReceiver() {
        return receiver;
    }

    public void setReceiver(NodeRef receiver) {
        this.receiver = receiver;
    }
}
