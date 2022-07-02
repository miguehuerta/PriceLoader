package com.nutech.atg.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nutech.atg.entities.WlmCampaignItem;
import com.nutech.atg.repository.WlmCampaignItemRepository;

@Service
public class WlmCampaignItemService {

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	WlmCampaignItemRepository wlmCampaignItemRepository;
	
	@Async
	public CompletableFuture<List<WlmCampaignItem>> saveCampaignItems(List<WlmCampaignItem> campaignItems, String env) {
		// TODO Auto-generated method stub
		List<WlmCampaignItem> returnedCamp= new ArrayList<>();
		if (env.equals("preview")) {
			System.out.println("Guardando nuevos wlmCampaignItem en preview");
			catalogService.changeCatPreview();
			catalogService.changeCatA();
			List<WlmCampaignItem> validatedWlmCampaignItemsnotin = this
					.validateCampaignNotIn(campaignItems);
			returnedCamp = wlmCampaignItemRepository.saveAll(validatedWlmCampaignItemsnotin);
			System.out.println("Saving a list of wlmCampaignItem of size " + campaignItems.size() + " records");
			final long start = System.currentTimeMillis();
			returnedCamp = wlmCampaignItemRepository.saveAll(campaignItems);
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
		if (env.equals("prod")) {
			System.out.println("Guardando nuevos wlmCampaignItem en prod");
			System.out.println("Saving a list of wlmCampaignItem of size " + campaignItems.size() + " records");
			final long start = System.currentTimeMillis();
			
			catalogService.changeCatA();
			List<WlmCampaignItem> validatedWlmCampaignItemsnotin = this
					.validateCampaignNotIn(campaignItems);
			returnedCamp = wlmCampaignItemRepository.saveAll(validatedWlmCampaignItemsnotin);
			wlmCampaignItemRepository.flush();
			
			catalogService.changeCatA();
			List<WlmCampaignItem> validatedWlmCampaignItemsnotin2 = this
					.validateCampaignNotIn(campaignItems);
			returnedCamp.addAll(wlmCampaignItemRepository.saveAll(validatedWlmCampaignItemsnotin2));
			wlmCampaignItemRepository.flush();
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		return CompletableFuture.completedFuture(returnedCamp);
	}
	
	private List<WlmCampaignItem> validateCampaignNotIn(List<WlmCampaignItem> wlmCampaignItems) {
		List<WlmCampaignItem> validatedCampaigns = new ArrayList<>();
		for (WlmCampaignItem campaign : wlmCampaignItems) {
			WlmCampaignItem item = wlmCampaignItemRepository.findBySkuIdAndStoreId(campaign.getSkuId(), campaign.getStoreId());
			if(item==null) {
				validatedCampaigns.add(campaign);
			}
		}

		return validatedCampaigns;
	}

}
