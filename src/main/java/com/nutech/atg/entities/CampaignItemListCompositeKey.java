package com.nutech.atg.entities;

import java.io.Serializable;
import java.util.Objects;


public class CampaignItemListCompositeKey implements Serializable{
	private String skuId;
	private String storeId;
	private Integer  sequenceNum;
	private String  campaigns;
	
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.skuId);
        hash = 59 * hash + Objects.hashCode(this.storeId);
        hash = 59 * hash + Objects.hashCode(this.sequenceNum);
        hash = 59 * hash + Objects.hashCode(this.campaigns);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CampaignItemListCompositeKey other = (CampaignItemListCompositeKey) obj;
        if (!Objects.equals(this.skuId, other.skuId)) {
            return false;
        }
        if (!Objects.equals(this.storeId, other.storeId)) {
            return false;
        }
        if (!Objects.equals(this.sequenceNum, other.sequenceNum)) {
            return false;
        }
        if (!Objects.equals(this.campaigns, other.campaigns)) {
            return false;
        }
        return true;
    }
}
