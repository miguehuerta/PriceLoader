package com.nutech.atg.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nutech.atg.entities.WlmCampaignItemList;
import com.nutech.atg.repository.WlmCampaignItemListRepository;

@Service
public class WlmCampaignItemListService {

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	WlmCampaignItemListRepository wlmCampaignItemListRepository;
	
	
	@Async
	public CompletableFuture<List<WlmCampaignItemList>> saveCampaignListItemsCheckingBeforeExists(List<WlmCampaignItemList> campaignItems, String env) {
		// TODO Auto-generated method stub
		List<WlmCampaignItemList> returnedCamp= new ArrayList<>();
		if (env.equals("preview")) {
			System.out.println("Guardando nuevos wlmCampaignItemList en preview");
			final long start = System.currentTimeMillis();
			List<WlmCampaignItemList> validatedCampaignItemsListnotin = this
					.validateCampaignsListNotIn(campaignItems);
			for (WlmCampaignItemList citemList: validatedCampaignItemsListnotin) {
				try {
					wlmCampaignItemListRepository.save(citemList);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("campaignItemList "+citemList + " conn problema de fk");
					System.out.println("Eliminando y guardando nuevamente");
					
				}
			}
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
		if (env.equals("prod")) {
			System.out.println("Guardando nuevos wlmCampaignItemList en prod con reitento, y validación de que o existen");
			System.out.println("Saving a list of wlmCampaignItemList of size " + campaignItems.size() + " records");
			final long start = System.currentTimeMillis();
			catalogService.changeCatA();
			
			List<WlmCampaignItemList> validatedCampaignItemsListnotin = this
					.validateCampaignsListNotIn(campaignItems);
			for (WlmCampaignItemList citemList: validatedCampaignItemsListnotin) {
				try {
					wlmCampaignItemListRepository.save(citemList);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("campaignItemList "+citemList + " conn problema de fk en cata");
					System.out.println("Eliminando y guardando nuevamente");
					
				}
			}
			
			wlmCampaignItemListRepository.flush();
			catalogService.changeCatB();
			List<WlmCampaignItemList> validatedCampaignItemsListnotin2 = this
					.validateCampaignsListNotIn(campaignItems);
			for (WlmCampaignItemList citemList: validatedCampaignItemsListnotin2) {
				try {
					wlmCampaignItemListRepository.save(citemList);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("campaignItemList "+citemList + " conn problema de fk en catb");
				}
			}
			wlmCampaignItemListRepository.flush();
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		return CompletableFuture.completedFuture(returnedCamp);
	}
	
	
	@Async
	public CompletableFuture<List<WlmCampaignItemList>> saveCampaignListItems(List<WlmCampaignItemList> campaignItems, String env) {
		// TODO Auto-generated method stub
		List<WlmCampaignItemList> returnedCamp= new ArrayList<>();
		if (env.equals("preview")) {
			System.out.println("Guardando nuevos wlmCampaignItemList en preview");
			catalogService.changeCatPreview();
			System.out.println("Saving a list of wlmCampaignItemList of size " + campaignItems.size() + " records");
			final long start = System.currentTimeMillis();
			List<WlmCampaignItemList> validatedCampaignItemsListnotin = this
					.validateCampaignsListNotIn(campaignItems);
			returnedCamp = wlmCampaignItemListRepository.saveAll(validatedCampaignItemsListnotin);
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
		if (env.equals("prod")) {
			System.out.println("Guardando nuevos wlmCampaignItemList en prod");
			System.out.println("Saving a list of wlmCampaignItemList of size " + campaignItems.size() + " records");
			final long start = System.currentTimeMillis();
			catalogService.changeCatA();
			
			List<WlmCampaignItemList> validatedCampaignItemsListnotin = this
					.validateCampaignsListNotIn(campaignItems);
			returnedCamp = wlmCampaignItemListRepository.saveAll(validatedCampaignItemsListnotin);
			wlmCampaignItemListRepository.flush();
			catalogService.changeCatB();
			List<WlmCampaignItemList> validatedCampaignItemsListnotin2 = this
					.validateCampaignsListNotIn(campaignItems);
			returnedCamp.addAll(wlmCampaignItemListRepository.saveAll(validatedCampaignItemsListnotin2));
			wlmCampaignItemListRepository.flush();
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		return CompletableFuture.completedFuture(returnedCamp);
	}
	
	private List<WlmCampaignItemList> validateCampaignsListNotIn(List<WlmCampaignItemList> wlmCampaignItems) {
		List<WlmCampaignItemList> validatedCampaigns = new ArrayList<>();

		for (WlmCampaignItemList campaign : wlmCampaignItems) {
			WlmCampaignItemList item = wlmCampaignItemListRepository.findBySkuIdAndStoreIdAndSequenceNumAndCampaigns(campaign.getSkuId(), campaign.getStoreId(), campaign.getSequenceNum(), campaign.getCampaigns());
			if (item==null) {
				validatedCampaigns.add(campaign);
			}
		}
		return validatedCampaigns;
	}
	
	
	@Async
	public void deleteCampaignListItems(List<WlmCampaignItemList> campaignItems, String environment) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (environment.equals("preview")) {
			System.out.println("borrando delta en CampaignList en preview");
			catalogService.changeCatPreview();
			System.out.println("borrando lista de CampaignList de tamaño " + campaignItems.size() + " records");
			final long start = System.currentTimeMillis();
			wlmCampaignItemListRepository.deleteAll(campaignItems);
			wlmCampaignItemListRepository.flush();
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
		if (environment.equals("prod")) {
			System.out.println("borrando delta en CampaignList en prod");
			final long start = System.currentTimeMillis();
			catalogService.changeCatA();
			wlmCampaignItemListRepository.deleteAll(campaignItems);
			wlmCampaignItemListRepository.flush();
			catalogService.changeCatB();
			wlmCampaignItemListRepository.deleteAll(campaignItems);
			wlmCampaignItemListRepository.flush();
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
	}


	public void saveCampaignListItemsOneByOneDeletingBeforeSave(List<WlmCampaignItemList> campaignItems,
			String env) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		List<WlmCampaignItemList> returnedCamp= new ArrayList<>();
		if (env.equals("preview")) {
			System.out.println("Guardando nuevos wlmCampaignItemList en preview");
			final long start = System.currentTimeMillis();
			List<WlmCampaignItemList> validatedCampaignItemsListnotin = this
					.validateCampaignsListNotIn(campaignItems);
			for (WlmCampaignItemList citemList: validatedCampaignItemsListnotin) {
				try {
					System.out.println("Borrando item "+citemList);
					wlmCampaignItemListRepository.delete(citemList);
					System.out.println("Guardando item "+citemList);
					wlmCampaignItemListRepository.save(citemList);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("campaignItemList "+citemList + " conn problema de fk");
					System.out.println("Eliminando y guardando nuevamente");
					System.out.println("Borrando item "+citemList);
					wlmCampaignItemListRepository.delete(citemList);
					System.out.println("Guardando item "+citemList);
					wlmCampaignItemListRepository.save(citemList);
				}
			}
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
		if (env.equals("prod")) {
			System.out.println("Guardando nuevos wlmCampaignItemList en prod con reitento, y validación de que o existen");
			System.out.println("Saving a list of wlmCampaignItemList of size " + campaignItems.size() + " records");
			final long start = System.currentTimeMillis();
			catalogService.changeCatA();
			
			List<WlmCampaignItemList> validatedCampaignItemsListnotin = this
					.validateCampaignsListNotIn(campaignItems);
			for (WlmCampaignItemList citemList: validatedCampaignItemsListnotin) {
				try {
					System.out.println("Borrando item "+citemList);
					wlmCampaignItemListRepository.delete(citemList);
					System.out.println("Guardando item "+citemList);
					wlmCampaignItemListRepository.save(citemList);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("campaignItemList "+citemList + " conn problema de fk en cata");
					System.out.println("Eliminando y guardando nuevamente");
					System.out.println("Borrando item "+citemList);
					wlmCampaignItemListRepository.delete(citemList);
					System.out.println("Guardando item "+citemList);
					wlmCampaignItemListRepository.save(citemList);
				}
			}
			
			wlmCampaignItemListRepository.flush();
			catalogService.changeCatB();
			List<WlmCampaignItemList> validatedCampaignItemsListnotin2 = this
					.validateCampaignsListNotIn(campaignItems);
			for (WlmCampaignItemList citemList: validatedCampaignItemsListnotin2) {
				try {
					System.out.println("Borrando item "+citemList);
					wlmCampaignItemListRepository.delete(citemList);
					System.out.println("Guardando item "+citemList);
					wlmCampaignItemListRepository.save(citemList);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("campaignItemList "+citemList + " conn problema de fk en catb");
					System.out.println("Eliminando y guardando nuevamente");
					System.out.println("Borrando item "+citemList);
					wlmCampaignItemListRepository.delete(citemList);
					System.out.println("Guardando item "+citemList);
					wlmCampaignItemListRepository.save(citemList);
				}
			}
			wlmCampaignItemListRepository.flush();
			System.out.println("Elapsed time:" + (System.currentTimeMillis() - start));
		}
		
	}

}
