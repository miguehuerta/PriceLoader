package com.nutech.atg.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutech.atg.entities.WlmProduct;

public interface WlmProductRepository extends JpaRepository<WlmProduct, String>{

	List<WlmProduct> findByproductIdNotIn(Set<String> ids);

	List<WlmProduct> findByProductIdIn(Set<String> setProductIds);
	
}
