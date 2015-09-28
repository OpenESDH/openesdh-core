package dk.openesdh.repo.services.system;

import org.alfresco.service.cmr.repository.NodeRef;

public interface OpenESDHFoldersService {

    public static String OPENESDH_ROOT_CONTEXT = "OpenESDH";
    public static String CASES_ROOT = "cases";
    public static String CASES_TYPES_ROOT = "types";
    public static String CLASSIFICATIONS = "classifications";
    public static String DOCUMENT_TYPES = "document_types";

    public NodeRef getOpenESDHRootFolder();

    public NodeRef getCasesRootNodeRef();

    public NodeRef getCasesTypeStorageRootNodeRef();

    public NodeRef getClassificationsRootNodeRef();

    public NodeRef getDocumentTypesRootNodeRef();
}
