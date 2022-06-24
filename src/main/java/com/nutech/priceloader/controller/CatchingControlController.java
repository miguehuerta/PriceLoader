package com.nutech.priceloader.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutech.priceloader.services.CatchingControlService;

@RequestMapping("/api/v1")
@RestController
public class CatchingControlController {
	
	@Autowired
	CatchingControlService catchingControlService;

	@GetMapping("/initializeCaches")
	public void initializeCaches() {
		catchingControlService.initializeCaches();
	}
	
	@GetMapping("/populateCatching")
	public void populateCatching(String identifier) {
		catchingControlService.populateCatching(identifier);
	}
}
