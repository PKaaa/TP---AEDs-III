package util;

import java.util.Base64;

public class CriptografiaXOR {
    private static final String CHAVE = "NutriChef2024SecureKey";
    
    /**
     * Criptografa um texto usando XOR
     */
    public static String criptografar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        
        byte[] bytes = texto.getBytes();
        byte[] chaveBytes = CHAVE.getBytes();
        byte[] resultado = new byte[bytes.length];
        
        for (int i = 0; i < bytes.length; i++) {
            resultado[i] = (byte) (bytes[i] ^ chaveBytes[i % chaveBytes.length]);
        }
        
        return Base64.getEncoder().encodeToString(resultado);
    }
    
    /**
     * Descriptografa um texto criptografado com XOR
     */
    public static String descriptografar(String textoCriptografado) {
        if (textoCriptografado == null || textoCriptografado.isEmpty()) {
            return textoCriptografado;
        }
        
        byte[] bytes = Base64.getDecoder().decode(textoCriptografado);
        byte[] chaveBytes = CHAVE.getBytes();
        byte[] resultado = new byte[bytes.length];
        
        for (int i = 0; i < bytes.length; i++) {
            resultado[i] = (byte) (bytes[i] ^ chaveBytes[i % chaveBytes.length]);
        }
        
        return new String(resultado);
    }
    
    /**
     * Verifica se o texto parece estar criptografado (Base64)
     */
    public static boolean isCriptografado(String texto) {
        if (texto == null || texto.isEmpty()) {
            return false;
        }
        return texto.matches("^[A-Za-z0-9+/=]+$");
    }
}
