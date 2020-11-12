package com.cheeseind.blogengine.services;

import com.cheeseind.blogengine.models.CaptchaCode;
import com.cheeseind.blogengine.models.dto.authdto.CaptchaDto;
import com.cheeseind.blogengine.repositories.CaptchaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final CaptchaRepository captchaRepository;

    @Value("${captcha.code_length}")
    private int codeLength;
    @Value("${captcha.secret_length}")
    private int secretCodeLength;
    @Value("${captcha.text_size}")
    private int captchaTextSize;
    @Value("${captcha.width}")
    private int captchaWidth;
    @Value("${captcha.height}")
    private int captchaHeight;
    @Value("${captcha.frame_width}")
    private int captchaFrameWidth;
    @Value("${captcha.frame_height}")
    private int captchaFrameHeight;

    public CaptchaCode findBySecretCode(String code) {
        return captchaRepository.findBySecretCode(code).orElse(null);
    }

    public CaptchaCode findByCode(final String code) {
        return captchaRepository.findByCode(code).orElse(null);
    }

    public void save(final CaptchaCode captchaCode) {
        captchaRepository.save(captchaCode);
    }

    @Transactional
    public void delete(final CaptchaCode captchaCode) {
        captchaRepository.delete(captchaCode);
    }

    @Transactional
    public void deleteCaptchaCodeBySecretCode(final String secretCode) {
        captchaRepository.deleteBySecretCode(secretCode);
    }

    public List<CaptchaCode> getAllCaptchaCodes() {
        return captchaRepository.findAllBy();
    }

    public CaptchaDto generateCaptcha() {

        CaptchaDto captchaDto = new CaptchaDto();
        String secretCode = RandomStringUtils.randomAlphanumeric(secretCodeLength);
        String code = RandomStringUtils.randomAlphanumeric(codeLength);
        BufferedImage image = generateCaptchaImage(code);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            captchaDto.setSecret(secretCode);
            captchaDto.setImage("data:image/png;charset=utf-8;base64, "
                    + java.util.Base64.getEncoder().encodeToString(os.toByteArray()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        CaptchaCode captchaCode = new CaptchaCode(code, secretCode, LocalDateTime.now(ZoneOffset.UTC));
        captchaRepository.save(captchaCode);
//        dbEventsCreator.deleteCaptchaWhenExpired(captchaCode, captchaExpirationTime);
        return captchaDto;
    }

    private BufferedImage generateCaptchaImage(final String code) {

        BufferedImage image = new BufferedImage(captchaFrameWidth, captchaFrameHeight, BufferedImage.OPAQUE);
        Graphics graphics = image.createGraphics();
        graphics.setFont(new Font("Arial", Font.BOLD, captchaTextSize));
        graphics.setColor(Color.GRAY);
        graphics.fillRect(0, 0, captchaFrameWidth, captchaFrameHeight);
        graphics.setColor(Color.BLACK);
        graphics.drawString(code, captchaWidth, captchaHeight);
        return image;
    }

//    public boolean isCaptchaCodeExpired(final String captcha) {
//        Optional<CaptchaCode> captchaCodeOptional = captchaRepository.findByCode(captcha);
//        return captchaCodeOptional.map(captchaCode
//                -> LocalDateTime.now().isAfter(captchaCode.getTime().plusSeconds(captchaExpirationTime))).orElse(true);
//    }
}
