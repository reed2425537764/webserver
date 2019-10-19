package cn.webserver.utils;

import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaUtils {

    //  从文件中读取公钥

    public static PublicKey getPublicKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPublicKey(bytes);
    }

    //从文件中读取密钥
    public static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPrivateKey(bytes);
    }

    //获取公钥
    public static PublicKey getPublicKey(byte[] bytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    //获取私钥
    public static PrivateKey getPrivateKey(byte[] bytes) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    //根据密文，生成rsa公钥和私钥,并写入指定文件
    public static void generateKey(String publicKeyFilename, String privateKeyFilename, String secret) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        keyPairGenerator.initialize(1024, secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        // 获取公钥并写出
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        writeFile(publicKeyFilename, publicKeyBytes);
        // 获取私钥并写出
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        writeFile(privateKeyFilename, privateKeyBytes);
    }

    private static byte[] readFile(String fileName) throws Exception {
        return Files.readAllBytes(new File(fileName).toPath());
    }

    private static void writeFile(String destPath, byte[] bytes) throws IOException {
        File dest = new File(destPath);
        if (!dest.exists()) {
            dest.createNewFile();
        }
        Files.write(dest.toPath(), bytes);
    }

    //RSA加密
    public static String encryptStr(String str) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, RsaUtils.getPrivateKey("E:\\mianshi\\pri.pem"));
        byte[] bytes = str.getBytes();
        byte[] encrypt = null;
        for (int i = 0; i < bytes.length; i += 64) {
            byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(bytes, i, i+64));
            encrypt = ArrayUtils.addAll(encrypt, doFinal);
        }
        return bytesToString(encrypt);
    }

    //RSA解密
    public static String decryptStr(String str)  throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, RsaUtils.getPublicKey("E:\\mianshi\\pub.pem"));
        byte[] bytes = stringToBytes(str);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i += 128) {
            byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(bytes, i, i + 128));
            sb.append(new String(doFinal));
        }
        return sb.toString();
    }

    public static String bytesToString(byte[] encrytpByte) {
        StringBuilder sb = new StringBuilder();
        for (Byte bytes : encrytpByte) {
            sb.append(bytes.toString()).append(" ");
        }
        return sb.toString();
    }

    public static byte[] stringToBytes(String data) {
        String[] strArr = data.split(" ");
        int len = strArr.length;
        byte[] clone = new byte[len];
        for (int i = 0; i < len; i++) {
            clone[i] = Byte.parseByte(strArr[i]);
        }

        return clone;
    }
}