package com.minimarket.security.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XssSanitizer {

    /**
     * Sanitiza un texto eliminando cualquier etiqueta HTML o script malicioso (XSS).
     * @param input El texto a sanitizar.
     * @return El texto limpio y seguro.
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        // Usamos none() para eliminar ABSOLUTAMENTE TODA etiqueta HTML.
        return Jsoup.clean(input, Safelist.none());
    }
}
