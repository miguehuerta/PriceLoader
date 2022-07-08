package com.nutech.priceloader.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
	public void test() {
	    List<CompletableFuture<Integer>> futures = new ArrayList<>();

	    for (int i = 0; i < 21; i++) {
	        int finalI = i;
	        futures.add(CompletableFuture.supplyAsync(() -> {
	            System.out.println(finalI);
	            return 3;
	        }));
	    }

	    System.out.println("before running all of");
	    CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[3]));
	    try {
	        allOf.get();
	    } catch (InterruptedException | ExecutionException e) {
	        e.printStackTrace();
	    }
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
