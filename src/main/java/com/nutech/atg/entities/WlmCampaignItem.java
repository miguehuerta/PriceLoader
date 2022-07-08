package com.nutech.atg.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;


@Entity
@IdClass(CampaignItemCompositeKey.class)
@Data
@Table(name="WLM_CAMPAIGN_ITEM")
public class WlmCampaignItem{
	@Id
	@Column(name="SKU_ID", nullable = false)
	private String skuId;
	@Id
	@Column(name="STORE_ID", nullable = false)
	private String storeId;

	@Column(name="COST", nullable = true)
	private String  cost;

	@Column(name="BUNDLE_CAMPAIGN", nullable = true)
	private String  bundleCampaign;
	
	
	public WlmCampaignItem() {}

	public WlmCampaignItem(String skuId, String storeId) {
		super();
		this.skuId = skuId;
		this.storeId = storeId;
	}
}
/*

SKU_ID	N	VARCHAR2(40)
STORE_ID	N	VARCHAR2(40)
COST	Y	VARCHAR2(100)
BUNDLE_CAMPAIGN	Y	VARCHAR2(100)

*/