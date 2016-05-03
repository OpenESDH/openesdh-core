package dk.openesdh.repo.webscripts.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebscriptResponse;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

public class WebScriptUtils {

    //Get nodeRef from request uri
    private static final String NODE_ID = "node_id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";
    private static final MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();

    public static final String CONTENT_ENCODING_UTF_8 = "UTF-8";

    public static final String TASK_ID = "taskId";

    public static final String TEMPLATE_ENGINE_FREEMARKER = "freemarker";

    public static final String WEBSCRIPT_TEMPLATES_FOLDER_PATH = "alfresco/extension/templates/webscripts";

    public static final String JSON = "json";

    static {
        ObjectMapper objectMapper = jsonMessageConverter.getObjectMapper();
        objectMapper.registerModule(MultiLanguageValueDeserializer.getDeserializerModule());
        objectMapper.registerModule(PersonInfoDeserializer.getDeserializerModule());
        objectMapper.registerModule(CaseFolderItemDeserializer.getDeserializerModule());
        objectMapper.registerModule(NodeRefSerializer.getSerializerModule());
    }

    public static final String webScriptTemplatePath(String relativeTemplatePath) {
        return WEBSCRIPT_TEMPLATES_FOLDER_PATH + "/" + relativeTemplatePath;
    }

    public static boolean isContentTypeJson(WebScriptRequest req) {
        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }
        return MimetypeMap.MIMETYPE_JSON.equals(contentType);
    }

    public static void checkContentTypeJson(WebScriptRequest req) {
        if (!isContentTypeJson(req)) {
            throw new WebScriptException(Status.STATUS_UNSUPPORTED_MEDIA_TYPE, "Wrong Content-Type");
        }
    }

    /**
     * Returns a noderef from the request uri the template args must be of the form {store_type}/{store_id}/{id} with
     * the variables names as such
     *
     * @param req
     * @return
     */
    public static NodeRef getNodeRefFromRequest(WebScriptRequest req) {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        NodeRef nodeRef = null;
        String storeType = templateArgs.get(STORE_TYPE);
        String storeId = templateArgs.get(STORE_ID);
        String nodeId = templateArgs.get(NODE_ID);
        if (storeType != null && storeId != null && nodeId != null) {
            nodeRef = new NodeRef(storeType, storeId, nodeId);
        }
        return nodeRef;
    }

    public static JSONObject readJson(WebScriptRequest req) {
        checkContentTypeJson(req);

        JSONObject json;
        JSONParser parser = new JSONParser();
        try {
            String content = req.getContent().getContent();
            json = (JSONObject) parser.parse(content);
        } catch (IOException | ParseException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Invalid JSON: " + e.getMessage());
        }
        return json;
    }

    public static Object readJson(Class<? extends Object> clazz, WebScriptRequest req) throws IOException {
        checkContentTypeJson(req);
        return jsonMessageConverter.read(clazz, getHttpInputMessage(req));
    }

    public static void writeJson(Object obj, WebScriptResponse res) throws IOException {
        jsonMessageConverter.write(obj, MediaType.APPLICATION_JSON, getHttpOutputMessage(res));
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

    public static void respondSuccess(WebScriptResponse res, String message)
            throws IOException {
        respondWithMessage(res, Status.STATUS_OK, message);
    }

    public static void respondWithMessage(WebScriptResponse res, int status,
            String message) throws IOException {
        JSONObject json = new JSONObject();
        res.setStatus(status);
        json.put("message", message);
        json.writeJSONString(res.getWriter());
        res.getWriter().flush();
    }

    public static Resolution respondSuccess(String message) {
        return jsonResolution(new JSONObject().put("message", message));
    }

    /**
     * prepares response and executes action
     *
     * @param response
     * @param action
     * @throws IOException
     */
    private static void write(AnnotationWebscriptResponse res, WriteAction action) throws Exception {
        res.setContentType(MimetypeMap.MIMETYPE_JSON);
        res.setContentEncoding(CONTENT_ENCODING_UTF_8);
        res.setHeader("Cache-Control", "no-cache,no-store");
        action.execute();
    }

    private interface WriteAction {

        public void execute() throws Exception;
    }

    public static Resolution jsonResolution(Object o) {
        return (req, res, params) -> write(res, ()
                -> writeJson(o, res));
    }

    public static Resolution jsonResolution(org.json.JSONObject o) {
        return (req, res, params) -> write(res, ()
                -> o.write(res.getWriter()));
    }

    public static Resolution jsonResolution(org.json.JSONArray o) {
        return (req, res, params) -> write(res, ()
                -> o.write(res.getWriter()));
    }

    /**
     *
     * @param o - {@link org.json.simple.JSONObject}, {@link org.json.simple.JSONArray} and etc.
     * @return
     */
    public static Resolution jsonResolution(org.json.simple.JSONStreamAware o) {
        return (req, res, params) -> write(res, ()
                -> o.writeJSONString(res.getWriter()));
    }
}
