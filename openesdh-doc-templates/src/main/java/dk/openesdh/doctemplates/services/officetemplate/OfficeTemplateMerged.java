package dk.openesdh.doctemplates.services.officetemplate;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;

public class OfficeTemplateMerged {

    private String fileName;
    private String mimetype;
    private byte[] content;
    private DocumentType documentType;
    private DocumentCategory documentCategory;

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

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public DocumentCategory getDocumentCategory() {
        return documentCategory;
    }

    public void setDocumentCategory(DocumentCategory documentCategory) {
        this.documentCategory = documentCategory;
    }

}
