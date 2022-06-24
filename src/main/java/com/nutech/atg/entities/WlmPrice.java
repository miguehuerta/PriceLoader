package com.nutech.atg.entities;

import java.sql.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "WLM_PRICE")
public class WlmPrice {
	@Id
	@Column(name = "PRICE_ID", nullable = false)
	private String priceId;

	@Column(name = "PACK_SIZE", nullable = true)
	private Integer packSize;

	@Column(name = "EFFECTIVE_FROM", nullable = true)
	private Date effectiveFrom;

	@Column(name = "EFFECTIVE_UP", nullable = true)
	private Date effectiveUp;

	@Column(name = "CAMPAIGN_MAX_AMT", nullable = true)
	private Integer campaignMaxAmt;

	@Column(name = "PRICE_PER_UM", nullable = true)
	private String pricePerUm;
	
	public WlmPrice() {
	}
}
/*
 * 
 * PRICE_ID N VARCHAR2(40) PACK_SIZE Y NUMBER(22) EFFECTIVE_FROM Y
 * TIMESTAMP(6)(11) EFFECTIVE_UP Y TIMESTAMP(6)(11) CAMPAIGN_MAX_AMT Y
 * NUMBER(22) PRICE_PER_UM Y VARCHAR2(50)
 * 
 */