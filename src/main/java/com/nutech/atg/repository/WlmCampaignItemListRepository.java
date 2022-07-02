package com.nutech.atg.repository;


import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutech.atg.entities.WlmCampaignItemList;

@Repository
public interface WlmCampaignItemListRepository extends JpaRepository<WlmCampaignItemList, String> {

	List<WlmCampaignItemList> findBySkuIdIn(Set<String> subset);

	WlmCampaignItemList findBySkuIdAndStoreIdAndSequenceNumAndCampaigns(String skuId, String storeId,
			Integer sequenceNum, String campaigns);

}
