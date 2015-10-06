package dk.openesdh.repo.webscripts;

import java.io.IOException;

import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

@Component
@WebScript(families = { "somesome" }, description = "some description", defaultFormat = "json")
public class SomeWebScript {

    @Uri(value = "/dynamic-extensions/examples/some", method = HttpMethod.GET)
    public void doIt(@RequestParam(required = false) final String param, final WebScriptResponse response)
            throws IOException {
        System.out.println("This is param: " + param);
        response.getWriter().write("hello world");
    }
}
