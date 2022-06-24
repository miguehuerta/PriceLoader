package com.nutech.priceloader.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutech.priceloader.services.PriceListProjectService;
import com.nutech.priceloader.utils.Ssh;

@RestController
public class tests {
	
	@Autowired
	PriceListProjectService priceListProjectService;
	
	@Autowired
	Ssh ssh;


	@GetMapping("/test")
	public String test(String uom, int price) {
		return priceListProjectService.calculateUom(uom, price);
	}
	
	@GetMapping("/sendFile")
	public void sendFile() {
		try {
			ssh.initializeClient();
			ssh.sendFile("/opt/price_loader/bcc_files_migue@gmail.com/price_loader_1655517772_0.csv", "/opt/shared/priceImport");
			ssh.sendFile("/opt/price_loader/bcc_files_migue@gmail.com/price_loader_1655495009_1.csv", "/opt/shared/priceImport");
			ssh.closeClient();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
