package io.github.fabriccompatibilitylayers.guavaforwarder.g13_0.stubs;

import com.google.common.io.Files;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

@GuavaStubs("com/google/common/io/Files")
public class I_Files {
    @GuavaStub
    public static byte[] getDigest(File file, MessageDigest messageDigest) throws IOException {
        return Files.hash(file, MessageDigestUtils.messageDigestToHashFunction(messageDigest)).asBytes();
    }
}
