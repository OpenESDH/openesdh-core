package dk.openesdh.repo.services.authorities;

import java.io.InputStream;

import org.json.JSONObject;

public interface UsersService {

    JSONObject uploadUsersCsv(InputStream usersCsv) throws Exception;

    void setEmailFeedDisabled(String userId, boolean emailFeedDisabled);
}
