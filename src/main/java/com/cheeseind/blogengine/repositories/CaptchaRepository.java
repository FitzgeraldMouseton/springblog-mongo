package com.cheeseind.blogengine.repositories;

import com.cheeseind.blogengine.models.CaptchaCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaptchaRepository extends MongoRepository<CaptchaCode, String> {

    Optional<CaptchaCode> findBySecretCode(String code);

    Optional<CaptchaCode> findByCode(String code);

    void deleteBySecretCode(String secretCode);

    List<CaptchaCode> findAllBy();
}
