package com.makersacademy.acebook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makersacademy.acebook.model.Comment;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class PostsController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostsController(PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    static void createPostComment(@RequestParam("postId") Long postId, @RequestParam("content") String content, PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository) {
        Optional<Post> postOptional = postRepository.findById(postId);
        Post post = postOptional.get();
        Comment comment = new Comment();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipleName = authentication.getName();
        comment.setContent(content);
        comment.setPost(post);
        comment.setUser_id(userRepository.findIdByUsername(currentPrincipleName));
        commentRepository.save(comment);
    }

    @GetMapping("/welcome")
    public String welcome(Model model) {
        return "posts/landing-page";
    }

    @GetMapping("/posts")
    public String index(Model model) {
        Iterable<Post> posts = postRepository.findAllByOrderByIdDesc();
        model.addAttribute("posts", posts);
        model.addAttribute("post", new Post());
        model.addAttribute("comment", new Comment());
        return "posts/index";
    }

    @PostMapping("/posts")
    public RedirectView create(@ModelAttribute Post post, @RequestParam(value = "imageInfoInput", required=false) String imageInfo, @RequestParam(required=false) String fromProfilePage) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipleName = authentication.getName();
        User user = userRepository.findByUsername(currentPrincipleName);
        post.setUser(user);
        if (imageInfo != "") {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(imageInfo, Map.class);
            String url = map.get("url");
            post.setImg_url(url);
        }
        postRepository.save(post);
        if ("true".equals(fromProfilePage)) {
            return new RedirectView("/users/my-profile");
        }
        return new RedirectView("/posts");
    }

    @PostMapping("/posts-like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> like(@RequestParam("postId") Long postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        Post post = postOptional.get();
        post.incrementLikeCount();
        postRepository.save(post);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newLikeCount", post.getLikeCount());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts-comments")
    public RedirectView comment(@RequestParam("postId") Long postId, @RequestParam("content") String content, @RequestParam(required = false) String fromProfilePage, @RequestParam(value="profileUsername", required = false) String fromOtherProfilePage) {
        createPostComment(postId, content, postRepository, userRepository, commentRepository);
        if ("true".equals(fromProfilePage)) {
            return new RedirectView("/users/my-profile");
        }
        if (fromOtherProfilePage != null) {
            String redirectUrl = "/users/other-profile/?username=" + fromOtherProfilePage;
            return new RedirectView(redirectUrl);
        }
        return new RedirectView("/posts");
    }
}
