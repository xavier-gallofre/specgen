package com.specgen.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextUtilsTest {

    @Test
    public void testSimpleWord() {
        String input = "ORDERS";
        
        assertEquals("orders", TextUtils.toLower(input));
        assertEquals("ORDERS", TextUtils.toUpper(input));
        assertEquals("Orders", TextUtils.capitalize(input));
        
        String singular = TextUtils.singularize(input);
        assertEquals("ORDER", singular); // singularize preserves case if possible or just returns substring
        
        assertEquals("Order", TextUtils.capitalize(TextUtils.singularize(input)));
        assertEquals("Orders", TextUtils.pluralize("Order"));
    }

    @Test
    public void testCompoundWord() {
        String input = "customer_orders";
        
        assertEquals("Customer orders", TextUtils.toPhrase(input));
        assertEquals("customer_orders", TextUtils.toSnakeCase(input));
        assertEquals("customer-orders", TextUtils.toKebabCase(input));
        assertEquals("customerOrders", TextUtils.toCamelCase(input));
    }

    @Test
    public void testPluralizationRules() {
        assertEquals("categories", TextUtils.pluralize("category"));
        assertEquals("plays", TextUtils.pluralize("play"));
        assertEquals("boxes", TextUtils.pluralize("box"));
        assertEquals("classes", TextUtils.pluralize("class"));
        
        assertEquals("category", TextUtils.singularize("categories"));
        assertEquals("play", TextUtils.singularize("plays"));
        assertEquals("box", TextUtils.singularize("boxes"));
        assertEquals("class", TextUtils.singularize("classes"));
    }

    @Test
    public void testCamelCaseSplit() {
        String input = "CustomerOrders";
        assertEquals("customer_orders", TextUtils.toSnakeCase(input));
        assertEquals("customer-orders", TextUtils.toKebabCase(input));
        assertEquals("customerOrders", TextUtils.toCamelCase(input));
        assertEquals("Customer orders", TextUtils.toPhrase(input));
    }
}
