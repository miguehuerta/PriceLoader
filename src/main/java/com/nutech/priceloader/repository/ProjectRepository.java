package com.nutech.priceloader.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;

import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.entities.User;

public interface ProjectRepository extends JpaRepository<Project, Long>{
	
    @Query("SELECT p FROM Project p WHERE p.step= :step and p.user = :user")
    public List<Project> findProjectsByUserIdAndStep(@Param("user") User user, @Param("step") int step);

    @Query("SELECT p FROM Project p WHERE p.step>1 AND p.step<4")
	public List<Project> findActiveProject();

    @Query("SELECT p FROM Project p WHERE p.isProcessing=true")
	public List<Project> findProjectsRunning();
}