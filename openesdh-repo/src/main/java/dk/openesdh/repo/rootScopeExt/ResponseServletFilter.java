package dk.openesdh.repo.rootScopeExt;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter was created to prevent browsers rendering the basic auth dialog in respnse to a 401 message. For this to
 * work please copy the filter and filter-mapping elements in src/main/amp/config/alfresco/filter-mapping.config.xml
 * to the web.xml in the deployed alfresco.
 * @author Lanre.
 */
//TODO find out if it is possible to have a maven profile that can inject the filters into the deployed web.xml file.
public class ResponseServletFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseServletFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse wrappedResponse = new HttpServletResponseWrapper((HttpServletResponse) response) {

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
            // aaa
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
                this.setHeader("WWW-Authenticate", "FormBased");
            }
        };

        filterChain.doFilter(request, wrappedResponse);
    }

    @Override
    public void destroy() {

    }
}