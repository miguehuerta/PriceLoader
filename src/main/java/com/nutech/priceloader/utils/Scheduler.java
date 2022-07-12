package com.nutech.priceloader.utils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.nutech.atg.services.WlmProductService;
import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.repository.CatchingControlRepository;
import com.nutech.priceloader.services.CatchingControlService;
import com.nutech.priceloader.services.ProjectService;

@Component
public class Scheduler {

	@Autowired
	ProjectService projectService;


	@Autowired
	WlmProductService wlmProductService;
	
	@Autowired
	CatchingControlService catchingControlService;


	@Autowired
	CatchingControlRepository catchingControlRepository;
/*
	
	@Scheduled(cron = "0 0/5 * * * ?")
	public void checkCacheProducts() throws InterruptedException, ExecutionException {
		ForkJoinPool.commonPool().submit(() -> {
			List<Project> projectsRunning;
			try {
				projectsRunning = projectService.getActiveProject().get();
				if (projectsRunning != null && projectsRunning.size() > 0) {
					System.out.println("Proyecto en ejecución, sin actualizar cache ahora");
				} else {
					catchingControlService.populateCatching("productsCache");

				}
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
*/


}
