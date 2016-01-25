/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.openesdh.repo.services.parameters;

import static org.junit.Assert.*;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.model.OEParameter;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class OEParametersServiceImplIT {
    @Autowired
    private OEParametersService parametersService;
    @Autowired
    private TransactionRunner transactionRunner;

    @Test
    public void testGetOEParameters() {
        assertEquals("All parameters are added to OEParam and returned by service",
                OEParam.values().length, parametersService.getOEParameters().size());
    }

    @Test
    public void testGetOEParameter() {
        OEParameter oeParameter = parametersService.getOEParameter(OEParam.can_create_contacts.name());
        assertEquals("Gets requested parameter by name", OEParam.can_create_contacts.name(), oeParameter.getName());
        assertNotNull("Parameter has value", oeParameter.getValue());
    }

    @Test
    public void testSaveOEParameter() {
        String paramName = OEParam.can_create_contacts.name();
        OEParameter oeParameter = parametersService.getOEParameter(paramName);
        Boolean val = (Boolean) oeParameter.getValue();

        saveOEParameterIntransaction(oeParameter.getNodeRef(), oeParameter.getName(), !val);

        OEParameter oeParameterUpdated = parametersService.getOEParameter(paramName);
        assertNotNull("Has nodeRef after save", oeParameterUpdated.getNodeRef());
        assertEquals("Updates value correctlly", !val, oeParameterUpdated.getValue());
        saveOEParameterIntransaction(oeParameterUpdated.getNodeRef(), oeParameter.getName(), val);
    }

    private void saveOEParameterIntransaction(NodeRef nodeRef, String name, Object value) {
        transactionRunner.runInTransactionAsAdmin(() -> {
            parametersService.saveOEParameter(nodeRef, name, value);
            return null;
        });
    }

}
