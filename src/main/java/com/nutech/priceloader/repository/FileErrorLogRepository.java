package com.nutech.priceloader.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;

import com.nutech.priceloader.entities.ExcelValidationFile;
import com.nutech.priceloader.entities.FileErrorLog;

public interface FileErrorLogRepository extends JpaRepository<FileErrorLog, Long> {
	public List<FileErrorLog> findFileErrorLogsByExcelValidationFile(ExcelValidationFile excelValidationFile);

}
