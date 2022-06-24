package com.nutech.atg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutech.atg.services.DynamoService;
import com.nutech.priceloader.entities.Project;

@RestController
@RequestMapping("/api/atg/v1")
public class DynamoContoller {
	
	@Autowired
	DynamoService dynamoService;
	
	@GetMapping("/changeToCata")
	public Boolean changeToCata() {
		return dynamoService.changeToCata();
	}
	
	@GetMapping("/changeToCatb")
	public Boolean changeToCatb() {
		return dynamoService.changeToCatb();
	}
	
	@GetMapping("/changeCatalog")
	public String changeCatalog() {
		return dynamoService.changeCatalog();
	}
	
	@GetMapping("/currentCatalog")
	public String currentCatalog() {
		return dynamoService.getCurrentCatalog();
	}
	
	@GetMapping("/queryCampaign")
	public void queryCampaign(String xml) {
		dynamoService.changeToCata();
		dynamoService.executeXmlCampaign(xml);
	}
	
	@GetMapping("/queryPrice")
	public void queryPrice(String xml) {
		dynamoService.changeToCata();
		dynamoService.executeXmlPriceList(xml);
	}
	
	@GetMapping("/pruebaDelete")
	public String pruebaDelete(String xml) {
		dynamoService.changeToCata();
		return dynamoService.executeXmlPriceList("<remove-item id=\"p20117483\" item-descriptor=\"price\" />");
	}	
	
}
