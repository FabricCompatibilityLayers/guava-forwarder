package io.github.fabriccompatibilitylayers.guavaforwarder.g13_0.stubs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;

public class MessageDigestUtils {
    static HashFunction messageDigestToHashFunction(MessageDigest messageDigest) {
        switch (messageDigest.getAlgorithm()) {
            case "MD5":
                return Hashing.md5();
            case "SHA-1":
                return Hashing.sha1();
            case "SHA-256":
                return Hashing.sha256();
            case "SHA-512":
                return Hashing.sha512();
            default:
                try {
                    Class mdhfClass = Class.forName("com.google.common.hash.MessageDigestHashFunction");
                    Constructor ctr = mdhfClass.getDeclaredConstructor(String.class, String.class);
                    ctr.setAccessible(true);
                    return (HashFunction) ctr.newInstance(messageDigest.getAlgorithm(), "");
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalArgumentException(messageDigest.getAlgorithm());
                }
        }
    }
}
