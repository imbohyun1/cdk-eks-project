package com.example.fortunecookie.repository;

import com.example.fortunecookie.entity.Fortune;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FortuneRepository extends MongoRepository<Fortune, String> {

    List<Fortune> findByMessageContainingIgnoreCase(String keyword);

}