package com.nutech.priceloader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;

import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.ExcelValidationFile;

public interface ExcelValidationFileRepository extends JpaRepository<ExcelValidationFile, Long>{

	ExcelValidationFile findExcelValidationFileByExcelFile(ExcelFile excelFile);

}
