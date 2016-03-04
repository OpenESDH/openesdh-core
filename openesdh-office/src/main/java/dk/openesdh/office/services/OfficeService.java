package dk.openesdh.office.services;

import org.alfresco.service.cmr.repository.NodeRef;

public interface OfficeService {

    public NodeRef createEmailDocument(String caseId, String name, String bodyText);
}
