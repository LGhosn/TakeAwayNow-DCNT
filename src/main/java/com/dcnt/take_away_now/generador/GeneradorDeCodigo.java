package com.dcnt.take_away_now.generador;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor
public class GeneradorDeCodigo {

    // Caracteres permitidos en el código alfanumérico
    private static final String CARACTERES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    // Longitud predeterminada del código
    private static final int LONGITUD_CODIGO = 10;

    public static String generarCodigoAleatorio() {
        StringBuilder codigoAleatorio = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            int indice = random.nextInt(CARACTERES.length());
            char caracter = CARACTERES.charAt(indice);
            codigoAleatorio.append(caracter);
        }

        return codigoAleatorio.toString();
    }

}
