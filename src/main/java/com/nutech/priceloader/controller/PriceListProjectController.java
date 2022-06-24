package com.nutech.priceloader.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.nutech.priceloader.entities.PriceListProject;
import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.services.PriceListProjectService;
import com.nutech.priceloader.services.ProjectService;


@RestController
public class PriceListProjectController {
	
	@Autowired
	PriceListProjectService priceListProjectService;
	
	@Autowired
	ProjectService projectService;
	
	@GetMapping("/export")
	public void exportToCSV(HttpServletResponse response, Long idProject, boolean isRollback) throws IOException, InterruptedException, ExecutionException {
		response.setContentType("text/csv");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateTime = dateFormatter.format(new Date());

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=priceListReport" + currentDateTime + ".csv";
		response.setHeader(headerKey, headerValue);

		Project myProject = projectService.getProject(idProject).get();
		List<PriceListProject> rows = priceListProjectService.findByProject(myProject, isRollback);

		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
		String[] csvHeader = { "id", "StoreNbr", "ProductNbr", "BasePriceReference", "BasePriceSales", "Icon1",
				"Icon2", "BasePackSize", "BasePackPrice", "pricePerUm", "isError", "actionPeformed" };

		String[] nameMapping = { "id", "StoreNbr", "ProductNbr", "BasePriceReference", "BasePriceSales", "Icon1",
				"Icon2", "BasePackSize", "BasePackPrice", "pricePerUm", "isError", "actionPeformed" };
		csvWriter.writeHeader(csvHeader);
		for (PriceListProject row : rows) {
			csvWriter.write(row, nameMapping);
		}

		csvWriter.close();
	}

}
