package dk.openesdh.repo.services.system;

import java.util.Optional;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("OpenESDHFoldersService")
public class OpenESDHFoldersServiceImpl implements OpenESDHFoldersService {

    @Autowired
    @Qualifier("repositoryHelper")
    private Repository repositoryHelper;
    @Autowired
    @Qualifier("NodeService")
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
    public NodeRef getSubsystemRootNodeRef() {
        return getFolder(getOpenESDHRootFolder(), SUBSYSTEM_ROOT);
    }

    @Override
    public NodeRef getClassificationsRootNodeRef() {
        return getFolder(getOpenESDHRootFolder(), CLASSIFICATIONS);
    }

    @Override
    public NodeRef getTemplatesRootNodeRef() {
        return getFolder(getSubsystemRootNodeRef(), DOCUMENT_TEMPLATES);
    }

    @Override
    public NodeRef getFilesRootNodeRef() {
        return getFolder(getOpenESDHRootFolder(), FILES_ROOT);
    }

    @Override
    public NodeRef getDocumentTypesRootNodeRef() {
        return getFolder(getClassificationsRootNodeRef(), DOCUMENT_TYPES);
    }

    @Override
    public NodeRef getDocumentCategoriesRootNodeRef() {
        return getFolder(getClassificationsRootNodeRef(), DOCUMENT_CATEGORIES);
    }

    @Override
    public NodeRef getParametersRootNodeRef() {
        return getFolder(getOpenESDHRootFolder(), PARAMETERS_ROOT);
    }

    @Override
    public NodeRef getFolder(NodeRef parent, String folderName) {
        NodeRef folderNodeRef = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, folderName);
        if (folderNodeRef == null) {
            return getByAssociationOrThrowError(parent, folderName);
        }
        return folderNodeRef;
    }
    
    @Override
    public Optional<NodeRef> getFolderOptional(NodeRef parent, String folderName){
        NodeRef folderNodeRef = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, folderName);
        if (folderNodeRef != null) {
            return Optional.of(folderNodeRef);
        }
        return getByAssociation(parent, folderName);
    }

    private NodeRef getByAssociationOrThrowError(NodeRef parent, String folderName) throws InvalidNodeRefException, AlfrescoRuntimeException {
        Optional<NodeRef> folder = getByAssociation(parent, folderName);
        if (folder.isPresent()) {
            return folder.get();
        }
        //Throw an exception. This should have been created on first boot along with the context root folder
        throw new AlfrescoRuntimeException("The \"" + folderName + "\" folder doesn't exist.");
    }
    
    private Optional<NodeRef> getByAssociation(NodeRef parent, String folderName){
        return nodeService.getChildAssocs(parent)
                .stream()
                .filter((ChildAssociationRef t) -> t.getQName().getLocalName().equals(folderName))
                .findFirst()
                .map(ChildAssociationRef::getChildRef);
    }
}
