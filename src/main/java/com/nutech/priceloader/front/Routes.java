package com.nutech.priceloader.front;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/?app")

@Controller
public class Routes {

	@GetMapping({
		"/home",
		"/project/list",
		"/project/add",
		"/project/edit/{id}",
		"/usuario"
		
	})
	public String pages() {
		return "index";
	}
	
}
