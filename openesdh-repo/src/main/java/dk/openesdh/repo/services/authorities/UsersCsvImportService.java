package dk.openesdh.repo.services.authorities;

import java.io.InputStream;

import org.json.simple.JSONObject;

public interface UsersCsvImportService {

    JSONObject uploadUsersCsv(InputStream usersCsv);
}
