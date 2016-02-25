package dk.openesdh.repo.services.search;

import org.json.JSONException;
import org.json.JSONObject;

public interface LiveSearchService {

    /**
     * register search component from modules e.g. document template
     *
     * @param name
     * @param component
     */
    void registerComponent(String name, LiveSearchComponent component);

    /**
     * execute search in component by name
     *
     * @param name - component name. eg. "case", "caseDocs"...
     * @param query
     * @param size
     * @return
     * @throws JSONException
     */
    public JSONObject search(String name, String query, int size) throws JSONException;
}
