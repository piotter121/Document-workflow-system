package pl.edu.pw.ee.pyskp.documentworkflow.service;

/**
 * Created by piotr on 20.12.16.
 */
public interface SecurityService {
    String findLoggedInUsername();
    void autologin(String username, String password);
}