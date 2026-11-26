package com.payroll.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdentificationNumbersTest {

    @Test
    void nifAcceptsEightNineOrTenDigits() {
        assertEquals("12345678", IdentificationNumbers.nif("12345678"));
        assertEquals("123456789", IdentificationNumbers.nif("123 456 789"));
        assertEquals("1234567890", IdentificationNumbers.nif("1234567890"));
        assertNull(IdentificationNumbers.nif("1234567"));
        assertNull(IdentificationNumbers.nif("12345678901"));
        assertNull(IdentificationNumbers.nif("12345678A"));
    }

    @Test
    void nissRequiresElevenDigits() {
        assertEquals("11911111201", IdentificationNumbers.niss("119 1111 1201"));
        assertNull(IdentificationNumbers.niss("1191111120"));
        assertNull(IdentificationNumbers.niss("119111112011"));
        assertNull(IdentificationNumbers.niss("SS-E-201"));
    }
}
