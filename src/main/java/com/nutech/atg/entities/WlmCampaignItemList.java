package com.nutech.atg.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Entity
@IdClass(CampaignItemListCompositeKey.class)
@Data
@Table(name="WLM_CAMPAIGN_ITEM_LIST")
public class WlmCampaignItemList implements  Cloneable{

	@Id
	@Column(name="SKU_ID", nullable = false)
	private String skuId;
	
	@Id
	@Column(name="STORE_ID", nullable = false)
	private String storeId;

	@Id
	@Column(name="SEQUENCE_NUM", nullable = false)
	private Integer  sequenceNum;

	@Id
	@Column(name="CAMPAIGNS", nullable = true)
	private String  campaigns;
	
	
	public WlmCampaignItemList() {}


	public WlmCampaignItemList(String skuId, String storeId, Integer sequenceNum, String campaigns) {
		super();
		this.skuId = skuId;
		this.storeId = storeId;
		this.sequenceNum = sequenceNum;
		this.campaigns = campaigns;
	}
	
    @Override
    public Object clone() throws CloneNotSupportedException {
        // this will make  a shallow copy............................
        return super.clone();
    }
	
}
/*

SKU_ID	N	VARCHAR2(40)
STORE_ID	N	VARCHAR2(40)
SEQUENCE_NUM	N	NUMBER(22)
CAMPAIGNS	Y	VARCHAR2(254)

*/