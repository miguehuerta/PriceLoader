package com.nutech.atg.entities;


import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="DCS_PRICE")
public class DcsPrice {
	@Id
	@Column(name="PRICE_ID", nullable = false)
	private String priceId;
	
	@Column(name="VERSION", nullable = false)
	private int version;
	
	@Column(name="PRICE_LIST", nullable = false)
	private String  priceList;
	
	@Column(name="PRODUCT_ID", nullable = true)
	private String  productId;
	
	@Column(name="SKU_ID", nullable = true)
	private String skuId;
	
	@Column(name="PARENT_SKU_ID", nullable = true)
	private String  parentSkuId;
	
	@Column(name="PRICING_SCHEME", nullable = false)
	private Integer  pricingScheme;
	
	@Column(name="LIST_PRICE", nullable = true)
	private Integer  listPrice;
	
	@Column(name="COMPLEX_PRICE", nullable = true)
	private String complexPrice;
	
	@Column(name="START_DATE", nullable = true)
	private LocalDateTime  startDate;
	
	@Column(name="END_DATE", nullable = true)
	private LocalDateTime  endDate;


	
	public DcsPrice() {}
}
/*

PRICE_ID	N	VARCHAR2(40)
VERSION	N	NUMBER(22)
PRICE_LIST	N	VARCHAR2(40)
PRODUCT_ID	Y	VARCHAR2(40)
SKU_ID	Y	VARCHAR2(40)
PARENT_SKU_ID	Y	VARCHAR2(40)
PRICING_SCHEME	N	NUMBER(22)
LIST_PRICE	Y	NUMBER(22)
COMPLEX_PRICE	Y	VARCHAR2(40)
START_DATE	Y	TIMESTAMP(6)(11)
END_DATE	Y	TIMESTAMP(6)(11)

*/