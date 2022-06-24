package com.nutech.priceloader;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.nutech.priceloader.repository", entityManagerFactoryRef = "userEntityManager", transactionManagerRef = "userTransactionManager")
public class PostgresqlConfiguration {
	@Autowired
	private Environment env;
	
	@Bean
	public DataSource userDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getProperty("postgres.datasource.driver-class-name"));
		dataSource.setUrl(env.getProperty("postgres.datasource.url"));
		dataSource.setUsername(env.getProperty("postgres.datasource.username"));
		dataSource.setPassword(env.getProperty("postgres.datasource.password"));

		return dataSource;
	}


	@Bean
	public LocalContainerEntityManagerFactoryBean userEntityManager() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(userDataSource());
		em.setPackagesToScan(new String[] { "com.nutech.priceloader.entities" });

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		HashMap<String, Object> properties = new HashMap<>();
		properties.put("hibernate.hbm2ddl.auto", env.getProperty("postgres.hibernate.hbm2ddl.auto"));
		properties.put("hibernate.show_sql", env.getProperty("postgres.hibernate.show_sql"));
		properties.put("spring.datasource.testWhileIdle", env.getProperty("postgres.spring.datasource.testWhileIdle"));
		properties.put("hibernate.format_sql", env.getProperty("postgres.hibernate.format_sql"));
		properties.put("datasource.validationQuery", env.getProperty("postgres.datasource.validationQuery"));
		//properties.put("spring.jpa.hibernate.naming.implicit-strategy", env.getProperty("postgres.jpa.hibernate.naming.implicit-strategy"));
		//properties.put("hibernate.jpa.hibernate.naming.physical-strategy", env.getProperty("postgres.jpa.hibernate.naming.physical-strategy"));
		//properties.put("hibernate.batch.job.enabled", env.getProperty("postgres.batch.job.enabled"));
		em.setJpaPropertyMap(properties);

		return em;
	}


	@Bean
	public PlatformTransactionManager userTransactionManager() {

		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(userEntityManager().getObject());
		return transactionManager;
	}
}