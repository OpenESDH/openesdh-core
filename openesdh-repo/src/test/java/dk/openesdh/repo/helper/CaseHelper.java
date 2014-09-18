package dk.openesdh.repo.helper;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ole on 18/08/14.
 */
public class CaseHelper {

    /**
     * Create a case. If disableBehaviour is true, transaction is run with
     * behaviours disabled.
     * when creating the case.
     * @param nodeService
     * @param retryingTransactionHelper
     * @param username
     * @param parent
     * @param cmName
     * @param caseType
     * @param properties
     * @param owners
     * @param disableBehaviour
     * @return
     */
    public static NodeRef createCase(final NodeService nodeService,
                              final RetryingTransactionHelper
                                      retryingTransactionHelper,
                              String username,
                              final NodeRef parent,
                              final String cmName,
                              final QName caseType,
                              final Map<QName, Serializable> properties,
                              final List<NodeRef> owners,
                              boolean disableBehaviour) {

        AuthenticationUtil.setFullyAuthenticatedUser(username);
        // We have to do in a transaction because we must set the case:owner
        // association before commit, to avoid an integrity error.
        BehaviourFilter behaviourFilter = null;
        if (disableBehaviour) {
            behaviourFilter = (BehaviourFilter)
                    ApplicationContextHelper.getApplicationContext().getBean("policyBehaviourFilter");
        }
        final BehaviourFilter finalBehaviourFilter = behaviourFilter;
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
            @Override
            public NodeRef execute() throws Throwable {
                if (finalBehaviourFilter != null) {
                    // Disable behaviour for txn
                    finalBehaviourFilter.disableBehaviour();
                }

                properties.put(ContentModel.PROP_NAME, cmName);

                // Create test case
                ChildAssociationRef childAssoc = nodeService.createNode(
                        parent,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, cmName),
                        caseType,
                        properties
                );

                nodeService.setAssociations(childAssoc.getChildRef(),
                        OpenESDHModel.ASSOC_CASE_OWNERS, owners);

                if (finalBehaviourFilter != null) {
                    // Re-enable behaviour
                    finalBehaviourFilter.enableBehaviour();
                }
                return childAssoc.getChildRef();
            }
        });
    }

    /**
     * Create a case without disabling the behaviour.
     * @param nodeService
     * @param retryingTransactionHelper
     * @param username
     * @param parent
     * @param cmName
     * @param caseType
     * @param properties
     * @param owners
     * @return
     */
    public static NodeRef createCase(NodeService nodeService,
                              RetryingTransactionHelper
                                      retryingTransactionHelper,
                              String username,
                              NodeRef parent,
                              String cmName,
                              QName caseType,
                              Map<QName, Serializable> properties,
                              List<NodeRef> owners) {
        return createCase(nodeService, retryingTransactionHelper, username,
                parent, cmName, caseType, properties, owners, false);
    }
}
