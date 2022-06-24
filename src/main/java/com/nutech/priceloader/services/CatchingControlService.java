package com.nutech.priceloader.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.nutech.atg.entities.DcsPrice;
import com.nutech.atg.entities.WlmProduct;
import com.nutech.atg.services.DcsPriceService;
import com.nutech.atg.services.WlmProductService;
import com.nutech.priceloader.entities.CatchingControl;
import com.nutech.priceloader.repository.CatchingControlRepository;
import com.nutech.priceloader.utils.Helpers;

@Service
public class CatchingControlService {

	@Value("${cache.names}")
	private String caches;

	@Autowired
	private CatchingControlRepository catchingControlRepository;

	@Autowired
	private WlmProductService wlmProductService;

	@Autowired
	DcsPriceService dcsPriceService;


	@Autowired
	Helpers helper;

	public void initializeCaches() {
		String[] cachesList = caches.split("\\|");

		for (String cacheId : cachesList) {
			CatchingControl cache = catchingControlRepository.findByName(cacheId);
			if (cache == null) {
				CatchingControl newPrices = new CatchingControl(cacheId, (long) 0, false);
				catchingControlRepository.save(newPrices);
			} else {
				cache.setRunning(false);
				cache.setQuantity((long) 0);
				catchingControlRepository.save(cache);
			}
		}
	}

	public void populateCatching(String identifier) {
		System.out.println("Revisando el cache " + identifier);
		System.out.println("hora de inicio: "+helper.getCurrentDate());
		CatchingControl catchingControl = catchingControlRepository.findByName(identifier);
		if (catchingControl!=null && !catchingControl.isRunning()) {
			catchingControl.setRunning(true);
			catchingControlRepository.save(catchingControl);
			
			Long beforeCacheSize = (long) 0;
			
			Long sizeNow = (long) 0;
			switch (identifier) {
			case "productsCache":
				beforeCacheSize = wlmProductService.getCount();
				List<WlmProduct> data2 = wlmProductService.findAll();
				sizeNow = (long) data2.size();
				break;
			case "dcsPriceCache":
				beforeCacheSize = dcsPriceService.getCount();
				List<DcsPrice> data4 = dcsPriceService.findAll();
				sizeNow = (long) data4.size();
				break;
			}
		
			catchingControl.setQuantity(sizeNow);
			catchingControlRepository.save(catchingControl);
			
			
			if (!beforeCacheSize.equals(sizeNow)) {
				System.out.println("Es diferente el tamaño, se buscarán e insertará registros faltantes");
				this.updateCache(identifier);
			}
			
			catchingControl.setRunning(false);
			catchingControlRepository.save(catchingControl);
			System.out.println("hora de fin: "+helper.getCurrentDate());
		}else

	{
		System.out.println("El cache " + identifier + " no está inicializado");
	}

	}

	private void updateCache(String identifier) {
		switch (identifier) {
		case "productsCache":
			wlmProductService.updateCacheDeltas();
			break;
		case "dcsPriceCache":
			dcsPriceService.updateCacheDeltas();
			break;
		}

	}
}
