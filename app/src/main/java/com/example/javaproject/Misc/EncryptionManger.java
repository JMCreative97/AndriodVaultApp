package com.example.javaproject.Misc;


import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class EncryptionManger {

    private String mTransformation;
    private String mEncoder;
    private int mKeySize;
    private KeyGenerator mKeyGenerator;
    private Context mContext;
    private SharedPreferencesManager sharedPreferencesManager;
    private String ENCRYPTION_ALGORITHM, HASH_ALGORITHM;


    public EncryptionManger(Context context) {
        mContext = context;
        sharedPreferencesManager = new SharedPreferencesManager(context);
        ENCRYPTION_ALGORITHM = sharedPreferencesManager.getStringPref("ENCRYPTION_ALGORITHM") == null ? "AES" : sharedPreferencesManager.getStringPref("ENCRYPTION_ALGORITHM");
        HASH_ALGORITHM = sharedPreferencesManager.getStringPref("HASH_ALGORITHM") == null ? "PBKDF2" : sharedPreferencesManager.getStringPref("HASH_ALGORITHM");
        System.out.println(ENCRYPTION_ALGORITHM);
        System.out.println(HASH_ALGORITHM);
    }


    public HashMap<String, String> AuthenticateCloudFolderPW(String folderPassword, String inputPassword, String hashAlgorithm, String encAlgorithm) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {


        Base64.Encoder encoder = Base64.getEncoder();

        //AUTHENTICATION STEP 1:
        //Decrypt string with pw
        String dec = AnyDecrypterStr(folderPassword, inputPassword, ENCRYPTION_ALGORITHM, null);

        //AUTHENTICATION STEP 2:
        //Validate string matches hash
        System.out.println(dec);
        HashMap<String, String> hashMap = varifyHash(inputPassword, dec);
        if (hashMap != null) {
            hashMap.put("password", folderPassword);
            return hashMap;
        }

        return null;
    }

    public HashMap<String, String> AuthenticateLocalFolderPW(String folderTitle, String pw, String hashAlgorithm, String encAlgorithm) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {


        Base64.Encoder encoder = Base64.getEncoder();

        File file = new File(mContext.getFilesDir() + "/credentials", folderTitle);

        FileInputStream input = new FileInputStream(file);

        byte[] folderPw = new byte[(int) file.length()];

        int readBytes;

        while ((readBytes = input.read(folderPw)) != -1) {
            break;
        }

        input.close();

        //AUTHENTICATION STEP 1:
        //Decrypt string with pw
        String dec = AnyDecrypterStr(new String(folderPw, "UTF-8"), pw, ENCRYPTION_ALGORITHM, null);

        //AUTHENTICATION STEP 2:
        //Validate string matches hash
        HashMap<String, String> hashmap = varifyHash(pw, dec);
        if (hashmap != null) {
            hashmap.put("password", new String(folderPw, "UTF-8"));

            System.out.println("Hash Accepted");
            if (sharedPreferencesManager.getStringPref(hashmap.get("salt")) == null) {
                byte[] iv = generateIV(encAlgorithm);
                sharedPreferencesManager.setStringPref(hashmap.get("salt"), encoder.encodeToString(iv));
            }
            return hashmap;
        }
        System.out.println("Hash Rejected");
        return null;
    }

    public void copyCloudPassword(String folderTitle, String folderPassword) throws IOException {

        FileOutputStream outputFile = new FileOutputStream(new File(mContext.getFilesDir() + "/credentials", folderTitle));

        outputFile.write(folderPassword.getBytes());

        outputFile.close();

    }

    public void generateLocalPW(String fileOutputTitle, String pw) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {

        byte[] salt = generateSalt();

        //Generate folder IV and store
        byte[] iv = generateIV(ENCRYPTION_ALGORITHM);

        Base64.Encoder encoder = Base64.getEncoder();

        sharedPreferencesManager.setStringPref(encoder.encodeToString(salt), encoder.encodeToString(iv));

        String pwHash = AnyHash(pw, salt);

        System.out.println("Generated Password Hash : " + pwHash);

        //pw used to encrypt hashed pw
        String folderPw = AnyEncrypterStr(pwHash, pw, null, ENCRYPTION_ALGORITHM);

        System.out.println("Generated Password Encrypted Hash : " + folderPw);

        FileOutputStream outputFile = new FileOutputStream(new File(mContext.getFilesDir() + "/credentials", fileOutputTitle));

        outputFile.write(folderPw.getBytes());

        outputFile.close();

//        System.out.println(folderPw);
//        System.out.println(pw);
        //System.out.println(encAlgorithm);

    }

    public HashMap<String, String> varifyHash(String string, String input) throws UnsupportedEncodingException, InvalidKeySpecException, NoSuchAlgorithmException {

        Base64.Encoder encoder = Base64.getEncoder();
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] inputBytes = decoder.decode(input);

        byte[] salt = new byte[inputBytes.length / 2];

        for (int i = 0; i < inputBytes.length / 2; i++) {
            salt[i] = inputBytes[i];
        }

        byte[] pwBytes = string.getBytes("UTF-8");
        byte[] newHash = new byte[pwBytes.length + salt.length];

        //Combine salt and string, then hash
        for (int i = 0; i < pwBytes.length + salt.length; i++) {
            newHash[i] = i < (salt.length) ? salt[i] : pwBytes[i - salt.length];
        }

        byte[] hash = null;
        byte[] finalHash = null;

        switch (HASH_ALGORITHM) {
            case ("PBKDF2"):
                PBEKeySpec spec = new PBEKeySpec(string.toCharArray(), salt, 6225, 256);
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                hash = secretKeyFactory.generateSecret(spec).getEncoded();
                finalHash = new byte[hash.length + salt.length];
                for (int i = 0; i < hash.length + salt.length; i++) {
                    finalHash[i] = i < (salt.length) ? salt[i] : hash[i - salt.length];
                }
                break;
            case ("SHA"):
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                hash = md.digest(newHash);
                finalHash = new byte[hash.length + salt.length];
                for (int i = 0; i < hash.length + salt.length; i++) {
                    finalHash[i] = i < (salt.length) ? salt[i] : hash[i - salt.length];
                }
                break;
            case ("BCrypt"):
                //BCrypt.checkpw(string, input);
        }

        if (encoder.encodeToString(finalHash).equals(input)) {
            HashMap<String, String> hashmap = new HashMap<>();
            hashmap.put("salt", encoder.encodeToString(salt));
            hashmap.put("hash", encoder.encodeToString(hash));
            return hashmap;
        } else return null;
    }

    public String AnyHash(String string, byte[] optionalSalt) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeySpecException {

        Base64.Encoder encoder = Base64.getEncoder();

        byte[] salt = optionalSalt != null ? optionalSalt : generateSalt();

        byte[] pwBytes = string.getBytes("UTF-8");
        byte[] newHash = new byte[pwBytes.length + salt.length];

        //Combine salt and string, then hash
        for (int i = 0; i < pwBytes.length + salt.length; i++) {
            newHash[i] = i < (salt.length) ? salt[i] : pwBytes[i - salt.length];
        }

        byte[] hash = null;
        byte[] finalHash = null;

        switch (HASH_ALGORITHM) {
            case ("PBKDF2"):
                PBEKeySpec spec = new PBEKeySpec(string.toCharArray(), salt, 6225, 256);
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                hash = secretKeyFactory.generateSecret(spec).getEncoded();
                finalHash = new byte[hash.length + salt.length];
                for (int i = 0; i < hash.length + salt.length; i++) {
                    finalHash[i] = i < (salt.length) ? salt[i] : hash[i - salt.length];
                }
                break;
            case ("SHA"):
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                hash = md.digest(newHash);
                System.out.println("HASH LENGTH  " + hash.length);
                finalHash = new byte[hash.length + salt.length];
                for (int i = 0; i < hash.length + salt.length; i++) {
                    finalHash[i] = i < (salt.length) ? salt[i] : hash[i - salt.length];
                }
                break;
            case ("BCrypt"):
               // BCrypt.hashpw(string, BCrypt.gensalt(6225));
        }

        return encoder.encodeToString(finalHash);

    }

    public void fileDecrypter(File fileIn, String stringKey, String algorithm, File fileOut, String optionalIV, String optionalSalt) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] salt;
            if (optionalIV != null) {
                salt = decoder.decode(optionalSalt);
            } else salt = generateSalt();
            byte[] iv = null;
            IvParameterSpec ivParameterSpec = null;
            SecretKeySpec keySpec = null;
            SecretKeyFactory secretKeyFactory = null;
            KeySpec spec = null;
            SecretKey key = null;
            String cipherInstance = "";

            switch (ENCRYPTION_ALGORITHM) {
                case "AES":
                    //Gen IV
                    iv = generateIV("AES");
                    //Gen secretKey
                    secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    //Gen keySpec
                    spec = new PBEKeySpec(stringKey.toCharArray(), salt, 6225, 256);

                    //Gen Key
                    key = secretKeyFactory.generateSecret(spec);

                    //Gen secret key with blowfish
                    keySpec = new SecretKeySpec(key.getEncoded(), ENCRYPTION_ALGORITHM);

                    //Cipher Instace
                    cipherInstance = "AES/CBC/PKCS5Padding";
                    break;
                case "Blowfish":
                    char[] pw = stringKey.toCharArray();
                    //Gen IV
                    iv = generateIV("Blowfish");
                    //Gen secretKey
                    secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    //Gen keySpec
                    spec = new PBEKeySpec(stringKey.toCharArray(), salt, 6225, 448);

                    //Gen Key
                    key = secretKeyFactory.generateSecret(spec);

                    //Gen secret key with blowfish
                    keySpec = new SecretKeySpec(key.getEncoded(), "Blowfish/CBC/PKCS5Padding");

                    //Cipher instance
                    cipherInstance = "Blowfish/CBC/PKCS5Padding";
                    break;
                case "DESede":
                    //Gen IV
                    iv = generateIV("DESede");

                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    byte[] toHash = new byte[salt.length + stringKey.getBytes().length];

                    for (int i = 0; i < toHash.length; i++) {
                        toHash[i] = i < salt.length ? salt[i] : (stringKey.getBytes("UTF-8"))[i - salt.length];
                    }

                    byte[] digestBytes = messageDigest.digest(toHash);
                    byte[] keyBytes = Arrays.copyOf(digestBytes, 24);
                    secretKeyFactory = SecretKeyFactory.getInstance("DESede");
                    DESedeKeySpec DESspec = new DESedeKeySpec(keyBytes);
                    SecretKey secretKey = secretKeyFactory.generateSecret(DESspec);
                    keySpec = new SecretKeySpec(secretKey.getEncoded(), "DESede");

                    cipherInstance = "DESede/CBC/PKCS5Padding";
                    break;
            }

            ivParameterSpec = new IvParameterSpec(optionalIV != null ? decoder.decode(optionalIV) : iv);

            //Gen Enc
            Cipher cipher = Cipher.getInstance(cipherInstance);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);


            //file encryption
            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            FileInputStream file2Dec = new FileInputStream(fileIn);
            FileOutputStream out = new FileOutputStream(fileOut);
            while ((bytesRead = file2Dec.read(buffer)) > 0) {
                byte[] output = cipher.update(buffer, 0, bytesRead);

                if (output != null)
                    out.write(output);
            }

            byte[] output = cipher.doFinal();
            if (output != null)
                out.write(output);


            file2Dec.close();
            out.flush();
            out.close();

            System.out.println("File Encrypted.");

        } catch (Exception e) {
            System.err.println("Encryption err : " + e);
        }
    }


    public void fileEncrypter(File fileIn, String stringKey, File fileOut, String optionalIV, String optionalSalt) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] salt;
            if (optionalIV != null) {
                salt = decoder.decode(optionalSalt);
            } else salt = generateSalt();
            byte[] iv = null;
            IvParameterSpec ivParameterSpec = null;
            SecretKeySpec keySpec = null;
            SecretKeyFactory secretKeyFactory = null;
            KeySpec spec = null;
            SecretKey key = null;
            String cipherInstance = "";

            switch (ENCRYPTION_ALGORITHM) {
                case "AES":
                    //Gen IV
                    iv = generateIV("AES");
                    //Gen secretKey
                    secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    //Gen keySpec
                    spec = new PBEKeySpec(stringKey.toCharArray(), salt, 6225, 256);

                    //Gen Key
                    key = secretKeyFactory.generateSecret(spec);

                    //Gen secret key with blowfish
                    keySpec = new SecretKeySpec(key.getEncoded(), "AES");

                    //Cipher Instace
                    cipherInstance = "AES/CBC/PKCS5Padding";
                    break;
                case "Blowfish":
                    char[] pw = stringKey.toCharArray();
                    //Gen IV
                    iv = generateIV("Blowfish");
                    //Gen secretKey
                    secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    //Gen keySpec
                    spec = new PBEKeySpec(stringKey.toCharArray(), salt, 6225, 448);

                    //Gen Key
                    key = secretKeyFactory.generateSecret(spec);

                    //Gen secret key with blowfish
                    keySpec = new SecretKeySpec(key.getEncoded(), "Blowfish");

                    //Cipher instance
                    cipherInstance = "Blowfish/CBC/PKCS5Padding";
                    break;
                case "DESede":
                    //Gen IV
                    iv = generateIV("DESede");

                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    byte[] toHash = new byte[salt.length + stringKey.getBytes().length];

                    for (int i = 0; i < toHash.length; i++) {
                        toHash[i] = i < salt.length ? salt[i] : (stringKey.getBytes("UTF-8"))[i - salt.length];
                    }

                    byte[] digestBytes = messageDigest.digest(toHash);
                    byte[] keyBytes = Arrays.copyOf(digestBytes, 24);
                    secretKeyFactory = SecretKeyFactory.getInstance("DESede");
                    DESedeKeySpec DESspec = new DESedeKeySpec(keyBytes);
                    SecretKey secretKey = secretKeyFactory.generateSecret(DESspec);
                    keySpec = new SecretKeySpec(secretKey.getEncoded(), "DESede");

                    cipherInstance = "DESede/CBC/PKCS5Padding";
                    break;
            }

            //Gen Enc
            Cipher cipher = Cipher.getInstance(cipherInstance);


            ivParameterSpec = new IvParameterSpec(optionalIV != null ? decoder.decode(optionalIV) : iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

            FileInputStream file2Enc = new FileInputStream(fileIn);

            FileOutputStream out = new FileOutputStream(fileOut);

            //file encryption
            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            while ((bytesRead = file2Enc.read(buffer)) > 0) {
                byte[] output = cipher.update(buffer, 0, bytesRead);

                if (output != null)
                    out.write(output);
            }

            byte[] output = cipher.doFinal();
            if (output != null)
                out.write(output);


            file2Enc.close();
            out.flush();
            out.close();

            System.out.println("File Encrypted.");

        } catch (Exception e) {
            System.err.println("Encryption err : " + e);
        }
    }

    //String to encrypt would be hash, string is pw
    public String AnyEncrypterStr(String string2Enc, String stringKey, byte[] optionalIV, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

        Base64.Encoder encoder = Base64.getEncoder();

        //Gen Salt
        byte[] salt = generateSalt();
        byte[] iv = {};
        IvParameterSpec ivParameterSpec = null;
        SecretKeySpec keySpec = null;
        SecretKeyFactory secretKeyFactory = null;
        KeySpec spec = null;
        SecretKey key = null;
        String cipherInstance = "";

        switch (algorithm) {
            case "AES":
                //Gen IV
                iv = generateIV("AES");
                //Gen secretKey
                secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                //Gen keySpec
                spec = new PBEKeySpec(stringKey.toCharArray(), salt, 6225, 256);

                //Gen Key
                key = secretKeyFactory.generateSecret(spec);

                //Gen secret key
                keySpec = new SecretKeySpec(key.getEncoded(), "AES");

                //Cipher Instace
                cipherInstance = "AES/CBC/PKCS5Padding";
                break;
            case "Blowfish":
                char[] pw = stringKey.toCharArray();
                //Gen IV
                iv = generateIV("Blowfish");

                //Gen secretKey
                secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                //Gen keySpec
                spec = new PBEKeySpec(stringKey.toCharArray(), salt, 6225, 448);

                //Gen Key
                key = secretKeyFactory.generateSecret(spec);

                //Gen secret key with blowfish
                keySpec = new SecretKeySpec(key.getEncoded(), "Blowfish");


                //Cipher instance
                cipherInstance = "Blowfish/CBC/PKCS5Padding";
                break;
            case "DESede":
                //Gen IV
                iv = generateIV("DESede");

                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] toHash = new byte[salt.length + stringKey.getBytes().length];

                for (int i = 0; i < toHash.length; i++) {
                    toHash[i] = i < salt.length ? salt[i] : (stringKey.getBytes("UTF-8"))[i - salt.length];
                }

                byte[] digestBytes = messageDigest.digest(toHash);
                byte[] keyBytes = Arrays.copyOf(digestBytes, 24);
                secretKeyFactory = SecretKeyFactory.getInstance("DESede");
                DESedeKeySpec DESspec = new DESedeKeySpec(keyBytes);
                SecretKey secretKey = secretKeyFactory.generateSecret(DESspec);
                keySpec = new SecretKeySpec(secretKey.getEncoded(), "DESede");

                cipherInstance = "DESede/CBC/PKCS5Padding";
                break;
        }

        //Gen Enc
        Cipher cipher = Cipher.getInstance(cipherInstance);

        ivParameterSpec = new IvParameterSpec(optionalIV != null ? optionalIV : iv);


        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

        byte[] encByte = cipher.doFinal(string2Enc.getBytes("UTF-8"));

        //Gen Enc:salt:Iv
        String encString = encoder.encodeToString(encByte);
        String saltString = encoder.encodeToString(salt);
        String ivString = encoder.encodeToString(iv);

