package dk.openesdh.repo.rootScopeExt;

import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptUtils;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.nodelocator.*;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lanre Abiwon.
 */
public class UtilsExt extends ScriptUtils {

    /** Services */
    protected ServiceRegistry services;
    private NodeService unprotNodeService;
    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setServices(ServiceRegistry services) {
        this.services = services;
    }

    public void setNodeService(NodeService unprotNodeService) {
        this.unprotNodeService = unprotNodeService;
    }

    /**
     * Use the Node Locator Service to find the a node reference from a number of possible locator types.
     * This method is responsible for determining the locator type and then calling the Service as the
     * Service does not know how to guess which locator to use.
     * <p/>
     * This service supports 'virtual' nodes including the following:
     * <p/>
     * alfresco://company/home      The Company Home root node<br>
     * alfresco://user/home         The User Home node under Company Home<br>
     * alfresco://company/shared    The Shared node under Company Home<br>
     * alfresco://sites/home        The Sites home node under Company Home<br>
     * workspace://.../...          Any standard NodeRef<br>
     * /app:company_home/cm:...     XPath QName style node reference<br>
     *
     * @param reference The node reference - See above for list of possible node references supported.
     * @return ScriptNode representing the node or null if not found
     */
    @Override
    public ScriptNode resolveNodeReference(final String reference) {
        if (reference == null) {
            throw new IllegalArgumentException("Node 'reference' argument is mandatory.");
        }

        final NodeLocatorService locatorService = this.services.getNodeLocatorService();

        NodeRef nodeRef = null;

        switch (reference) {
            case "alfresco://company/home":
                nodeRef = locatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
                break;
            case "alfresco://user/home":
                nodeRef = locatorService.getNode(UserHomeNodeLocator.NAME, null, null);
                break;
            case "alfresco://company/shared":
                nodeRef = locatorService.getNode(SharedHomeNodeLocator.NAME, null, null);
                break;
            case "alfresco://sites/home":
                nodeRef = locatorService.getNode(SitesHomeNodeLocator.NAME, null, null);
                break;
            case "openesdh://cases/home":
                nodeRef = caseService.getCasesRootNodeRef();
                break;
            default:
                if (reference.indexOf("://") > 0) {
                    NodeRef ref = new NodeRef(reference);
                    if (this.services.getNodeService().exists(ref) &&
                            this.services.getPermissionService().hasPermission(ref, PermissionService.READ) == AccessStatus.ALLOWED) {
                        nodeRef = ref;
                    }
                } else if (reference.startsWith("/")) {
                    final Map<String, Serializable> params = new HashMap<>(1, 1.0f);
                    params.put(XPathNodeLocator.QUERY_KEY, reference);
                    nodeRef = locatorService.getNode(XPathNodeLocator.NAME, null, params);
                }
                break;
        }

        return nodeRef != null ? (ScriptNode) new ValueConverter().convertValueForScript(this.services, getScope(), null, nodeRef) : null;
    }
}
