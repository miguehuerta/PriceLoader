package com.nutech.priceloader.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.nutech.priceloader.entities.PriceListProject;
import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.repository.PriceListProjectRepository;
import com.nutech.priceloader.services.ExcelFileService;
import com.nutech.priceloader.services.PriceListProjectService;
import com.nutech.priceloader.services.ProjectService;

@RestController
@RequestMapping("/api/v1")
public class ProjectController {

	@Autowired
	ProjectService service;

	@Autowired
	PriceListProjectService priceListProjectService;
	
	@Autowired
	PriceListProjectRepository priceListProjectRepository;

	@Autowired
	ExcelFileService excelFileService;

	@Autowired
	FileController fileController;

	@PostMapping(path = "/saveProject", produces = "application/json")
	public CompletableFuture<Project> saveProject(@RequestBody Project project, Authentication auth) {
		return service.saveProject(project, auth);
	}

	@PostMapping(path = "/nextStepValidation", produces = "application/json")
	public void nextStepValidation(@RequestBody Project project, Authentication auth) {

			try {
				Project myProject = service.getProject(project.getId()).get();
				List<Project> activeProjects = service.getActiveProject().get();
				List<Project> projectsRuning = service.getProjectsRunning().get();
				int projectsRunningQuantity = projectsRuning.size();
				Long idActiveProject = activeProjects.get(0).getId();
				if (myProject.getUser().getUsername().equals(auth.getName()) && activeProjects.size() == 1
						&& idActiveProject.equals(myProject.getId())) {
					myProject.setProcessing(true);
					service.update(myProject);
					List<PriceListProject> priceListProjectNoRollback = priceListProjectRepository.findByProjectAndIsRollback(myProject, false);
					if (priceListProjectNoRollback.size() == 0) {
						priceListProjectNoRollback = priceListProjectService.setPriceListFromProject(myProject, auth.getName());
					}
					//List<PriceListProject> myPriceListsProject = priceListProjectService.setPriceListFromProject(myProject, auth.getName());

					try {
						priceListProjectService.doValidations(priceListProjectNoRollback, myProject, false, auth);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					myProject.setStep(3);
					myProject.setProcessing(false);
					service.update(myProject);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	
	@PostMapping(path = "/nextStepFinish", produces = "application/json")
	public Project nextStepFinish(@RequestBody Project project, Authentication auth) {
		Project myProject=null;
			try {
				myProject = service.getProject(project.getId()).get();
				List<Project> activeProjects = service.getActiveProject().get();
				Long idActiveProject = activeProjects.get(0).getId();
				if (myProject.getUser().getUsername().equals(auth.getName()) && activeProjects.size() == 1
						&& idActiveProject.equals(myProject.getId())) {
					myProject.setStep(4);
					service.update(myProject);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return myProject;
	}

	/*
	@PostMapping(path = "/nextStepValidation", produces = "application/json")
	public DeferredResult<ResponseEntity<?>> nextStepValidation(@RequestBody Project project, Authentication auth)
			throws InterruptedException, ExecutionException {
		DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			try {
				Project myProject = service.getProject(project.getId()).get();
				List<Project> activeProjects = service.getActiveProject().get();
				List<Project> projectsRuning = service.getProjectsRunning().get();
				int projectsRunningQuantity = projectsRuning.size();
				Long idActiveProject = activeProjects.get(0).getId();
				if (myProject.getUser().getUsername().equals(auth.getName()) && activeProjects.size() == 1
						&& idActiveProject.equals(myProject.getId()) && projectsRunningQuantity == 0) {
					myProject.setProcessing(true);
					service.update(myProject);
					List<PriceListProject> myPriceListsProject = priceListProjectService.setPriceListFromProject(myProject, auth.getName());
					priceListProjectService.doValidations(myPriceListsProject);
					myProject.setStep(3);
					myProject.setProcessing(false);
					service.update(myProject);
				}
			} catch (InterruptedException e) {
				
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			output.setResult(ResponseEntity.ok("ok"));
		});
		return output;
	}
	*/

	@PostMapping(path = "/addProjectToExecutionArea", produces = "application/json")
	public List<Project> addProjectToExecutionArea(@RequestBody Project project, Authentication auth)
			throws InterruptedException, ExecutionException {
		Project myProject = service.getProject(project.getId()).get();
		List<Project> activeProjects = service.getActiveProject().get();
		if (myProject.getUser().getUsername().equals(auth.getName()) && activeProjects.isEmpty() == true) {
			myProject.setStep(2);
			service.update(myProject);
		}
		return activeProjects;
	}

	@PostMapping(path = "/outProjectFromValidationArea", produces = "application/json")
	public CompletableFuture<List<Project>> outProjectFromValidationArea(@RequestBody Project project,
			Authentication auth) throws InterruptedException, ExecutionException {
		Project myProject = service.getProject(project.getId()).get();
		List<Project> activeProjects = service.getActiveProject().get();
		List<Project> projectsRuning = service.getProjectsRunning().get();
		int projectsRunningQuantity = projectsRuning.size();
		Long idActiveProject = activeProjects.get(0).getId();
		if (myProject.getUser().getUsername().equals(auth.getName()) && activeProjects.size() == 1
				&& idActiveProject.equals(myProject.getId()) && projectsRunningQuantity == 0) {
			myProject.setProcessing(true);
			service.update(myProject);
			
			List<PriceListProject> priceListProjectRollback = priceListProjectRepository.findByProjectAndIsRollback(myProject, true);
			if (priceListProjectRollback.size() == 0) {
				priceListProjectRollback = priceListProjectService.setPriceListFromProject(myProject, auth.getName());
			}
			//List<PriceListProject> myPriceListsProject = priceListProjectService.setPriceListFromProject(myProject, auth.getName());
			try {
				priceListProjectService.doValidations(priceListProjectRollback, myProject, true, auth);
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			priceListProjectService.deletePriceListsProjectByProject(myProject);
			myProject.setStep(2);
			myProject.setProcessing(false);
			service.update(myProject);
		}
		return service.getActiveProject();
	}

	@PostMapping(path = "/outProjectFromExecutionArea", produces = "application/json")
	public CompletableFuture<List<Project>> outProjectFromExecutionArea(@RequestBody Project project,
			Authentication auth) throws InterruptedException, ExecutionException {
		Project myProject = service.getProject(project.getId()).get();

		List<Project> activeProjects = service.getActiveProject().get();
		Long idActiveProject = activeProjects.get(0).getId();
		if (myProject.getUser().getUsername().equals(auth.getName()) && activeProjects.size() == 1
				&& idActiveProject.equals(myProject.getId())) {
			myProject.setStep(1);
			service.update(myProject);
		}
		return service.getActiveProject();
	}

	@GetMapping("/getProjectsByLoggedUserStepOne")
	public CompletableFuture<List<Project>> getProjectsByLoggedUserStepOne(Authentication auth) {
		return service.getProjectsByLoggedUserStepOne(auth);
	}

	@GetMapping(path = "/getProject/{id}")
	public CompletableFuture<Project> getProject(@PathVariable(required = false, name = "id") Long id) {
		return service.getProject(id);
	}

	@DeleteMapping(path = "/deleteProject")
	public Project deleteProject(@RequestBody Project project) throws InterruptedException, ExecutionException {
		service.deleteProject(project);
		return project;
	}

	@GetMapping(path = "/getActiveProject")
	public List<Project> getActiveProject() throws InterruptedException, ExecutionException {
		return service.getActiveProject().get();
	}

}
