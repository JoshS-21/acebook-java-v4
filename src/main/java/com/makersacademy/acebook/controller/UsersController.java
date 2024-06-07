package com.makersacademy.acebook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makersacademy.acebook.model.Authority;
import com.makersacademy.acebook.model.Comment;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.AuthoritiesRepository;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import com.makersacademy.acebook.model.UserSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.makersacademy.acebook.utils.EmailValidator.isEmailValid;
import static com.makersacademy.acebook.utils.PasswordValidator.isPasswordValid;

@Controller
public class UsersController {

    private final UserRepository userRepository;
    private final AuthoritiesRepository authoritiesRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UsersController(
            UserRepository userRepository, AuthoritiesRepository authoritiesRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.authoritiesRepository = authoritiesRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/users/new")
    public String signup(Model model) {
        model.addAttribute("user", new User());
        return "users/new";
    }

    @PostMapping("/users")
    public RedirectView signup(@ModelAttribute User user, @RequestParam("confirm_password") String confirmPassword) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return new RedirectView("/users/new?error=username");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return new RedirectView("/users/new?error=emailExists");
        }
        if (!isEmailValid(user.getEmail())) {
            return new RedirectView("/users/new?error=email");
        }
        if (!isPasswordValid(user.getPassword()) || !Objects.equals(user.getPassword(), confirmPassword)) {
            return new RedirectView("/users/new?error=password");
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setProfilePicture("https://res.cloudinary.com/dk3vxa56n/image/upload/c_limit,h_60,w_90/v1717509379/mxi8udntfuauiaxy5vyj.png");
        userRepository.save(user);
        Authority authority = new Authority(user.getUsername(), "ROLE_USER");
        authoritiesRepository.save(authority);
        return new RedirectView("/login");
    }


    @GetMapping("/users/my-profile")
    public ModelAndView profile() {
        ModelAndView modelAndView = new ModelAndView("users/my-profile");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipleName = authentication.getName();

        User user = userRepository.findByUsername(currentPrincipleName);
        return getUserModelAndView(modelAndView, currentPrincipleName, user);
    }

    private ModelAndView getUserModelAndView(ModelAndView modelAndView, String currentPrincipleName, User user) {
        modelAndView.addObject("user", user);
        List<Post> posts = postRepository.findByUserIdOrderByIdDesc(
                userRepository.findIdByUsername(currentPrincipleName));
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("post", new Post());
        modelAndView.addObject("comment", new Comment());

        return modelAndView;
    }

    @PostMapping("/profile-pic-add")
    public RedirectView profilePicAdd(@RequestParam(
            value = "imageProfileInfoInput", required=false) String imageProfileInfo) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipleName = authentication.getName();

        User user = userRepository.findByUsername(currentPrincipleName);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(imageProfileInfo, Map.class);
        String thumbnail_url = map.get("thumbnail_url");
        user.setProfilePicture(thumbnail_url);
        userRepository.save(user);
        return new RedirectView("/users/my-profile");
    }

    @GetMapping("/users")
    public ModelAndView users() {
        ModelAndView modelAndView = new ModelAndView("users/users");
        modelAndView.addObject("users", userRepository.findAll());
        return modelAndView;
    }

    @GetMapping("/users/other-profile/")
    public ModelAndView showOtherProfile(@RequestParam("username") String username) {
        ModelAndView modelAndView = new ModelAndView("users/other-profile");

        User user = userRepository.findByUsername(username);
        return getUserModelAndView(modelAndView, username, user);
    }

    @PostMapping("/users/other-profile/{username}")
    public RedirectView comment(@RequestParam("postId") Long postId, @RequestParam("content") String content, @RequestParam("username") String username) {
        PostsController.createPostComment(postId, content, postRepository, userRepository, commentRepository);
        String redirectUrl = "/users/other-profile/?username=" + username;
        return new RedirectView(redirectUrl);
    }

    @GetMapping("/users/searched-users")
    public ModelAndView searchedUsers(@RequestParam("search_user") String keyword) {
        ModelAndView modelAndView = new ModelAndView("users/searched-users");
        UserSearch userSearch = new UserSearch();
        List<User> searchedUsers = userSearch.searchUsers(userRepository.findAll(), keyword);
        modelAndView.addObject("users", searchedUsers);
        return modelAndView;
    }
}
