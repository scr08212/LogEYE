package com.nkm.logeye.global.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class ApiKeyGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private static final int numBytes = 32; // api 길이

    public static String generateApiKey() {
        byte[] randomBytes = new byte[numBytes];
        secureRandom.nextBytes(randomBytes);

        return encoder.encodeToString(randomBytes);
    }
}
