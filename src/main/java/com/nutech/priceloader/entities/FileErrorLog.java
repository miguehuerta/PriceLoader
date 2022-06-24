package com.nutech.priceloader.entities;

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
@Table(name="file_error")
public class FileErrorLog {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name="id", nullable = false)
	Long id;
	String message;
	
	@ManyToOne
	ExcelValidationFile excelValidationFile;

	public FileErrorLog() {}
	
	public FileErrorLog(String message, ExcelValidationFile excelFile) {
		super();
		this.message = message;
		this.excelValidationFile = excelFile;
	}

}
