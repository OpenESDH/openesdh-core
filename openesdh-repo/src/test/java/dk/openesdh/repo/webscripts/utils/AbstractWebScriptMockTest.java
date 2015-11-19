package dk.openesdh.repo.webscripts.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebScriptRequest;
import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebscriptResponse;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.DefaultResolutionParameters;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

public abstract class AbstractWebScriptMockTest {

    protected AnnotationWebscriptResponse getMockAnnotationResponse() {
        Writer out = new StringWriter();
        AnnotationWebscriptResponse response = mock(AnnotationWebscriptResponse.class);
        try {
            when(response.getWriter()).thenReturn(out);
        } catch (IOException unimportantException) {

        }
        return response;
    }

    protected String extractResolution(Resolution resolution) {
        try {
            AnnotationWebscriptResponse response = getMockAnnotationResponse();
            resolution.resolve(getMockAnnotationRequest(),
                    response,
                    getMockDefaultResolutionParameters());
            return response.getWriter().toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed: ", ex);
        }
    }

    protected WebScriptRequest getMockRequest() {
        return mock(WebScriptRequest.class);
    }

    protected AnnotationWebScriptRequest getMockAnnotationRequest() {
        return new AnnotationWebScriptRequest(getMockRequest());
    }

    protected DefaultResolutionParameters getMockDefaultResolutionParameters() {
        return mock(DefaultResolutionParameters.class);
    }

}
