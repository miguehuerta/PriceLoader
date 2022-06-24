package com.nutech.priceloader.entities;

import java.util.List;

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
@Table(name="action")
public class Action {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name="id", nullable=false)
	private long id;
	
	private String type;
	
	private String entity;
	
	private String fields; 
 	
	@ManyToOne
	Project project;
	
	@ManyToOne
	PriceListProject priceListProject;
	
	public Action() {}
}
