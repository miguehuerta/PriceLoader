package com.nutech.priceloader.services;


import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.ExcelValidationFile;
import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.repository.ExcelFileRepository;
import com.nutech.priceloader.repository.ExcelValidationFileRepository;
import com.nutech.priceloader.repository.FileErrorLogRepository;

import lombok.Data;

@Service
@Data
public class ExcelValidationService {

	private Project project;
	
	private @Autowired
	AutowireCapableBeanFactory beanFactory;

	@Autowired
	ExcelFileRepository excelFileRepo;
	
	@Autowired
	ExcelValidationFileRepository excelValidationFileRepo;
	
	@Autowired
	FileErrorLogRepository fileErrorLogRepository;


	public ExcelValidationService() {
	}
	
	public void deleteExcelValidation(ExcelValidationFile excelValidationFile) {
		excelValidationFileRepo.delete(excelValidationFile);
	}
	
	@Async
	public CompletableFuture<ExcelValidationFile> getExcelValidationFile(Long excelValidationFileId) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(excelValidationFileRepo.getById(excelValidationFileId));
	}
	
	@Async
	public CompletableFuture<ExcelValidationFile> getExcelValidationFileByExcelFile(ExcelFile excelFile) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(excelValidationFileRepo.findExcelValidationFileByExcelFile(excelFile));
	}

	@Async
	public CompletableFuture<ExcelValidationFile> save(ExcelValidationFile validationFile) {
		// TODO Auto-generated method stub
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(excelValidationFileRepo.save(validationFile));
	}
}
