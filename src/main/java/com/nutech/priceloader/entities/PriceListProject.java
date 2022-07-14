package com.nutech.priceloader.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="price_list_project")
public class PriceListProject implements  Cloneable{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name="id", nullable=false)
	private Long id;
	
	@Column(name="store_nbr", nullable=false)
	private Integer StoreNbr;
	
	@Column(name="product_nbr", nullable=false)
	private Integer ProductNbr;
	
	@Column(name="base_price_reference", nullable=true)
	private Integer BasePriceReference;
	
	@Column(name="base_price_sales", nullable=true)
	private Integer BasePriceSales;
	
	@Column(name="tlm_price", nullable=true)
	private Integer tlmPrice=0;
	
	@Column(name="icon1", nullable=true)
	private String Icon1;	
	
	@Column(name="icon2", nullable=true)
	private String Icon2;	
	
	@Column(name="base_pack_size", nullable=true)
	private Integer BasePackSize;	
	
	@Column(name="base_pack_price", nullable=true)
	private Integer BasePackPrice;
	
	@Column(name="cost", nullable=true)
	private Integer cost=0;
	
	@Column(name="stock", nullable=true)
	private Integer stock=0;
	
	@Column(name="price_per_um")
	private String pricePerUm;
	
	@Column(name="validation_from", nullable=true)
	private String validationFrom;
	
	@Column(name="validation_to", nullable=true)
	private String validationTo;
	
	@Column(name="bundle_campaign", nullable=true)
	private Integer bundleCampaign;
	
	@Column(name="error", columnDefinition = "boolean default false")
	private boolean isError=false; 
	
	@Column(name="is_rollback", columnDefinition = "boolean default false")
	private boolean isRollback;
	
	@Column(name="action_performed", length=1000)
	private String actionPeformed;
	
	//Product Number;Physical Store ID;List Price;Sale Price;TLMC Price;Pack Price;Pack Size;Cost;Campaign 1;Campaign 2;PPUM;Validation From;Validation To;Stock;BundleCampaign
	
	@ManyToOne
	private Project project;
	
	public PriceListProject() {}
	
    @Override
    public Object clone() throws CloneNotSupportedException {
        // this will make  a shallow copy............................
        return super.clone();
    }
	
	public boolean getIsError() {
		return this.isError;
	}

}
