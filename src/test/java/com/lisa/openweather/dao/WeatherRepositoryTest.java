package com.lisa.openweather.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static com.lisa.openweather.Utils.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WeatherRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WeatherRepository weatherRepository;

    @Test
    public void testFindTopByCityAndCountryOrderByIdDesc() {
        // Setup data
        Weather weather1 = new Weather(CITY, COUNTRY, DESCRIPTION_SUNNY);
        Weather weather2 = new Weather(CITY, COUNTRY, DESCRIPTION_CLOUDY);
        entityManager.persist(weather1);
        entityManager.persist(weather2);
        entityManager.flush();

        // Test findTopByCityAndCountryOrderByIdDesc
        Optional<Weather> found = weatherRepository.findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY);

        assertTrue(found.isPresent());
        // Assuming that weather2 has the latest ID due to descending order.
        assertEquals(DESCRIPTION_CLOUDY, found.get().getDescription());
    }

    @Test
    public void testFindTopByCityAndCountryOrderByIdDesc_returnEmpty() {
        // Test findTopByCityAndCountryOrderByIdDesc
        Optional<Weather> found = weatherRepository.findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY);

        // Assert that no data is found
        assertFalse(found.isPresent());
    }
}