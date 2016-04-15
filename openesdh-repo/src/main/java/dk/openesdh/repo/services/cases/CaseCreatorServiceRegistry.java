package dk.openesdh.repo.services.cases;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This is a Strategy pattern implementation to distinguish between case creator services. 
 * CaseService is used if no other case creator service is registered (e.g. CaseTemplateService).
 * 
 * @author rudinjur
 *
 */
@Service("CaseCreatorServiceRegistry")
public class CaseCreatorServiceRegistry {

    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;
    
    private Map<Predicate<ChildAssociationRef>, Consumer<ChildAssociationRef>> registry = new HashMap<>();
    
    /**
     * Registers case creator service with a predicate to filter.
     * 
     * @param predicate
     *            used to determine whether the service can perform case creation.
     * @param caseCreatorService
     */
    public void registerCaseCreatorService(Predicate<ChildAssociationRef> predicate, Consumer<ChildAssociationRef> caseCreatorService){
        registry.put(predicate, caseCreatorService);        
    }

    /**
     * Retrieves case creator service by provided case association object.
     * 
     * @param assoc
     * @return
     */
    public Consumer<ChildAssociationRef> getCaseCreatorService(ChildAssociationRef assoc){
        return registry.entrySet().stream()
                .filter(entry -> entry.getKey().test(assoc))
                .map(entry -> entry.getValue())
                .findAny()
                .orElse(caseService::createCase); 
    }
}
