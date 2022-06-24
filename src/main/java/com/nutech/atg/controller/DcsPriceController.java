package com.nutech.atg.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutech.atg.entities.DcsPrice;
import com.nutech.atg.services.DcsPriceService;

@RestController
@RequestMapping("/api/atg/v1")
public class DcsPriceController {
	
	@Autowired
	DcsPriceService dcsPriceService;
	
	@GetMapping("checkProduct")
	public List<DcsPrice> checkProduct() {
		return dcsPriceService.getPriceBySkuId("981107");
	}
}
