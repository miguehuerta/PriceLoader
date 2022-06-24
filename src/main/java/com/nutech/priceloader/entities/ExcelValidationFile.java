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
@Table(name="excel_validation_file")
public class ExcelValidationFile {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name="id", nullable = false)
	private Long id;
	private boolean isError;
	
	@ManyToOne
	ExcelFile excelFile;
	
	public ExcelValidationFile() {}

	public ExcelValidationFile(ExcelFile excelFile) {
		this.excelFile=excelFile;
	}

	public ExcelValidationFile(ExcelFile excelFile, boolean isError) {
		this.excelFile=excelFile;
		this.isError=isError;
	}
	


}
