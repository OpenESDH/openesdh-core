package dk.openesdh.repo.services.system;

import org.alfresco.service.cmr.repository.NodeRef;

public interface OpenESDHFoldersService {

    public static String OPENESDH_ROOT_CONTEXT = "OpenESDH";
    public static String CASES_ROOT = "cases";
    public static String CASES_TYPES_ROOT = "types";
    public static String CLASSIFICATIONS = "classifications";
    public static String DOCUMENT_TYPES = "document_types";
    public static String DOCUMENT_TEMPLATES = "officeTemplates";
    public static String DOCUMENT_CATEGORIES = "document_categories";
    public static String SUBSYSTEM_ROOT = "subsystems";

    public NodeRef getOpenESDHRootFolder();

    public NodeRef getCasesRootNodeRef();

    public NodeRef getCasesTypeStorageRootNodeRef();

    public NodeRef getClassificationsRootNodeRef();

    public NodeRef getTemplatesRootNodeRef();

    public NodeRef getDocumentTypesRootNodeRef();

    public NodeRef getDocumentCategoriesRootNodeRef();

    public NodeRef getSubsystemRootNodeRef();
}
