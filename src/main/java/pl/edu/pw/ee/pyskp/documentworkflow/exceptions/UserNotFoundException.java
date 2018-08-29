package pl.edu.pw.ee.pyskp.documentworkflow.exceptions;

import java.util.Collections;
import java.util.Map;

/**
 * Created by piotr on 29.12.16.
 */
public class UserNotFoundException extends ResourceNotFoundException {
    private final String email;

    public UserNotFoundException(String email) {
        super(String.format("Didn't found user with e-mail: %s", email));
        this.email = email;
    }

    @Override
    public Map<String, String> getMessageParams() {
        return Collections.singletonMap("email", email);
    }
}
