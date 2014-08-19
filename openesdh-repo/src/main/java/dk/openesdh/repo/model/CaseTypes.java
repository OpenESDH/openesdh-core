package dk.openesdh.repo.model;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by flemming on 8/19/14.
 */
public class CaseTypes extends AbstractWebScript {

        public void execute(WebScriptRequest req, WebScriptResponse res)
                throws IOException
        {
            try
            {
                // build a json object
                JSONObject obj = new JSONObject();

                // put some data on it
                obj.put("field1", "data1");

                // build a JSON string and send it back
                String jsonString = obj.toString();
                res.getWriter().write(jsonString);
            }
            catch(JSONException e)
            {
                throw new WebScriptException("Unable to serialize JSON");
            }
        }
}


