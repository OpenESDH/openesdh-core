package dk.openesdh.repo.services.authorities;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class UserSavingContext {

    private NodeRef nodeRef;
    private String userName;
    private boolean accountEnabled;
    private Map<QName, Serializable> props;

    private Map<QName, Serializable> copiedProps;

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isAccountEnabled() {
        return accountEnabled;
    }

    public void setAccountEnabled(boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public Map<QName, Serializable> getProps() {
        return props;
    }

    public void setProps(Map<QName, Serializable> props) {
        this.props = props;
    }

    public Map<QName, Serializable> getCopiedProps() {
        return copiedProps;
    }

    public void setCopiedProps(Map<QName, Serializable> copiedProps) {
        this.copiedProps = copiedProps;
    }

}
