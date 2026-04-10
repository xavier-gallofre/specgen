package com.openapi.generator.core.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utilidades para la transformación de cadenas de texto.
 */
public class TextUtils {

    /**
     * Convierte a minúsculas.
     */
    public static String toLower(String text) {
        if (text == null) return null;
        return text.toLowerCase();
    }

    /**
     * Convierte a mayúsculas.
     */
    public static String toUpper(String text) {
        if (text == null) return null;
        return text.toUpperCase();
    }

    /**
     * Capitaliza la primera letra de la cadena.
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Intenta convertir una palabra al plural (reglas básicas en inglés).
     */
    public static String pluralize(String text) {
        if (text == null || text.isEmpty()) return text;
        String lower = text.toLowerCase();
        if (lower.endsWith("y")) {
            if (isVowel(lower.charAt(lower.length() - 2))) {
                return text + "s";
            }
            return text.substring(0, text.length() - 1) + "ies";
        } else if (lower.endsWith("s") || lower.endsWith("sh") || lower.endsWith("ch") || lower.endsWith("x") || lower.endsWith("z")) {
            return text + "es";
        } else {
            return text + "s";
        }
    }

    /**
     * Intenta convertir una palabra al singular (reglas básicas en inglés).
     */
    public static String singularize(String text) {
        if (text == null || text.isEmpty()) return text;
        String lower = text.toLowerCase();
        if (lower.endsWith("ies")) {
            return text.substring(0, text.length() - 3) + "y";
        } else if (lower.endsWith("es")) {
            // Casos como "boxes" -> "box", "classes" -> "class"
            String base = text.substring(0, text.length() - 2);
            String baseLower = base.toLowerCase();
            if (baseLower.endsWith("s") || baseLower.endsWith("sh") || baseLower.endsWith("ch") || baseLower.endsWith("x") || baseLower.endsWith("z")) {
                return base;
            }
            return text.substring(0, text.length() - 1); // e.g. "orders" -> "order"
        } else if (lower.endsWith("s")) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    /**
     * Convierte a frase (primera mayúscula, resto minúsculas, espacios entre palabras).
     */
    public static String toPhrase(String text) {
        if (text == null) return null;
        String[] parts = splitWords(text);
        String phrase = Arrays.stream(parts)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
        return capitalize(phrase);
    }

    /**
     * Convierte a snake_case.
     */
    public static String toSnakeCase(String text) {
        if (text == null) return null;
        String[] parts = splitWords(text);
        return Arrays.stream(parts)
                .map(String::toLowerCase)
                .collect(Collectors.joining("_"));
    }

    /**
     * Convierte a kebab-case.
     */
    public static String toKebabCase(String text) {
        if (text == null) return null;
        String[] parts = splitWords(text);
        return Arrays.stream(parts)
                .map(String::toLowerCase)
                .collect(Collectors.joining("-"));
    }

    /**
     * Convierte a camelCase.
     */
    public static String toCamelCase(String text) {
        if (text == null) return null;
        String[] parts = splitWords(text);
        if (parts.length == 0) return "";
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            sb.append(capitalize(parts[i]));
        }
        return sb.toString();
    }

    private static String[] splitWords(String text) {
        // Divide por guiones, guiones bajos, espacios o cambios de camelCase
        return text.split("(?<=[a-z])(?=[A-Z])|[_\\-\\s]+");
    }

    private static boolean isVowel(char c) {
        return "aeiou".indexOf(Character.toLowerCase(c)) != -1;
    }
}
