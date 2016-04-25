package dk.openesdh.repo.model;

import org.alfresco.service.cmr.repository.NodeRef;

public class CaseDocsFolder extends CaseFolderItem {

    public CaseDocsFolder() {
        setItemType(ITEM_TYPE_FOLDER);
    }

    public CaseDocsFolder(NodeRef nodeRef) {
        super(nodeRef);
        setItemType(ITEM_TYPE_FOLDER);
    }
}
