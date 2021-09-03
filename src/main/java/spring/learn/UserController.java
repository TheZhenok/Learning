package spring.learn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.learn.Models.Roles;
import spring.learn.Models.User;
import spring.learn.Repos.UserRepos;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {
    @Autowired
    private UserRepos userRepos;
    @GetMapping()
    public String userList(Model model){
        model.addAttribute("list", userRepos.findAll());
        return "user/userList";
    }

    @GetMapping("{id}")
    public String editUser(@PathVariable Long id, Model model){
        User user = userRepos.getById(id);
        System.out.println(user.getRoles());
        model.addAttribute("user", user);
        model.addAttribute("roles", Roles.values());
        return "user/userEdit";
    }

    @PostMapping()
    public String saveUser(
            @RequestParam("username") String username,
            @RequestParam Map<String, String> form,
            @RequestParam("id") User user){
        user.setUsername(username);
        Set<String> roles = Arrays.stream(Roles.values()).map(Roles::name).collect(Collectors.toSet());

        user.getRoles().clear();
        for (String key : form.keySet()) {
            if(roles.contains(key)){
                user.getRoles().add(Roles.valueOf(key));
            }
            
        }
        userRepos.save(user);
        return "redirect:/user";
    }
}
