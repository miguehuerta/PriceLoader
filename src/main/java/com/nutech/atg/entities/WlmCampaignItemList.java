package com.nutech.atg.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="WLM_CAMPAIGN_ITEM_LIST")
public class WlmCampaignItemList {
	@Id
	@Column(name="SKU_ID", nullable = false)
	private String skuId;
	
	@Column(name="STORE_ID", nullable = false)
	private String storeId;
	
	@Column(name="SEQUENCE_NUM", nullable = false)
	private Integer  sequenceNum;
	
	@Column(name="CAMPAIGNS", nullable = true)
	private String  campaigns;
	
	
	public WlmCampaignItemList() {}
}
/*

SKU_ID	N	VARCHAR2(40)
STORE_ID	N	VARCHAR2(40)
SEQUENCE_NUM	N	NUMBER(22)
CAMPAIGNS	Y	VARCHAR2(254)

*/