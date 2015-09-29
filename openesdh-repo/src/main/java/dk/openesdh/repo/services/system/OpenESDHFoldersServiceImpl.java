package dk.openesdh.repo.services.system;

import java.util.Optional;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

public class OpenESDHFoldersServiceImpl implements OpenESDHFoldersService {

    private Repository repositoryHelper;
    private NodeService nodeService;

    @Override
    public NodeRef getOpenESDHRootFolder() {
        return getFolder(repositoryHelper.getCompanyHome(), OPENESDH_ROOT_CONTEXT);
    }

    @Override
    public NodeRef getCasesRootNodeRef() {
        return getFolder(getOpenESDHRootFolder(), CASES_ROOT);
    }

    @Override
    public NodeRef getCasesTypeStorageRootNodeRef() {
        return getFolder(getCasesRootNodeRef(), CASES_TYPES_ROOT);
    }

    @Override
    public NodeRef getClassificationsRootNodeRef() {
        return getFolder(getOpenESDHRootFolder(), CLASSIFICATIONS);
    }

    @Override
    public NodeRef getDocumentTypesRootNodeRef() {
        return getFolder(getClassificationsRootNodeRef(), DOCUMENT_TYPES);
    }

    @Override
    public NodeRef getDocumentCategoriesRootNodeRef() {
        return getFolder(getClassificationsRootNodeRef(), DOCUMENT_CATEGORIES);
    }

    private NodeRef getFolder(NodeRef parent, String folderName) {
        NodeRef folderNodeRef = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, folderName);
        if (folderNodeRef == null) {
            return getByAssociationOrThrowError(parent, folderName);
        }
        return folderNodeRef;
    }

    private NodeRef getByAssociationOrThrowError(NodeRef parent, String folderName) throws InvalidNodeRefException, AlfrescoRuntimeException {
        Optional<ChildAssociationRef> folder = nodeService.getChildAssocs(parent)
                .stream()
                .filter((ChildAssociationRef t) -> t.getQName().getLocalName().equals(folderName))
                .findFirst();
        if (folder.isPresent()) {
            return folder.get().getChildRef();
        }
        //Throw an exception. This should have been created on first boot along with the context root folder
        throw new AlfrescoRuntimeException("The \"" + folderName + "\" folder doesn't exist.");
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

}
