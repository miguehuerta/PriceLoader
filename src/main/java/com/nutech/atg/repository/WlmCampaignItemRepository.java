package com.nutech.atg.repository;


import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutech.atg.entities.WlmCampaignItem;

public interface WlmCampaignItemRepository extends JpaRepository<WlmCampaignItem, String> {

	List<WlmCampaignItem> findBySkuIdIn(Set<String> subset);

	WlmCampaignItem findBySkuIdAndStoreId(String skuId, String storeId);

}
