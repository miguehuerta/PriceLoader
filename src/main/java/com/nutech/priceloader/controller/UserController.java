package com.nutech.priceloader.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
	
	@PostMapping(path="/loginUser", produces="application/json")
	public String login(String user) {
		return user;
	}
	
}
