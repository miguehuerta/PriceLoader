package com.nutech.priceloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.nutech.priceloader.entities.User;
import com.nutech.priceloader.repository.UserRepository;

@SpringBootApplication(scanBasePackages={"com.nutech.priceloader", "com.nutech.atg"})
@EnableCaching
@EnableScheduling
public class PriceLoader2Application implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(PriceLoader2Application.class, args);
	}
	
	@Autowired
	private UserRepository repository;

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		



		User migue = repository.getUserByUsername("migue@gmail.com");
		
		if (migue==null) {
			BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
			String encodedPassword = bCryptPasswordEncoder.encode("nimda");
			migue = new User("Miguel", "Huerta", "migue@gmail.com", encodedPassword, "ROLE_ADMIN", true);
			repository.save(migue);
		}
		
	}

}