//        System.out.println("ENC enc " + encString);
//        System.out.println("ENC salt " + saltString);
//        System.out.println("ENC iv " + ivString);

        String combined = encString + "#" + saltString + "#" + ivString;


        return combined;
    }

    public String AnyDecrypterStr(String string2Dec, String stringKey, String algorithm, byte[] optionalIV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

        Base64.Decoder decoder = Base64.getDecoder();

        //Obtain data
        String[] split = string2Dec.split("#");
        String encString = split[0];
        byte[] salt = decoder.decode(split[1]);
        byte[] iv = decoder.decode(split[2]);


        IvParameterSpec ivParameterSpec = null;
        SecretKeySpec keySpec = null;
        SecretKeyFactory secretKeyFactory = null;
        KeySpec spec = null;
        SecretKey key = null;
        String cipherInstance = "";

        switch (ENCRYPTION_ALGORITHM) {
            case "AES":
                //Gen secretKey
                secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                //Gen keySpec
                spec = new PBEKeySpec(stringKey.toCharArray(), salt, 6225, 256);

                //Gen Key
                key = secretKeyFactory.generateSecret(spec);

                //Gen secret key with blowfish
                keySpec = new SecretKeySpec(key.getEncoded(), "AES");

                //Cipher Instace
                cipherInstance = "AES/CBC/PKCS5Padding";
                break;
            case "Blowfish":

                //Gen secretKey
                secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                //Gen keySpec
                spec = new PBEKeySpec(stringKey.toCharArray(), salt, 6225, 448);

                //Gen Key
                key = secretKeyFactory.generateSecret(spec);

                //Gen secret key with blowfish
                keySpec = new SecretKeySpec(key.getEncoded(), "Blowfish");

                //Cipher instance
                cipherInstance = "Blowfish/CBC/PKCS5Padding";
                break;
            case "DESede":

                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] toHash = new byte[salt.length + (stringKey.getBytes()).length];
                for (int i = 0; i < toHash.length; i++) {
                    toHash[i] = i < salt.length ? salt[i] : (stringKey.getBytes("UTF-8"))[i - salt.length];
                }
                byte[] digestBytes = messageDigest.digest(toHash);
                byte[] keyBytes = Arrays.copyOf(digestBytes, 24);
                secretKeyFactory = SecretKeyFactory.getInstance("DESede");
                DESedeKeySpec DESspec = new DESedeKeySpec(keyBytes);
                SecretKey secretKey = secretKeyFactory.generateSecret(DESspec);
                keySpec = new SecretKeySpec(secretKey.getEncoded(), "DESede");

                cipherInstance = "DESede/CBC/PKCS5Padding";
                break;
        }

        ivParameterSpec = new IvParameterSpec(optionalIV != null ? optionalIV : iv);
        //Gen Enc
        Cipher cipher = Cipher.getInstance(cipherInstance);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);


        byte[] string = decoder.decode(encString);

        byte[] encByte = cipher.doFinal(string);

        String decString = new String(encByte, "UTF-8");
        return decString;
    }

    public static byte[] generateSalt() {

        byte[] salt = new byte[32]; //32 bytes / 265 bit

        SecureRandom random = new SecureRandom();

        random.nextBytes(salt);

        return salt;
    }

    public static byte[] generateIV(String algorithm) {
        int ivSize = 0;
        switch (algorithm) {
            case "AES":
                ivSize = 16;
                break;
            case "Blowfish":
                ivSize = 8;
                break;
            case "DESede":
                ivSize = 8;
                break;
        }

        byte[] iv = new byte[ivSize];

        SecureRandom random = new SecureRandom();

        random.nextBytes(iv);

        return iv;
    }

    public void deleteCredentials(String title) {
        String noteKey = title + ".n";
        sharedPreferencesManager.setStringPref(noteKey, null);
        deleteAlias(noteKey);
    }

    public String basicNoteDecrypter(File fileIn, String noteTitle, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

        Base64.Decoder decoder = Base64.getDecoder();
        Base64.Encoder encoder = Base64.getEncoder();

        byte[] encBytes = null;
        try {
            FileInputStream input = new FileInputStream(fileIn);
            encBytes = new byte[(int) fileIn.length()];
            int readBytes;
            while ((readBytes = input.read(encBytes)) != -1) {
                break;
            }
            input.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String cipherInstance = "";

        switch (algorithm) {
            case "AES":
                //Cipher Instace
                cipherInstance = "AES/CBC/PKCS7Padding";
                break;
            case "Blowfish":
                //Cipher Instace
                cipherInstance = "Blowfish/CBC/PKCS7Padding";
                break;
            case "DESede":
                //Cipher Instace
                cipherInstance = "DESede/CBC/PKCS7Padding";
                break;
        }

        //Gen Enc
        Cipher cipher = Cipher.getInstance(cipherInstance);

        byte[] iv = decoder.decode(sharedPreferencesManager.getStringPref(noteTitle + ".n"));

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        SecretKey secretKey = getKey(noteTitle + ".n");

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        byte[] decByte = cipher.doFinal(encBytes);

        System.out.println("Note Decrypted");

        return new String(decByte);


    }

    public void basicNoteEncrypter(String string2Enc, File fileOut, String noteTitle, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

        Base64.Decoder decoder = Base64.getDecoder();
        String cipherInstance = "";

        switch (algorithm) {
            case "AES":
                //Cipher Instace
                cipherInstance = "AES/CBC/PKCS7Padding";
                break;
//            case "Blowfish":
//                //Cipher Instace
//                cipherInstance = "Blowfish/CBC/PKCS7Padding";
//                break;
//            case "DESede":
//                //Cipher Instace
//                cipherInstance = "DESede/CBC/PKCS7Padding";
//                break;
        }

        //Gen Enc
        Cipher cipher = Cipher.getInstance(cipherInstance);

        byte[] iv = decoder.decode(sharedPreferencesManager.getStringPref(noteTitle + ".n"));

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        SecretKey secretKey = getKey(noteTitle + ".n");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        byte[] encByte = cipher.doFinal(string2Enc.getBytes("UTF-8"));

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileOut);
            fileOutputStream.write(encByte);
            fileOutputStream.close();
            System.out.println("Note Encrypted");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void basicFileDecrypter(String folderTitle, File fileIn, File fileOut, String algorithm) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            String cipherInstance = "";

            switch (algorithm) {
                case "AES":
                    cipherInstance = "AES/CBC/PKCS7Padding";
                    break;
//                case "Blowfish":
//                    cipherInstance = "AES/CBC/PKCS7Padding";
//                    break;
//                case "DESede":
//                    cipherInstance = "DESede/CBC/PKCS5Padding";
//                    break;
            }

            //Gen Enc
            Cipher cipher = Cipher.getInstance(cipherInstance);

            byte[] iv = decoder.decode(sharedPreferencesManager.getStringPref(folderTitle));

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            SecretKey secretKey = getKey(folderTitle);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            FileInputStream file2Enc = new FileInputStream(fileIn);

            FileOutputStream out = new FileOutputStream(fileOut);

            //file encryption
            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            while ((bytesRead = file2Enc.read(buffer)) > 0) {
                byte[] output = cipher.update(buffer, 0, bytesRead);

                if (output != null)
                    out.write(output);
            }

            byte[] output = cipher.doFinal();
            if (output != null)
                out.write(output);


            file2Enc.close();
            out.flush();
            out.close();

            System.out.println("Basic File Decryption Complete.");

        } catch (Exception e) {
            System.err.println("Decryption err : " + e);
        }
    }

    public void basicFileEncrypter(String folderTitle, File fileIn, File fileOut, String algorithm) {

        try {
            Base64.Decoder decoder = Base64.getDecoder();
            String cipherInstance = "";

            switch (algorithm) {
                case "AES":
                    cipherInstance = "AES/CBC/PKCS7Padding";
                    break;
                case "Blowfish":
                    cipherInstance = "AES/CBC/PKCS7Padding";
                    break;
                case "DESede":
                    cipherInstance = "DESede/CBC/PKCS5Padding";
                    break;
            }

            //Gen Enc
            Cipher cipher = Cipher.getInstance(cipherInstance);

            byte[] iv = decoder.decode(sharedPreferencesManager.getStringPref(folderTitle));

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            SecretKey secretKey = getKey(folderTitle);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            FileInputStream file2Enc = new FileInputStream(fileIn);

            FileOutputStream out = new FileOutputStream(fileOut);

            //file encryption
            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            while ((bytesRead = file2Enc.read(buffer)) > 0) {
                byte[] output = cipher.update(buffer, 0, bytesRead);

                if (output != null)
                    out.write(output);
            }

            byte[] output = cipher.doFinal();
            if (output != null)
                out.write(output);


            file2Enc.close();
            out.flush();
            out.close();

            System.out.println("Basic File Encryption Complete.");

        } catch (Exception e) {
            System.err.println("Encryption err : " + e);
        }
    }

    public void createNoteCredentials(String noteTitle, String encAlgorithm) {
        Base64.Encoder encoder = Base64.getEncoder();
        String noteKey = noteTitle + ".n";
        generateKey(noteKey, encAlgorithm);
        byte[] iv = generateIV(encAlgorithm);
        sharedPreferencesManager.setStringPref(noteKey, encoder.encodeToString(iv));

    }


    public void createFolderCredentials(String folderTitle, String encAlgorithm) {
        Base64.Encoder encoder = Base64.getEncoder();
        generateKey(folderTitle, encAlgorithm);
        byte[] iv = generateIV(encAlgorithm);
        sharedPreferencesManager.setStringPref(folderTitle, encoder.encodeToString(iv));
    }


    //Andriod Key store //////////////////////////////////////////////////////////////////
    public void deleteAlias(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public SecretKey getKey(String Alias) {
        SecretKey secret = null;

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");

            keyStore.load(null);
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                    .getEntry(Alias, null);
            SecretKey secretKey = secretKeyEntry.getSecretKey();
            secret = secretKey;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return secret;
    }

    public void generateKey(String Alias, String algorithm) {
        try {
            int keySize = 0;
            KeyGenerator keyGenerator = null;
            String transformation;

//
//            switch (algorithm) {
//                case "AES":
//                    keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
//                    keySize = 256;
//                    transformation = "AES/CBC/PKCS7Padding";
//                    break;
//                //TODO IMPLEMENT MORE
//            }

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(Alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setKeySize(256)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setRandomizedEncryptionRequired(false)
                    .build());

            keyGenerator.generateKey();
            System.out.println("Secret AES Key Generated.");

        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    //Andriod Key store //////////////////////////////////////////////////////////////////


}