package com.nutech.priceloader.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;


@ConfigurationProperties(prefix="excel")
@Configuration
@Data
public class  ExcelFileConstants{
	private String locationExcelFiles;
	private String storeField;
	private String productField;
	private String basePriceField;
	private String salePriceField;
	private String icon1Field;
	private String icon2Field;
	private String packSize;
	private String packPrice;
	public ExcelFileConstants() {}
	
	public List<CellType> STR(){
		List<CellType> types = new ArrayList<CellType>();
		types.add(CellType.STRING);
		return types;
	}
	
	public List<CellType> NUM(){
		List<CellType> types = new ArrayList<CellType>();
		types.add(CellType.NUMERIC);
		return types;
	}
	
	public List<CellType> STR_OR_EMPTY(){
		List<CellType> types = new ArrayList<CellType>();
		types.add(CellType.STRING);
		types.add(CellType.BLANK);
		return types;
	}
	
	public List<CellType> NUM_OR_EMPTY(){
		List<CellType> types = new ArrayList<CellType>();
		types.add(CellType.NUMERIC);
		types.add(CellType.BLANK);
		return types;
	}
}