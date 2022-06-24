package com.nutech.atg.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="WLM_CAMPAIGN_ITEM")
public class WlmCampaignItem {
	@Id
	@Column(name="SKU_ID", nullable = false)
	private String skuId;
	
	@Column(name="STORE_ID", nullable = false)
	private String storeId;
	
	@Column(name="COST", nullable = true)
	private String  cost;
	
	@Column(name="BUNDLE_CAMPAIGN", nullable = true)
	private String  bundleCampaign;
	
	@OneToOne
	WlmCampaignItemList wlmCampaignItemList;
	
	public WlmCampaignItem() {}
}
/*

SKU_ID	N	VARCHAR2(40)
STORE_ID	N	VARCHAR2(40)
COST	Y	VARCHAR2(100)
BUNDLE_CAMPAIGN	Y	VARCHAR2(100)

*/