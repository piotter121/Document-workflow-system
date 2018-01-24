package pl.edu.pw.ee.pyskp.documentworkflow.services.impl;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.edu.pw.ee.pyskp.documentworkflow.data.domain.User;
import pl.edu.pw.ee.pyskp.documentworkflow.services.UserService;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by piotr on 20.12.16.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = Logger.getLogger(UserDetailsServiceImpl.class);

    private final UserService userService;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user with login: " + username);
        User user = userService.getUserByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Nie znaleziono użytkownika o nazwie " + username));
        Collection<GrantedAuthority> authorities
                = Collections.singleton(new SimpleGrantedAuthority("USER"));
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(), user.getPassword(), authorities);
    }
}
