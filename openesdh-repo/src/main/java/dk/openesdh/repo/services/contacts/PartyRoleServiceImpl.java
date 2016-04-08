package dk.openesdh.repo.services.contacts;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.classification.ClassificatorManagementServiceImpl;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;

@Service("PartyRoleService")
public class PartyRoleServiceImpl extends ClassificatorManagementServiceImpl {

    private static final String CANNOT_CHANGE_SYSTEM_NAME = "Can not change name of system party role.";

    private static final String CANNOT_DELETE_SYSTEM_OBJECT = "Cannot delete system party role.";

    @Autowired
    @Qualifier("OpenESDHFoldersService")
    private OpenESDHFoldersService openESDHFoldersService;

    @Override
    protected String getCannotDeleteSystemMessage() {
        return CANNOT_DELETE_SYSTEM_OBJECT;
    }

    @Override
    protected String getCannotChangeNameMessage() {
        return CANNOT_CHANGE_SYSTEM_NAME;
    }

    @Override
    protected QName getClassifValueType() {
        return OpenESDHModel.TYPE_CONTACT_PARTY_ROLE;
    }

    @Override
    protected QName getClassifValueAssociationName() {
        return OpenESDHModel.ASSOC_CONTACT_PARTY_ROLE;
    }

    @Override
    protected NodeRef getClassificatorValuesRootFolder() {
        return openESDHFoldersService.getPartyRolesRootNodeRef();
    }

}
