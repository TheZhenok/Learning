package spring.learn.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring.learn.Models.Roles;
import spring.learn.Models.User;
import spring.learn.Repos.UserRepos;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepos userRepos;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepos userRepos) {
        this.userRepos = userRepos;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepos.findByUsername(username);
    }

    private void sendMessage(User user) {
        if (!StringUtils.isEmpty(user.getEmail())){
            String message = String.format(
                    "Hello, %s! \n " +
                            "Welocome to Learning! Please visit this link: http://localhost:8080/activate/%s",
                    user.getUsername(), user.getActivationCode()
            );

            mailSender.send(user.getEmail(),"Activation code", message);
        }
    }

    public boolean addUser(User user) {
        User userFromDB = userRepos.findByUsername(user.getUsername());

        System.out.println("Enter add");
        if (userFromDB != null) {
            return false;
        }

        System.out.println(user.getEmail());
        user.setActive(true);
        user.setRoles(Collections.singleton(Roles.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepos.save(user);
        sendMessage(user);
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

    public List<User> findAll() {

        return userRepos.findAll();
    }

    public User getById(Long id) {
        return userRepos.getById(id);
    }

    public void saveUser(String username, Map<String, String> form, User user) {
        user.setUsername(username);
        Set<String> roles = Arrays.stream(Roles.values()).map(Roles::name).collect(Collectors.toSet());

        user.getRoles().clear();
        for (String key : form.keySet()) {
            if(roles.contains(key)){
                user.getRoles().add(Roles.valueOf(key));
            }

        }
        userRepos.save(user);
    }

    public void editProfile(String newPassword, String newEmail, User user) {
        String oleEmail = user.getEmail();
        boolean isEmailChanged = (newEmail != null && !newEmail.equals(oleEmail) ||
                (oleEmail != null && !oleEmail.equals(newEmail)));
        if(isEmailChanged){
            user.setEmail(newEmail);

            if(!StringUtils.isEmpty(newEmail)){
                user.setActivationCode(UUID.randomUUID().toString());
            }
        }
        if(!StringUtils.isEmpty(newPassword)){
            user.setPassword(newPassword);
        }

        userRepos.save(user);

        if(!isEmailChanged) {
            sendMessage(user);
        }
    }
}
