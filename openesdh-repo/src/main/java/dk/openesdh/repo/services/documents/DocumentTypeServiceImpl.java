package dk.openesdh.repo.services.documents;

import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.classification.ClassifierServiceImpl;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;

@Service("DocumentTypeService")
public class DocumentTypeServiceImpl extends ClassifierServiceImpl<DocumentType>
        implements DocumentTypeService {

    private static final String CANNOT_CHANGE_SYSTEM_NAME = "Can not change name of system document type.";

    private static final String CANNOT_DELETE_SYSTEM_OBJECT = "Cannot delete system document type.";

    public static final List<String> SYSTEM_TYPES = Arrays.asList("invoice", "letter");
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
        return OpenESDHModel.TYPE_DOC_TYPE;
    }

    @Override
    protected QName getClassifValueAssociationName() {
        return OpenESDHModel.ASSOC_DOC_TYPE;
    }

    @Override
    protected NodeRef getClassificatorValuesRootFolder() {
        return openESDHFoldersService.getDocumentTypesRootNodeRef();
    }

    @Override
    protected String getClassifierType() {
        return OpenESDHModel.STYPE_DOC_TYPE;
    }

    @Override
    public Class<? extends ClassifValue> getClassifValueClass() {
        return DocumentType.class;
    }

}
