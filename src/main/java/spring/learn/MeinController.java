package spring.learn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spring.learn.Models.Message;
import spring.learn.Models.Roles;
import spring.learn.Models.User;
import spring.learn.Models.dto.CapchaResponseDTO;
import spring.learn.Repos.MessageRepos;
import spring.learn.Repos.UserRepos;
import spring.learn.Service.UserService;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Controller
public class MeinController {
    private final static String CAPCHA_URL="https://www.google.com/recaptcha/api/siteverify?secret=%s&response";

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${recapcha.secret}")
    private String recapchaSecret;

    @Autowired
    private RestTemplate restTemplate;

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
            @Valid @ModelAttribute("messageText") Message message,
            BindingResult bindingResult,
            Model model,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        System.out.println("ENTER POST!");
        message.setAuthor(user);
        if(bindingResult.hasErrors()){
            Map<String, String> errorMap = UtilsController.getErrorMap(bindingResult);
            model.mergeAttributes(errorMap);
            for (int i = 0; i < errorMap.size(); i++) {
                if(errorMap.keySet().toArray()[i] == "nameError"){
                    model.addAttribute("nameError", errorMap.values().toArray()[i]);
                }
                if(errorMap.keySet().toArray()[i] == "messageError"){
                    model.addAttribute("nameError", errorMap.values().toArray()[i]);
                }
                System.out.println(i + " " + errorMap.values().toArray()[i]);
            }
        }else {

            if (file != null && !file.getOriginalFilename().isEmpty()) {
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }
                String uuidFile = UUID.randomUUID().toString();
                String resultFileName = uuidFile + '.' + file.getOriginalFilename();

                file.transferTo(new File(uploadPath + "/" + resultFileName));

                message.setFilename(resultFileName);
            }

            messageRepos.save(message);
        }
        Iterable<Message> all = messageRepos.findAll();
        model.addAttribute("all", all);

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
    public String addUser(
            @RequestParam("g-recaptcha-response") String capchaReform,
            @Valid @ModelAttribute("newUser") User user,
            BindingResult bindingResult,
            Model model){
        String url = String.format(CAPCHA_URL, recapchaSecret, capchaReform);
        CapchaResponseDTO capchaResponseDTO = restTemplate.postForObject(
                url,
                Collections.emptyList(),
                CapchaResponseDTO.class);
        if(!capchaResponseDTO.isSuccess()){
            System.out.println("ERROR CAPCHA");
            model.addAttribute("capchaError", "Fill capcha");
        }
        if (bindingResult.hasErrors() || !capchaResponseDTO.isSuccess()) {
            System.out.println("ERROR BINDING!");
            Map<String, String> errors = UtilsController.getErrorMap(bindingResult);

            model.mergeAttributes(errors);

            return "reg";
        }
        if(!userService.addUser(user)){
            model.addAttribute("message", "User is found!");
            return "reg";
        }
        System.out.println("PostReg");
        return "redirect:trueEmail";
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

    @GetMapping("/trueEmail")
    public String mailIsSand(){
        return "trueEmail";
    }














}
