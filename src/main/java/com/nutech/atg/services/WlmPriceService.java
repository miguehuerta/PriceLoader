package com.nutech.atg.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.nutech.atg.entities.WlmPrice;
import com.nutech.atg.repository.WlmPriceRepository;


@Service
public class WlmPriceService {

	@Autowired
	WlmPriceRepository wlmPriceRepository;
	
	@Autowired
	CatalogService catalogService;
	
	public List<WlmPrice> findByPriceIdIn(Set<String> pricesIds){
		System.out.println("Getting All the prices from DB! | Not Cached");
		return wlmPriceRepository.findByPriceIdIn(pricesIds);
	}

	@Async
	public CompletableFuture<List<WlmPrice>> saveWlmPrices(List<WlmPrice> partialWlmPriceUpdates, String environment) {
		// TODO Auto-generated method stub
		
		List<WlmPrice> prices = new ArrayList<>();
		if (environment.equals("preview")) {
			System.out.println("actualizando delta en wlmPrice en preview");
			catalogService.changeCatPreview();
			System.out.println("Saving a list of wlmPrices of size " + partialWlmPriceUpdates.size() + "records");
			final long start = System.currentTimeMillis();
			prices = wlmPriceRepository.saveAll(partialWlmPriceUpdates);
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
			
		}
		if (environment.equals("prod")) {
			System.out.println("actualizando delta en wlmPrice en prod");
			System.out.println("Saving a list of wlmPrices of size " + partialWlmPriceUpdates.size() + "records");
			final long start = System.currentTimeMillis();
			catalogService.changeCatA();
			prices = wlmPriceRepository.saveAll(partialWlmPriceUpdates);
			catalogService.changeCatB();
			prices.addAll(wlmPriceRepository.saveAll(partialWlmPriceUpdates));

			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		return CompletableFuture.completedFuture(prices);

	}

	@Async
	public void deleteWlmPrices(List<WlmPrice> partialWlmPriceDeletes, String environment) {
		if (environment.equals("preview")) {
			System.out.println("borrando delta en wlmPrice en preview");
			catalogService.changeCatPreview();
			System.out.println("deleting a list of wlmPrices of size " + partialWlmPriceDeletes.size() + "records");
			final long start = System.currentTimeMillis();
			wlmPriceRepository.deleteAll(partialWlmPriceDeletes);
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
			
		}
		if (environment.equals("prod")) {
			System.out.println("borrando delta en wlmPrice en prod");
			System.out.println("deleting a list of wlmPrices of size " + partialWlmPriceDeletes.size() + "records");
			final long start = System.currentTimeMillis();
			catalogService.changeCatA();
			wlmPriceRepository.deleteAll(partialWlmPriceDeletes);
			catalogService.changeCatB();
			wlmPriceRepository.deleteAll(partialWlmPriceDeletes);

			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
	}
}
