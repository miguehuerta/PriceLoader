package com.nutech.priceloader.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutech.priceloader.entities.ExcelValidationFile;
import com.nutech.priceloader.entities.FileErrorLog;
import com.nutech.priceloader.services.ExcelValidationService;
import com.nutech.priceloader.services.FileErrorLogService;

@RequestMapping("/api/v1")
@RestController
public class FileErrorLogController {
	
	@Autowired
	ExcelValidationService excelValidationService;
	
	@Autowired
	FileErrorLogService fileErrorLogService;

	@GetMapping("/getFileErrorLogsByExcelValidationFileId/{excelValidationFileId}")
	public CompletableFuture<List<FileErrorLog>> getFileErrorLogsByExcelValidationFileId(
			@PathVariable(required = true, name = "excelValidationFileId") Long excelValidationFileId) throws InterruptedException, ExecutionException {
		ExcelValidationFile excelValidationFile = excelValidationService.getExcelValidationFile(excelValidationFileId).get();
		return fileErrorLogService.getFileErrorLogsByExcelValidationFile(excelValidationFile);
	}
}
