package com.lisa.openweather.dao;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findTopByCityAndCountryOrderByIdDesc(String city, String country);
}
