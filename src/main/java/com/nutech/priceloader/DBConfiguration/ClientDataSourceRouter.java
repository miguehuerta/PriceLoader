package com.nutech.priceloader.DBConfiguration;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ClientDataSourceRouter extends AbstractRoutingDataSource{

	@Override
	protected Object determineCurrentLookupKey() {
		// TODO Auto-generated method stub
		return DBContextHolder.getClientDatabase();
	}

}
