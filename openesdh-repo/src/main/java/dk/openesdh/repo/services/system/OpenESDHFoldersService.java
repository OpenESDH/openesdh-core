package dk.openesdh.repo.services.system;

import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;

public interface OpenESDHFoldersService {

    public static String OPENESDH_ROOT_CONTEXT = "OpenESDH";
    public static String CASES_ROOT = "cases";
    public static String CASES_ROOT_PATH = "/app:company_home/oe:OpenESDH/oe:cases/";
    public static String FILES_ROOT_PATH = "/app:company_home/oe:OpenESDH/oe:files/";
    public static String CASES_TYPES_ROOT = "types";
    public static String CLASSIFICATIONS = "classifications";
    public static String DOCUMENT_TYPES = "document_types";
    public static String DOCUMENT_TEMPLATES = "officeTemplates";
    public static String DOCUMENT_CATEGORIES = "document_categories";
    public static String SUBSYSTEM_ROOT = "subsystems";
    public static String PARAMETERS_ROOT = "parameters";
    public static String FILES_ROOT = "files";
    public static String SITES_PATH_ROOT = "/Company Home/Sites";
    public static String OPENE_SITE_CASES_PATH = "/Company Home/Sites/opene/cases";
    public static String TENANTS_MODULES_MAP_XPATH = "/app:company_home/oe:OpenESDH/oe:tenants/cm:tenant-modules-map.json";

    public NodeRef getOpenESDHRootFolder();

    public NodeRef getCasesRootNodeRef();

    public NodeRef getCasesTypeStorageRootNodeRef();

    public NodeRef getClassificationsRootNodeRef();

    public NodeRef getTemplatesRootNodeRef();

    public NodeRef getFilesRootNodeRef();

    public NodeRef getDocumentTypesRootNodeRef();

    public NodeRef getDocumentCategoriesRootNodeRef();

    public NodeRef getSubsystemRootNodeRef();

    public NodeRef getParametersRootNodeRef();

    NodeRef getFolder(NodeRef parent, String folderName);

    Optional<NodeRef> getFolderOptional(NodeRef parent, String folderName);
}
