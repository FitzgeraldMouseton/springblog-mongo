package com.cheeseind.blogengine.repositories;

import com.cheeseind.blogengine.models.GlobalSetting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingsRepository extends MongoRepository<GlobalSetting, Integer> {

    Optional<GlobalSetting> findByCode(String code);
    List<GlobalSetting> findAllBy();
}
