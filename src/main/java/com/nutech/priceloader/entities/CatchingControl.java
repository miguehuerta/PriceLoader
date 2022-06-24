package com.nutech.priceloader.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="catching_control")
public class CatchingControl {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private int id;
	private String name;
	private Long quantity;
	private boolean running;
	
	public CatchingControl() {}
	
	public CatchingControl(String name, Long quantity, boolean running) {
		super();
		this.name = name;
		this.quantity = quantity;
		this.running = running;
	}

	
}
