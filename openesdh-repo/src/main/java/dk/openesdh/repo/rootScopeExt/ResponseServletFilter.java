package dk.openesdh.repo.rootScopeExt;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.webdav.WebDAV;
import org.alfresco.repo.webdav.auth.AuthenticationDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter was created to prevent browsers rendering the basic auth dialog in response to a 401 message. 
 * For this to work please copy the filter and filter-mapping elements in src/main/amp/config/alfresco/filter-mapping.config.xml
 * to the web.xml in the deployed alfresco.
 * @author Lanre.
 */
//TODO find out if it is possible to have a maven profile that can inject the filters into the deployed web.xml file.
public class ResponseServletFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseServletFilter.class);

    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_NTLM = "NTLM";
    private static final String NO_AUTH_REQUIRED = "alfNoAuthRequired";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        filterNTLMRequestAuthorization((HttpServletRequest) request);

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper((HttpServletRequest) request) {

            /**
             * This is a fix for Chrome browser to be able to use NTLM SSO.
             */
            @Override
            public String getHeader(String name) {
                if (!WebDAV.HEADER_USER_AGENT.equalsIgnoreCase(name)) {
                    return super.getHeader(name);
                }
                String userAgent = super.getHeader(name);
                if (userAgent.indexOf("Chrome") != -1) {
                    return userAgent.replace("Safari", "Sfr");
                }
                return userAgent;
            }

        };

        HttpServletResponse wrappedResponse = new HttpServletResponseWrapper((HttpServletResponse) response) {

            private boolean isNtlmAuthentication = false;

            @Override
            public void sendError(int sc, String msg) throws IOException {
                super.sendError(sc, msg);
            }

            @Override
            public void sendError(int sc) throws IOException {
                super.sendError(sc);
            }

            @Override
            public void setStatus(int code) {
                super.setStatus(code);
                if(code == 401) changeAuthHeader();
            }

            @Override
            public void setStatus(int code, String sm) {
                super.setStatus(code, sm);
                if (code == 401)
                    changeAuthHeader();
            }

            private void changeAuthHeader() {

                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String dontChangeWwwAuthenticate = httpRequest.getHeader("DontChangeWwwAuthenticate");
                if (dontChangeWwwAuthenticate != null && dontChangeWwwAuthenticate.length() != 0) {
                    return;
                }

                if (isNtlmAuthentication) {
                    return;
                }

                this.setHeader(WWW_AUTHENTICATE, "FormBased");
            }

            @Override
            public void setHeader(String name, String value) {
                if (WWW_AUTHENTICATE.equals(name)) {
                    isNtlmAuthentication = isValidNtlmAuthentication(wrappedRequest, value);
                }
                super.setHeader(name, value);
            }

        };

        filterChain.doFilter(wrappedRequest, wrappedResponse);
    }

    /**
     * Checks whether NTLM authentication response is valid.
     * If request contains failed NTLM blob then suppress NTLM authentication.
     */
    private boolean isValidNtlmAuthentication(HttpServletRequest request, String responseAuthHeader) {
        if (!isNtlmAuth(responseAuthHeader)) {
            return false;
        }

        if (isStartLoginChallenge(responseAuthHeader)) {
            return noFailedNtlmAuth(request);
        }

        return true;
    }

    /**
     * Checks whether response tries to initiate NTLM authentication process.
     * The header contains NTLM blob with hash when it's a second or third handshake.
     */
    private boolean isStartLoginChallenge(String authHeader) {
        return AUTH_NTLM.equals(authHeader.trim());
    }

    /**
     * Checks whether request contains NTLM auth blob which failed to pass check. 
     */
    private boolean noFailedNtlmAuth(HttpServletRequest request) {
        return !isNtlmAuth(request.getHeader(AUTHORIZATION));
    }

    /**
     * Prevents from NTLM authorization if current user has already been authenticated and web session has been established.
     * Thus redundant browser authentication pop-ups are suppressed. 
     * @param request
     */
    private void filterNTLMRequestAuthorization(HttpServletRequest request) {
        String authHdr = request.getHeader(AUTHORIZATION);
        if (!isNtlmAuth(authHdr)) {
            return;
        }
        HttpSession session = request.getSession();
        SessionUser sessionUser = (SessionUser) session.getAttribute(AuthenticationDriver.AUTHENTICATION_USER);
        if (sessionUser == null) {
            return;
        }
        request.setAttribute(NO_AUTH_REQUIRED, true);
    }

    private boolean isNtlmAuth(String headerValue) {
        return headerValue != null && headerValue.startsWith(AUTH_NTLM);
    }

    @Override
    public void destroy() {

    }
}