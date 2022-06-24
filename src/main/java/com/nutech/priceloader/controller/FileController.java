package com.nutech.priceloader.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.ExcelValidationFile;
import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.services.ExcelFileService;
import com.nutech.priceloader.services.ExcelValidationService;
import com.nutech.priceloader.services.FileErrorLogService;
import com.nutech.priceloader.services.ProjectService;

@RestController
@RequestMapping("/api/v1")
public class FileController {
	
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	ExcelFileService excelFileService;
	
	@Autowired
	ExcelValidationService excelValidationService;
	
	@Autowired
	FileErrorLogService fileErrorLogService;
	
	@Autowired
	private  AutowireCapableBeanFactory autowireCapableBeanFactory;

	
	@PostMapping("/addExcelFile/{idProject}")
	public ExcelValidationFile addExcelFile(MultipartFile file, Authentication authentication, @PathVariable(required=true,name="idProject") Long idProject) throws InterruptedException, ExecutionException  {
		ExcelFileService myExcelFileService = new ExcelFileService();
		autowireCapableBeanFactory.autowireBean(myExcelFileService);
		return myExcelFileService.addExcelFile(authentication, file, idProject);
	}
	
	
	@GetMapping("/getExcelFilesByProjectId/{idProject}")
	public CompletableFuture<List<ExcelFile>> getExcelFilesByProjectId(@PathVariable(required=true, name="idProject") Long idProject) throws InterruptedException, ExecutionException{
		Project project = projectService.getProject(idProject).get();
		return excelFileService.getExcelFilesByProject(project);
	}
	
	@DeleteMapping(path="/deleteExcelFile", produces="application/json")
	public ExcelFile deleteExcelFile(@RequestBody ExcelFile excelFile) throws InterruptedException, ExecutionException {
		excelFileService.deleteExcelFile(excelFile);
		return excelFile;
	}
	
}
