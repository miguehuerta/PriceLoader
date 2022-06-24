package com.nutech.priceloader.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {
	@Bean(name="taskExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(12);
		executor.setMaxPoolSize(12);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("userThread-");
		executor.initialize();
		return executor;
	}
}