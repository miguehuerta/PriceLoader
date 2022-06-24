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
@Table(name="excel_file")
@Entity
public class ExcelFile {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name="id", nullable=false)
	private long id;
	
	@Column(name="uploaded_date", nullable=false)
	private LocalDateTime uploadedDate;
	
	@Column(name="location", nullable=false)
	private String location;
	
	@Column(name="file_name")
	private String fileName;
	
	@ManyToOne
	private Project project;
	
	public ExcelFile() {
		
	}

	public ExcelFile(LocalDateTime uploadedDate, String location, Project project, String fileName) {
		super();
		this.uploadedDate = uploadedDate;
		this.location = location;
		this.project = project;
		this.fileName = fileName;
	}
}
