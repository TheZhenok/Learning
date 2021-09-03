package spring.learn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import spring.learn.Models.Message;
import spring.learn.Models.Roles;
import spring.learn.Models.User;
import spring.learn.Repos.MessageRepos;
import spring.learn.Repos.UserRepos;
import spring.learn.Service.UserService;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.UUID;

@Controller
public class MeinController {
    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageRepos messageRepos;

    @Autowired
    private UserRepos userRepos;

    @GetMapping("")
    public String start(Model model, Principal principal){
        return "start";
    }

    @GetMapping("/mein")
    public String mein(Model model){
        Iterable<Message> all = messageRepos.findAll();
        model.addAttribute("all" ,all);
        return "mein";
    }
    @PostMapping("/mein")
    public String meinPost(
            @AuthenticationPrincipal User user,
            @RequestParam("name") String name,
            @RequestParam("message") String message,
            @RequestParam("file") MultipartFile file,
            Principal principal,
                           Model model) throws IOException {
        System.out.println(principal.getName());
        Message messageMein = new Message(message, name, user);

        if(file != null && !file.getOriginalFilename().isEmpty()){
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()){
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + '.' + file.getOriginalFilename();

            file.transferTo(new File(uploadPath + "/" + resultFileName));

            messageMein.setFilename(resultFileName);
        }

        messageRepos.save(messageMein);

        Iterable<Message> all = messageRepos.findAll();
        model.addAttribute("all" ,all);
        return "mein";
    }
    @PostMapping("filter")
    public String filter(@RequestParam("filter") String filter, Model model){
        System.out.println("PostFilter");
        Iterable<Message> messageList;
        if(filter != null && !filter.isEmpty()) {
            messageList = messageRepos.findByName(filter);
        }
        else {
            messageList = messageRepos.findAll();
        }
        model.addAttribute("all", messageList);
        return "mein";
    }

    @GetMapping("/reg")
    public String reg(){
        System.out.println("GetReg");
        return "reg";
    }
    @PostMapping("reg")
    public String addUser(User user, Model model){
        System.out.println("PostReg");
        if(!userService.addUser(user)){
            model.addAttribute("message", "User is found!");
            return "reg";
        }
        System.out.println("TRUE");
        user.setActive(true);
        user.setRoles(Collections.singleton(Roles.USER));
        userRepos.save(user);
        return "redirect:login";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code){
        boolean isActive = userService.activateUser(code);

        if(isActive){
            model.addAttribute("message", "User is activation!");
        }else {
            model.addAttribute("message", "User is not activation!");
        }
        return "login";
    }

















}
