package com.makersacademy.acebook.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Entity
@Table(name = "POSTS")
public class Post {

    @Getter
    @Setter

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String img_url;
    private Integer likeCount = 0;

    // No-args constructor
    public Post() {}

    // Constructor
    public Post(String content) {
        this.content = content;
    }
    public String getContent() { return this.content; }
    public void setContent(String content) { this.content = content; }
    public Integer getLikeCount() { return this.likeCount; }
    public void incrementLikeCount() { this.likeCount += 1; }
    public void setImg_url() { this.img_url = img_url; }
}
