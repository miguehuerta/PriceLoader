package com.nutech.priceloader.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutech.priceloader.entities.PriceListProject;
import com.nutech.priceloader.entities.Project;

public interface PriceListProjectRepository extends JpaRepository<PriceListProject, Long>{
	
	List<PriceListProject> findByProjectAndIsRollback(Project myProject, boolean isRollback);

	List<PriceListProject> findByProject(Project myProject);

}
