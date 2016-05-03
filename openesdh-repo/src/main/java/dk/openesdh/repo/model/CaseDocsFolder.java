package dk.openesdh.repo.model;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class CaseDocsFolder extends CaseFolderItem {

    private static final long serialVersionUID = 1L;

    public static final String FIELD_CHILDREN = "children";

    private List<CaseFolderItem> children = new ArrayList<>();

    public CaseDocsFolder() {
        setItemType(ITEM_TYPE_FOLDER);
    }

    public CaseDocsFolder(NodeRef nodeRef) {
        super(nodeRef);
        setItemType(ITEM_TYPE_FOLDER);
    }

    public List<CaseFolderItem> getChildren() {
        return children;
    }

    public void setChildren(List<CaseFolderItem> children) {
        this.children = children;
    }

}
