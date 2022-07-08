package com.nutech.atg.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nutech.atg.entities.DcsPrice;
import com.nutech.atg.entities.WlmPrice;
import com.nutech.atg.repository.DcsPriceRepository;
import com.nutech.priceloader.entities.PriceListProject;

@Service
public class DcsPriceService {

	@Autowired
	DcsPriceRepository dcsPriceRepo;

	@Autowired
	CatalogService catalogService;

	public List<DcsPrice> findByskuIdIn(Set<String> skus) {
		System.out.println("Getting All the prices from DB! | Not Cached");
		return dcsPriceRepo.findByskuIdIn(skus);
	}

	public List<DcsPrice> getPriceBySkuId(String sku) {
		// TODO Auto-generated method stub
		return dcsPriceRepo.findBySkuId(sku);
	}

	@Cacheable(key = "'dcsPrice'", value = "dcsPriceCache")
	public List<DcsPrice> findAll() {
		System.out.println("Getting All the skus from DB! | Not Cached");
		return dcsPriceRepo.findAll();
	}

	public Long getCount() {
		return dcsPriceRepo.count();
	}

	@CachePut(key = "'dcsPrice'", value = "dcsPriceCache")
	public List<DcsPrice> updateCacheDeltas() {
		List<DcsPrice> all = this.findAll();
		Set<String> ids = all.stream().map(DcsPrice::getPriceId).collect(Collectors.toSet()).stream()
				.map(String::valueOf).collect(Collectors.toSet());
		List<DcsPrice> delta = dcsPriceRepo.findByPriceIdNotIn(ids);
		List<DcsPrice> joinedList = Stream.concat(all.stream(), delta.stream()).collect(Collectors.toList());
		return joinedList;
	}

	@Async
	public CompletableFuture<List<DcsPrice>> savePrices(List<DcsPrice> partialDcs, String environment) {
		// TODO Auto-generated method stub
		List<DcsPrice> prices = new ArrayList<>();
		if (environment.equals("preview")) {
			System.out.println("actualizando delta en dcsPrice dcsPrice en preview");
			catalogService.changeCatPreview();
			System.out.println("Saving a list of dcsPrices of size " + partialDcs.size() + " records");
			final long start = System.currentTimeMillis();
			prices = dcsPriceRepo.saveAll(partialDcs);
			dcsPriceRepo.flush();
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
		if (environment.equals("prod")) {
			System.out.println("actualizando delta en dcsPrice en prod");
			System.out.println("Saving a list of dcsPrices of size " + partialDcs.size() + "records");
			final long start = System.currentTimeMillis();
			catalogService.changeCatA();
			prices = dcsPriceRepo.saveAll(partialDcs);
			dcsPriceRepo.flush();
			catalogService.changeCatB();
			prices.addAll(dcsPriceRepo.saveAll(partialDcs));
			dcsPriceRepo.flush();

			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}

		return CompletableFuture.completedFuture(prices);
	}

	public List<DcsPrice> findByPriceIdCata(List<DcsPrice> partialDynamoPrice) {
		// TODO Auto-generated method stub
		Set<String> setPriceIds = partialDynamoPrice.stream().map(DcsPrice::getPriceId).collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());
				
		catalogService.changeCatA();
		return dcsPriceRepo.findByPriceIdIn(setPriceIds);
	}
	
	public List<DcsPrice> findByPriceIdCatb(List<DcsPrice> partialDynamoPrice) {
		// TODO Auto-generated method stub
		Set<String> setPriceIds = partialDynamoPrice.stream().map(DcsPrice::getPriceId).collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());
				
		catalogService.changeCatB();
		return dcsPriceRepo.findByPriceIdIn(setPriceIds);
	}

	public List<DcsPrice> findByPriceIdPreview(List<DcsPrice> partialDynamoPrice) {
		// TODO Auto-generated method stub
		Set<String> setPriceIds = partialDynamoPrice.stream().map(DcsPrice::getPriceId).collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());
				
		catalogService.changeCatPreview();
		return dcsPriceRepo.findByPriceIdIn(setPriceIds);
	}

	public void deletePrices(List<DcsPrice> partialDcs, String environment) {
		// TODO Auto-generated method stub
		if (environment.equals("preview")) {
			System.out.println("borrando delta en dcsPrice en preview");
			catalogService.changeCatPreview();
			System.out.println("Saving a list of dcsPrices of size " + partialDcs.size() + " records");
			final long start = System.currentTimeMillis();
			dcsPriceRepo.deleteAll(partialDcs);
			dcsPriceRepo.flush();
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
		if (environment.equals("prod")) {
			System.out.println("borrando delta en dcsPrice en prod");
			final long start = System.currentTimeMillis();
			catalogService.changeCatA();
			dcsPriceRepo.deleteAll(partialDcs);
			dcsPriceRepo.flush();
			catalogService.changeCatB();
			dcsPriceRepo.deleteAll(partialDcs);
			dcsPriceRepo.flush();

			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}

	}
}
