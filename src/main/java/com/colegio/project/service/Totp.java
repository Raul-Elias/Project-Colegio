package com.colegio.project.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * Códigos de un solo uso basados en tiempo (TOTP, RFC 6238).
 * Compatible con Google Authenticator, Microsoft Authenticator, Authy, etc.
 * No necesita librerías externas.
 */
public class Totp {

    private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int PASO_SEGUNDOS = 30;

    private Totp() {}

    /** Genera una clave secreta aleatoria de 160 bits, codificada en Base32. */
    public static String generarSecreto() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return codificarBase32(bytes);
    }

    /** Comprueba el código de 6 dígitos (acepta el periodo anterior y el siguiente por desfase de reloj). */
    public static boolean verificar(String secreto, String codigo) {
        if (secreto == null || codigo == null || !codigo.matches("\\d{6}")) {
            return false;
        }
        long contador = System.currentTimeMillis() / 1000 / PASO_SEGUNDOS;
        for (long i = -1; i <= 1; i++) {
            if (generar(secreto, contador + i).equals(codigo)) {
                return true;
            }
        }
        return false;
    }

    static String generar(String secreto, long contador) {
        try {
            byte[] clave = decodificarBase32(secreto);
            byte[] mensaje = ByteBuffer.allocate(8).putLong(contador).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(clave, "HmacSHA1"));
            byte[] hash = mac.doFinal(mensaje);
            int offset = hash[hash.length - 1] & 0x0F;
            int binario = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            return String.format("%06d", binario % 1_000_000);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el código", e);
        }
    }

    static String codificarBase32(byte[] datos) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : datos) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                sb.append(ALFABETO.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0) {
            sb.append(ALFABETO.charAt((buffer << (5 - bits)) & 31));
        }
        return sb.toString();
    }

    static byte[] decodificarBase32(String texto) {
        byte[] salida = new byte[texto.length() * 5 / 8];
        int buffer = 0;
        int bits = 0;
        int indice = 0;
        for (char c : texto.toUpperCase().toCharArray()) {
            int valor = ALFABETO.indexOf(c);
            if (valor < 0) {
                continue;
            }
            buffer = (buffer << 5) | valor;
            bits += 5;
            if (bits >= 8 && indice < salida.length) {
                salida[indice++] = (byte) ((buffer >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        return salida;
    }
}