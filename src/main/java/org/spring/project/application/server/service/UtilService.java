package org.spring.project.application.server.service;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class UtilService {

    public String sendServerErrorMessage(String message) {
        Base64 base64 = new Base64();
        return new String(base64.encode(message.getBytes(StandardCharsets.UTF_8)));
    }
}
