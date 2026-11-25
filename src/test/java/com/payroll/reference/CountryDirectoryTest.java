package com.payroll.reference;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountryDirectoryTest {

    @Test
    void countryFileListsEachCodeOnceAndIncludesPortugal() {
        Set<String> codes = new HashSet<>();
        for (CountryDirectory.Country country : CountryDirectory.countries()) {
            assertTrue(codes.add(country.code()), country.code());
            assertTrue(!country.namePt().isBlank());
            assertTrue(!country.nameEn().isBlank());
        }

        assertTrue(codes.size() >= 190);
        CountryDirectory.Country portugal = CountryDirectory.find("PT").orElseThrow();
        assertEquals("Portugal", portugal.namePt());
        assertEquals("Portugal", portugal.nameEn());
    }
}
