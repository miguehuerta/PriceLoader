package com.nutech.priceloader;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.nutech.priceloader.DBConfiguration.DBTypeEnum;
import com.nutech.priceloader.DBConfiguration.MultiRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.nutech.atg.repository",
        entityManagerFactoryRef = "multiEntityManager",
        transactionManagerRef = "multiTransactionManager"
)
public class MultiCatalogOracleConfiguration {
	 private final String PACKAGE_SCAN = "com.nutech.atg.entities";
	    @Primary
	    @Bean(name = "CATA")
	    @ConfigurationProperties(prefix="oracle.datasource.cata")
	    public DataSource clientADataSource() {
	        return DataSourceBuilder.create().type(HikariDataSource.class).build();
	    }
	    @Bean(name = "CATB")
	    @ConfigurationProperties(prefix="oracle.datasource.catb")
	    public DataSource clientBDataSource() {
	        return DataSourceBuilder.create().type(HikariDataSource.class).build();
	    }
	    @Bean(name = "PREVIEW")
	    @ConfigurationProperties(prefix="oracle.datasource.preview")
	    public DataSource clientPreviewDataSource() {
	        return DataSourceBuilder.create().type(HikariDataSource.class).build();
	    }
	    @Bean(name = "multiRoutingDataSource")
	    public DataSource multiRoutingDataSource() {
	        Map<Object, Object> targetDataSources = new HashMap<>();
	        targetDataSources.put(DBTypeEnum.WLM_PROD_CATA, clientADataSource());
	        targetDataSources.put(DBTypeEnum.WLM_PROD_CATB, clientBDataSource());
	        targetDataSources.put(DBTypeEnum.WLM_PROD_PREVIEW, clientPreviewDataSource());
	        MultiRoutingDataSource multiRoutingDataSource = new MultiRoutingDataSource();
	        multiRoutingDataSource.setDefaultTargetDataSource(clientADataSource());
	        multiRoutingDataSource.setTargetDataSources(targetDataSources);
	        return multiRoutingDataSource;
	    }
	    @Bean(name = "multiEntityManager")
	    public LocalContainerEntityManagerFactoryBean multiEntityManager() {
	        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
	        em.setDataSource(multiRoutingDataSource());
	        em.setPackagesToScan(PACKAGE_SCAN);
	        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
	        em.setJpaVendorAdapter(vendorAdapter);
	        em.setJpaProperties(hibernateProperties());
	        return em;
	    }
	    
	    @Bean(name = "multiTransactionManager")
	    public PlatformTransactionManager multiTransactionManager() {
	        JpaTransactionManager transactionManager
	                = new JpaTransactionManager();
	        transactionManager.setEntityManagerFactory(
	                multiEntityManager().getObject());
	        return transactionManager;
	    }
	    
	    @Primary
	    @Bean(name = "dbSessionFactory")
	    public LocalSessionFactoryBean dbSessionFactory() {
	        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
	        sessionFactoryBean.setDataSource(multiRoutingDataSource());
	        sessionFactoryBean.setPackagesToScan(PACKAGE_SCAN);
	        sessionFactoryBean.setHibernateProperties(hibernateProperties());
	        return sessionFactoryBean;
	    }
	    private Properties hibernateProperties() {
	        Properties properties = new Properties();
	        properties.put("hibernate.show_sql", false);
	        properties.put("hibernate.format_sql", false);
	        properties.put("hibernate.jdbc.fetch_size", 10000);
	        properties.put("hibernate.jdbc.batch_size", 30);
	        properties.put("hibernate.order_updates",true);
	        return properties;
	    }
}
