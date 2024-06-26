package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Post;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Long> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Post p WHERE p.id = (SELECT MAX(p2.id) FROM Post p2)")
    void deleteTestPost();
    public List<Post> findAllByOrderByIdDesc();
    public Post findTopByOrderByIdDesc();
    @Query(value = "SELECT p FROM Post p WHERE p.user.id = :id ORDER BY p.id DESC")
    public List<Post> findByUserIdOrderByIdDesc(@Param("id") Long id);

}
