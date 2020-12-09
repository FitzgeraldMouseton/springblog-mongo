package com.cheeseind.blogengine.services;

import com.cheeseind.blogengine.models.GlobalSetting;
import com.cheeseind.blogengine.repositories.SettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingsRepository settingsRepository;

    @Value("#{${settings}}")
    private HashMap<String, String> settings;

    @Value("${setting.multiuser}")
    private String multiuserSetting;
    @Value("${setting.premoderation}")
    private String premoderationSetting;

    public List<GlobalSetting> getSettings() {
        return settingsRepository.findAllBy();
    }

    public void save(final GlobalSetting setting) {
        settingsRepository.save(setting);
    }

    public boolean isMultiUserEnabled() {
        return isSettingEnabled(multiuserSetting);
    }

    public boolean isPremoderationEnabled() {
        return isSettingEnabled(premoderationSetting);
    }

    public GlobalSetting getSettingByCode(final String code) {
        return settingsRepository.findByCode(code).orElse(null);
    }

    void fillSettings() {
        settings.forEach((k,v) -> settingsRepository.save(new GlobalSetting(k, v, true)));
    }

    public GlobalSetting setSetting(final String setting, final Boolean isActive) {
        return new GlobalSetting(setting, settings.get(setting), isActive);
    }

    private boolean isSettingEnabled(String setting) {
        final Optional<GlobalSetting> settingOptional = settingsRepository.findByCode(setting);
        if (settingOptional.isPresent()) {
            return settingOptional.get().getValue();
        } else {
            return false;
        }
    }
}
