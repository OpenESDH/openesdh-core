package dk.openesdh.repo.services.search;

import org.json.JSONArray;
import org.json.JSONException;

public interface LiveSearchComponent {

    JSONArray search(String query, int size) throws JSONException;
}
