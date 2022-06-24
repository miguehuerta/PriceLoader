package com.nutech.priceloader.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.MyUserDetails;
import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.entities.User;
import com.nutech.priceloader.repository.ExcelFileRepository;
import com.nutech.priceloader.repository.ProjectRepository;
import com.nutech.priceloader.utils.Helpers;

@Service
public class ProjectService {
	@Autowired 
	ProjectRepository projectRepo;
	
	private @Autowired
	AutowireCapableBeanFactory beanFactory;
	
	@Autowired
	ExcelFilelValidationHelper exHelper;
	
	@Autowired
	private Helpers helpers;
	
	@Autowired
	ExcelFileRepository excelRepo;
	
	@Async
	public CompletableFuture<Project> getProject(Long id) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		Project project = projectRepo.findById(id).get();
		return CompletableFuture.completedFuture(project);
	}
	
	@Async
	public CompletableFuture<Project> saveProject(Project project, Authentication auth) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		Helpers helpers = new Helpers();
		LocalDateTime currentDate = helpers.getCurrentDate();
		MyUserDetails userD = (MyUserDetails)auth.getPrincipal();
		User user = userD.getUser();
		int step = 1;
		project.setCreationDate(currentDate);
		project.setUpdatedDate(currentDate);
		project.setUser(user);
		project.setStep(step);
		return CompletableFuture.completedFuture(projectRepo.save(project));
	}
	
	@Async
	public CompletableFuture<List<Project>> getProjectsByLoggedUserStepOne(Authentication auth){
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		MyUserDetails userD = (MyUserDetails)auth.getPrincipal();
		User user = userD.getUser();
		return CompletableFuture.completedFuture(projectRepo.findProjectsByUserIdAndStep(user, 1));
	}
	
	@Async
	public void deleteProject(Project project) throws InterruptedException, ExecutionException {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		Project myProject = this.getProject(project.getId()).get();
		
		ExcelFileService excelFileService = new ExcelFileService();
		beanFactory.autowireBean(excelFileService);
		@SuppressWarnings("unchecked")
		List<ExcelFile> files = (List<ExcelFile>) excelFileService.getExcelFilesByProject(myProject).get();
		for (ExcelFile file : files) {
			excelFileService.deleteExcelFile(file);
		}
		projectRepo.delete(myProject);
		
	}

	@Async
	public CompletableFuture<Project> update(Project project) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		return CompletableFuture.completedFuture(projectRepo.save(project));
	}

	@Async
	public CompletableFuture<Project> updateDate(Project project) {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		project.setUpdatedDate(helpers.getCurrentDate());
		return CompletableFuture.completedFuture(projectRepo.save(project));
	}

	@Async
	public CompletableFuture<List<Project>> getActiveProject() {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		List<Project> projects = projectRepo.findActiveProject();
		return CompletableFuture.completedFuture(projects);
	}

	@Async
	public CompletableFuture<List<Project>> getProjectsRunning() {
		System.out.println("Thread used for this is "+Thread.currentThread().getName());
		// TODO Auto-generated method stub
		return CompletableFuture.completedFuture(projectRepo.findProjectsRunning());
	}
	
	public void setProgress(Project project, int progress) {
		project.setProcessedPercentage(progress);
		projectRepo.save(project);
	}

	public void setState(Project project, String state) {
		project.setState(state);
		projectRepo.save(project);
	}
}
