package com.nutech.atg.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutech.atg.entities.WlmPrice;

public interface WlmPriceRepository extends JpaRepository<WlmPrice, String>{

	List<WlmPrice> findByPriceIdIn(Set<String> skus);

}
