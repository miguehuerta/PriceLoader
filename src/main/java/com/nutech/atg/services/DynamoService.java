package com.nutech.atg.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import com.nutech.atg.repository.DcsPriceRepository;
import com.nutech.priceloader.utils.Helpers;
import com.nutech.priceloader.utils.Http;

import lombok.Data;

@Service
@ConfigurationProperties(prefix="dynamo")
@Configuration
@Data
public class DynamoService {
	private String staging_user;
	private String staging_pass;
	private String staging_url;
	private String prod_user;
	private String prod_pass;
	private String prod_url;
	
	@Value("${environment}")
	private String environment;
	@Autowired
	DcsPriceRepository dcsPriceRepository;
	
	@Autowired
	Helpers helpers;

	public String getCurrentCatalog() {
		String responseText = "";
		String cat = "";
		
		try {
			Http http = new Http(); 
			String url="";
			if (environment.equals("preview")) {
				http.setUsername(staging_user);
				http.setPassword(staging_pass);
				url=staging_url;
			}
			if (environment.equals("prod")) {
				http.setUsername(prod_user);
				http.setPassword(prod_pass);
				url=prod_url;
			}
			responseText = http.sendGET(
					url+"/dyn/admin/atg/commerce/admin/en/catalog/PrepareToSwitchProductCatalog.jhtml");
			int isCatB = responseText.indexOf("The data source currently being used is     <b>DataSourceB");
			if (isCatB != -1) {
				cat = "CATB";
			}
			int isCatA = responseText.indexOf("The data source currently being used is     <b>DataSourceA");
			if (isCatA != -1) {
				cat = "CATA";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cat;
	}
	
	public Boolean changeToCata() {
		Boolean changed=false;
		String currentCatalog = this.getCurrentCatalog();
		if (currentCatalog.equals("CATA")) {
			changed=true;
		} else {
			this.changeCatalog();
			if (this.getCurrentCatalog().equals("CATA")) {
				changed=true;
			}
		}
		return changed;
	}
	
	public Boolean changeToCatb() {
		Boolean changed=false;
		String currentCatalog = this.getCurrentCatalog();
		if (currentCatalog.equals("CATB")) {
			changed=true;
		} else {
			this.changeCatalog();
			if (this.getCurrentCatalog().equals("CATB")) {
				changed=true;
			}
		}
		return changed;
	}
	
	public String changeCatalog() {
		String wholeResponse="";
		
		try {
			Http http = new Http(); 
			String url="";
			if (environment.equals("preview")) {
				http.setUsername(staging_user);
				http.setPassword(staging_pass);
				url=staging_url;
			}
			if (environment.equals("prod")) {
				http.setUsername(prod_user);
				http.setPassword(prod_pass);
				url=prod_url;
			}
			String prepare= http.sendGET(url+"/dyn/admin/nucleus/atg/dynamo/service/jdbc/SwitchingDataSource/?invokeMethod=prepareSwitch");
			String perform = http.sendGET(url+"/dyn/admin/nucleus/atg/dynamo/service/jdbc/SwitchingDataSource/?invokeMethod=performSwitch");
			wholeResponse = prepare+"\n\n"+perform;
			System.out.println(prepare);
			System.out.println(perform);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wholeResponse;
	}

	
	public String xmlDeletePrice(String id) {
		return "<remove-item id=\""+id+"\" item-descriptor=\"price\" />";
		//return "%3Cremove-item+id%3D%22"+id+"%22+item-descriptor%3D%22price%22+%2F%3E%0D%0A";
	}
	
	public String xmlDeleteCampaign(String id) {
		return "<remove-item id=\""+id+"\" item-descriptor=\"campaign\" />";
	}
	
	public String xmlPrintItemPrice(String id) {
		return "<print-item id=\""+id+"\" item-descriptor=\"price\" />";
	}
	
	public String xmlAddCampaign(String storeId, String skuId, String labels) {
		return "<add-item item-descriptor=\"campaign\" id=\""+skuId+";"+storeId+"\">\n"
				+ "  <set-property name=\"campaigns\"><![CDATA["+labels+"]]></set-property>\n"
				+ "  <set-property name=\"storeId\"><![CDATA["+storeId+"]]></set-property>\n"
				+ "  <set-property name=\"skuId\"><![CDATA["+skuId+"]]></set-property>\n"
				+ "</add-item>\n";
	}
	
	public String executeXmlPriceList(String xml) {
		String response="";
		Http http = new Http();
		String url="";
		
		if (environment.equals("preview")) {
			http.setUsername(staging_user);
			http.setPassword(staging_pass);
			url=staging_url;
		}
		if (environment.equals("prod")) {
			http.setUsername(prod_user);
			http.setPassword(prod_pass);
			url=prod_url;
		}
		response = "";

		try {
			response = http.sendPOST(url+"/dyn/admin/nucleus/atg/commerce/pricing/priceLists/PriceLists/", xml);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	public void executeXmlCampaign(String xml) {
		try {
			Http http = new Http(); 
			String url="";
			if (environment.equals("preview")) {
				http.setUsername(staging_user);
				http.setPassword(staging_pass);
				url=staging_url;
			}
			if (environment.equals("prod")) {
				http.setUsername(prod_user);
				http.setPassword(prod_pass);
				url=prod_url;
			}
			http.sendPOST(url+"/dyn/admin/nucleus/com/i2btech/repositories/campaignRepository/CampaignRepository/",
					"xmltext=" + xml);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


}
