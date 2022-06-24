package com.nutech.atg.repository;


import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nutech.atg.entities.DcsPrice;

public interface DcsPriceRepository extends JpaRepository<DcsPrice, String>{
	
	
	List<DcsPrice> findByskuIdIn(Set<String> setSkusPriceList);

	List<DcsPrice> findBySkuId(String skuId);
	
	@Query(nativeQuery = true, value = "SELECT MAX(TO_NUMBER(substr(PRICE_ID, 2))) FROM DCS_PRICE")
	public Long max();

	List<DcsPrice> findByPriceIdNotIn(Set<String> ids);

	DcsPrice findBySkuIdAndPriceList(String skuId, String priceList);

	List<DcsPrice> findByPriceIdIn(Set<String> setPriceIds);
}
