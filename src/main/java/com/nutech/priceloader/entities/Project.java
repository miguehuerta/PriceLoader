package com.nutech.priceloader.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


import lombok.Data;

@Data
@Entity
@Table(name="project", schema = "public")
public class Project {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name="id", nullable = false)
	private Long id;
	
	@Column(name="name", nullable = false)
	private String name;
	
	@Column(name="updated_date", nullable = false)
	private LocalDateTime updatedDate;
	
	@Column(name="creation_date", nullable = false)
	private LocalDateTime creationDate;
	
	@Column(name="step", nullable = false)
	private int step;
	
	@Column(name="immediate_change", nullable=false)
	private boolean immediateChange;
	
	@Column(name="start_date", nullable=true)
	private LocalDateTime startDate;
	
	@Column(name="end_date", nullable=true)
	private LocalDateTime endDate;
	
	@Column(name="is_processing", columnDefinition = "boolean default false")
	private boolean isProcessing = false;
	
	@Column(name="processed_percentage", nullable=true)
	private int processedPercentage;
	
	@Column(name="state")
	private String state;
	
	@ManyToOne
	private User user;
	
	
	public Project() {}

	public Project(String name, LocalDateTime updatedDate, LocalDateTime creationDate, int step, User user) {
		super();
		this.name = name;
		this.updatedDate = updatedDate;
		this.creationDate = creationDate;
		this.step = step;
		this.user = user;
	}
	

	
}
