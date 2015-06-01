package dk.openesdh.repo.classification.sync;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by syastrov on 6/1/15.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class KLEClassificationSynchronizerIT {

    @Autowired
    @Qualifier("classificationSynchronizer")
    protected ClassificationSynchronizer classificationSynchronizer;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
    }

    @Test
    public void testSynchronize() throws Exception {
        // TODO: Test
        classificationSynchronizer.synchronize();
    }
}