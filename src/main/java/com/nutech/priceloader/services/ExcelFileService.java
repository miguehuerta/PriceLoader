package com.nutech.priceloader.services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.ExcelValidationFile;
import com.nutech.priceloader.entities.FileErrorLog;
import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.repository.ExcelFileRepository;
import com.nutech.priceloader.repository.ProjectRepository;
import com.nutech.priceloader.utils.Helpers;

import lombok.Data;

@Service
@Data
public class ExcelFileService {
	@Autowired
	ExcelFileRepository repo;
	
	@Autowired
	ExcelValidationService excelValidationService;
	
	@Autowired
	FileErrorLogService fileErrorLogService;
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	ExcelFilelValidationHelper excelFileValidationHelper;
	
	@Autowired
	Helpers helpers;

	@Async
	public CompletableFuture<List<ExcelFile>> getExcelFiles(){
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(repo.findAll());
	}
	
	
	@Async
	public CompletableFuture<List<ExcelFile>> getExcelFilesByProject (Project project){
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(repo.findExcelFilesByProjectId(project));
	}
	
	@Async
    public CompletableFuture<ExcelFile> getExcelFile(long id) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
        return CompletableFuture.completedFuture(repo.findById(id).get());
    }

	@Async
    public CompletableFuture<ExcelFile> addExcelFile(ExcelFile listElement) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(repo.save(listElement));
    }
    
	@Async
    public CompletableFuture<ExcelFile> updateExcelFile(ExcelFile ExcelFile) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(repo.save(ExcelFile));
    }


    public void deleteExcelFile(long id) {
		repo.deleteById(id);
    }

	
	

	public void deleteExcelFile(ExcelFile excelFile) throws InterruptedException, ExecutionException {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		ExcelFile myExcelFile = this.getExcelFile(excelFile.getId()).get();
		ExcelValidationFile excelValidationFile = excelValidationService.getExcelValidationFileByExcelFile(myExcelFile).get();

		helpers.deleteFsFile(myExcelFile.getLocation());
		
		if (excelValidationFile!=null) {
			List<FileErrorLog> errors = fileErrorLogService.getFileErrorLogsByExcelValidationFile(excelValidationFile).get();
			if(!errors.isEmpty())
				fileErrorLogService.deleteErorLogGroup(errors);
			excelValidationService.deleteExcelValidation(excelValidationFile);
		}
		
		this.deleteExcelFile(myExcelFile.getId());
		Project project = myExcelFile.getProject();
		project.setUpdatedDate(helpers.getCurrentDate());
		projectService.update(project);
	}


	public void deleteExcelFiles(List<ExcelFile> oldFiles, boolean deleteFsFile) throws InterruptedException, ExecutionException {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		for (ExcelFile excelFile : oldFiles) {
			this.deleteExcelFile(excelFile);
		}
	}

	@Async
	public ExcelValidationFile addExcelFile(Authentication authentication, MultipartFile file, Long idProject) throws InterruptedException, ExecutionException {
		String fileName = file.getOriginalFilename();
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		Project myProject = projectService.updateDate(projectService.getProject(idProject).get()).get();
		excelFileValidationHelper.initialize(authentication, file);
		String fileLocation = excelFileValidationHelper.getFileLocation();
		this.removeRepeatedExcelFiles(myProject, fileName);
		ExcelFile excelFile = new ExcelFile( helpers.getCurrentDate(), fileLocation, myProject, fileName);
		ExcelFile savedExcelFile = this.addExcelFile(excelFile).get();
		ExcelValidationFile validationFile = new ExcelValidationFile(savedExcelFile, false);
		ExcelValidationFile myExcelValidationFile = excelValidationService.save(validationFile).get();
		List<FileErrorLog> errors= excelFileValidationHelper.validate(myExcelValidationFile);
		if(errors!=null && errors.size()>0) {
			fileErrorLogService.saveList(errors);
			myExcelValidationFile.setError(true);
			excelValidationService.save(myExcelValidationFile);
		}
		return myExcelValidationFile;
	}

	private void removeRepeatedExcelFiles(Project myProject, String fileName) throws InterruptedException, ExecutionException {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		List<ExcelFile> exFiles = repo.getExcelFilesByProjectAndName(myProject, fileName);
		if (!exFiles.isEmpty()) {
			this.deleteExcelFiles(exFiles, false);
		}
			
	}
}
