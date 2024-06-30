package com.example.android_lib;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import android.util.Log;

public class Cryptography {
    private static final String KEY = "1234567890123456"; // Chave de 128 bits (16 bytes)
    private static final String ALGORITHM = "AES";

    public static String encrypt(String data) {
        long startTime = System.currentTimeMillis(); // Início da medição
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());

            //--------------------------------------------
            //Implementação Tarefa 4
            long endTime = System.currentTimeMillis(); // Fim da medição
            long executionTime = endTime - startTime;
            Log.d("Cryptography", "Tempo de execução da Tarefa 3 (Criptografia): " + executionTime + " ms");
            //--------------------------------------------
            return Base64.encodeToString(encrypted, Base64.DEFAULT); // Retorna a string criptografada
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while encrypting", e);
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while decrypting", e);
        }
    }
}
