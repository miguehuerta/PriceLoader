package com.nutech.atg.services;

import org.springframework.stereotype.Service;

import com.nutech.priceloader.DBConfiguration.DBContextHolder;
import com.nutech.priceloader.DBConfiguration.DBTypeEnum;

@Service
public class CatalogService {
	public String changeCatA() {
		DBContextHolder.set(DBTypeEnum.WLM_PROD_CATA);
		return DBContextHolder.getClientDatabase().toString();
	}
	
	public String changeCatB() {
		DBContextHolder.set(DBTypeEnum.WLM_PROD_CATB);
		return DBContextHolder.getClientDatabase().toString();
	}
	
	public String changeCatPreview() {
		DBContextHolder.set(DBTypeEnum.WLM_PROD_PREVIEW);
		return DBContextHolder.getClientDatabase().toString();
	}
}
