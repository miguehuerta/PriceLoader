package com.nutech.atg.entities;

import java.io.Serializable;
import java.util.Objects;

public class CampaignItemCompositeKey implements Serializable{
	private String skuId;
	private String storeId;
	
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.skuId);
        hash = 59 * hash + Objects.hashCode(this.storeId);
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
        final CampaignItemCompositeKey other = (CampaignItemCompositeKey) obj;
        if (!Objects.equals(this.skuId, other.skuId)) {
            return false;
        }
        if (!Objects.equals(this.storeId, other.storeId)) {
            return false;
        }
        return true;
    }
}
