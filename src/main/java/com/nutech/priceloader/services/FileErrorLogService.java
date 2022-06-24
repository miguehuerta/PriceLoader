package com.nutech.priceloader.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nutech.priceloader.entities.ExcelValidationFile;
import com.nutech.priceloader.entities.FileErrorLog;
import com.nutech.priceloader.repository.FileErrorLogRepository;

@Service
public class FileErrorLogService {
	@Autowired
	FileErrorLogRepository fileErrorLogRepo;
	
	public void deleteErorLogGroup(List<FileErrorLog> fileErrorslog) {
		fileErrorLogRepo.deleteAll(fileErrorslog);
	}
	
	@Async
	public CompletableFuture<FileErrorLog> getFileErroLog(Long id) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(fileErrorLogRepo.getById(id));
	}

	@Async
	public CompletableFuture<List<FileErrorLog>> getFileErrorLogsByExcelValidationFile(ExcelValidationFile excelValidationFile) {
		// TODO Auto-generated method stub
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(fileErrorLogRepo.findFileErrorLogsByExcelValidationFile(excelValidationFile));
	}
	

	public void deleteGrouFileErrorLog(List<FileErrorLog> fileErrorLogs) {
		fileErrorLogRepo.deleteAll(fileErrorLogs);
		
	}

	@Async
	public CompletableFuture<List<FileErrorLog>> saveList(List<FileErrorLog> errors) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		// TODO Auto-generated method stub
		return CompletableFuture.completedFuture(fileErrorLogRepo.saveAll(errors));
	}

}
