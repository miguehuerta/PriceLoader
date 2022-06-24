package com.nutech.priceloader.controller;



import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.ExcelValidationFile;
import com.nutech.priceloader.services.ExcelFileService;
import com.nutech.priceloader.services.ExcelValidationService;

@RequestMapping("/api/v1")
@RestController
public class ExcelValidationFileController {
	@Autowired
	ExcelValidationService excelValidationService;
	
	@Autowired
	ExcelFileService excelFileService;

	@GetMapping("/getExcelValidationFileByExcelFileId/{idFile}")
	public ExcelValidationFile getExcelValidationFileByExcelFileId(
			@PathVariable(required = true, name = "idFile") Long idFile) throws InterruptedException, ExecutionException {
		ExcelFile excelFile = excelFileService.getExcelFile(idFile).get();
		return excelValidationService.getExcelValidationFileByExcelFile(excelFile).get();
	}
}
