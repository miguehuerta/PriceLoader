package com.nutech.atg.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nutech.atg.entities.WlmProduct;
import com.nutech.atg.repository.WlmProductRepository;

@Service
public class WlmProductService {

	@Autowired
	WlmProductRepository wlmProductRepository;

	public List<WlmProduct> findAll(){
		System.out.println("Getting All the products from DB! | Not Cached");
		return wlmProductRepository.findAll();
	}

	public Long getCount() {
		return wlmProductRepository.count();
	}

	public List<WlmProduct> updateCacheDeltas() {
		List<WlmProduct> all = this.findAll();
		Set<String> ids = all.stream().map(WlmProduct::getProductId)
				.collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());
		List<WlmProduct> delta = wlmProductRepository.findByproductIdNotIn(ids);
		List<WlmProduct> joinedList = Stream.concat(all.stream(), delta.stream())
                .collect(Collectors.toList());
		return joinedList;
	}

	public List<WlmProduct> findByProductIdIn(Set<String> setProductIds) {
		// TODO Auto-generated method stub
		return wlmProductRepository.findByProductIdIn(setProductIds);
	}

}
