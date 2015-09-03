package dk.openesdh.repo.webscripts.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.alfresco.repo.content.MimetypeMap;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class WebScriptUtils {

    public static final String CASE_ID = "caseId";

    public static final String NODE_REF = "nodeRef";

    public static void checkContentTypeJson(WebScriptRequest req) {
        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }
    
        if (!MimetypeMap.MIMETYPE_JSON.equals(contentType)) {
            throw new WebScriptException(Status.STATUS_UNSUPPORTED_MEDIA_TYPE, "Wrong Content-Type");
        }
    }

    public static Object readJson(Class<? extends Object> clazz, WebScriptRequest req) throws IOException {
        checkContentTypeJson(req);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        return converter.read(clazz, getHttpInputMessage(req));
    }

    public static void writeJson(Object obj, WebScriptResponse res) throws IOException {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.write(obj, MediaType.APPLICATION_JSON, getHttpOutputMessage(res));
    }

    private static HttpOutputMessage getHttpOutputMessage(WebScriptResponse res) {
        return new HttpOutputMessage() {
            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            @Override
            public OutputStream getBody() throws IOException {
                return res.getOutputStream();
            }

        };
    }

    private static HttpInputMessage getHttpInputMessage(WebScriptRequest req) {
        return new HttpInputMessage() {
    
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders httpHeaders = new HttpHeaders();
                Arrays.asList(req.getHeaderNames())
                        .stream()
                        .forEach(headerName -> httpHeaders.put(headerName,
                                        Arrays.asList(req.getHeaderValues(headerName))
                                        ));
                return httpHeaders;
            }
    
            @Override
            public InputStream getBody() throws IOException {
                return req.getContent().getInputStream();
            }
        };
    }

}
