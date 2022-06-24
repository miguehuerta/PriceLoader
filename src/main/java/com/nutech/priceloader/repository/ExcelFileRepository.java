package com.nutech.priceloader.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;

import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.Project;

public interface ExcelFileRepository extends JpaRepository<ExcelFile, Long>{
	
	@Query("SELECT p FROM ExcelFile p WHERE p.project = :project")
	public List<ExcelFile> findExcelFilesByProjectId(@Param("project") Project project);

	@Query("SELECT p FROM ExcelFile p WHERE p.project = :project and p.location = :location")
	public ExcelFile getExcelFileByProjectAndName(@Param("project") Project project, @Param("location") String location);

	@Query("SELECT p FROM ExcelFile p WHERE p.project = :project and p.fileName = :fileName")
	public List<ExcelFile> getExcelFilesByProjectAndName(@Param("project") Project project, @Param("fileName") String fileName);

}
