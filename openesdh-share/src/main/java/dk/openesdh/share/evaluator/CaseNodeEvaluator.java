package dk.openesdh.share.evaluator;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.CredentialVault;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.Response;

import java.util.Map;

/**
 * Evaluates to true if the URI contains a nodeRef parameter which points to
 * a case.
 *
 * It's also possible to specify a uriPrefix parameter
 *
 * @author Seth Yastrov
 */
public class CaseNodeEvaluator extends URIPrefixEvaluator {
    public boolean applyModule(RequestContext context, Map<String, String> evaluationProperties) {
        if (!super.applyModule(context, evaluationProperties)) {
            return false;
        }
        boolean isCaseNode = false;

        try {
            CredentialVault cv = context.getCredentialVault();
            if (cv != null) {
                Credentials creds = cv.retrieve("alfresco");
                if (creds == null) {
                    // User is not logged in anymore
                    return false;
                }
                String userName = creds.getProperty("cleartextUsername").toString();
                Connector connector = context.getServiceRegistry().getConnectorService().getConnector("alfresco", userName, ServletUtil.getSession());
                String nodeRef = context.getParameter("nodeRef");
                Response res = connector.call("/api/openesdh/iscasenode?nodeRef=" + nodeRef);
                if (res.getStatus().getCode() == Status.STATUS_OK) {
                    isCaseNode = true;
                } else {
                    isCaseNode = false;
                }
            }
        } catch (ConnectorServiceException e) {
            e.printStackTrace();
        }
        return isCaseNode;
    }

    public String[] getRequiredProperties() {
        return new String[]{};
    }
}


