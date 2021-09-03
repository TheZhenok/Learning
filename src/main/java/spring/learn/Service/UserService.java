package spring.learn.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring.learn.Models.Roles;
import spring.learn.Models.User;
import spring.learn.Repos.UserRepos;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepos userRepos;

    @Autowired
    private MailSender mailSender;

    public UserService(UserRepos userRepos) {
        this.userRepos = userRepos;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepos.findByUsername(username);
    }

    public boolean addUser(User user) {
        User userFromDB = userRepos.findByUsername(user.getUsername());

        if (userFromDB != null) {
            return false;
        }

        user.setActive(true);
        user.setRoles(Collections.singleton(Roles.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        userRepos.save(user);
        if (!StringUtils.isEmpty(user.getEmail())){
            String message = String.format(
                    "Hello, %s! \n " +
                            "Welocome to Learning! Please visit this link: http://localhost:8080/activate/%s",
                    user.getUsername(),user.getActivationCode()
            );

            mailSender.send(user.getEmail(),"Activation code", message);
        }
        return true;
    }

    public boolean activateUser(String code) {
        User user = userRepos.findByActivationCode(code);
        if(user == null)
            return false;
        user.setActivationCode(null);
        userRepos.save(user);
        return true;
    }
}
