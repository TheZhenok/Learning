package spring.learn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.learn.Models.Roles;
import spring.learn.Models.User;
import spring.learn.Service.UserService;

import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping()
    public String userList(Model model){
        model.addAttribute("list", userService.findAll());
        return "user/userList";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("{id}")
    public String editUser(@PathVariable Long id, Model model){
        User user = userService.getById(id);
        System.out.println(user.getRoles());
        model.addAttribute("user", user);
        model.addAttribute("roles", Roles.values());
        return "user/userEdit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping()
    public String saveUser(
            @RequestParam("username") String username,
            @RequestParam Map<String, String> form,
            @RequestParam("id") User user){
        userService.saveUser(username, form, user);
        return "redirect:/user";
    }

    @GetMapping("profile")
    public String getProfile(Model model, @AuthenticationPrincipal User user){
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("profile")
    public String editProfile(@AuthenticationPrincipal User user,
                              @RequestParam("password") String newPassword,
                              @RequestParam("email") String newEmail){
        System.out.println(newEmail);
        userService.editProfile(newPassword, newEmail, user);

        return "redirect:/user/profile";
    }
}
