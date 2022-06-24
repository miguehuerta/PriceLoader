package com.nutech.atg.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
@Data
@Entity
@Table(name="WLM_PRODUCT")
public class WlmProduct implements Serializable{
	@Id
	@Column(name="PRODUCT_ID")
	private String productId;
	
	@Column(name="CONTENT_UOM")
	private String contentUom;
}
