package dk.openesdh.repo.services;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BehaviourFilterService {

    @Autowired
    @Qualifier("policyBehaviourFilter")
    private BehaviourFilter behaviourFilter;

    public interface Executable {

        void execute();
    }

    /**
     * disables behaviours on node for execution
     *
     * @param nodeRef
     * @param action
     */
    public void executeWithoutBehavior(NodeRef nodeRef, Executable action) {
        try {
            behaviourFilter.disableBehaviour(nodeRef);
            action.execute();
        } finally {
            behaviourFilter.enableBehaviour(nodeRef);
        }
    }

    public void executeWithoutBehavior(Executable action) {
        try {
            behaviourFilter.disableBehaviour();
            action.execute();
        } finally {
            behaviourFilter.enableBehaviour();
        }
    }
}
