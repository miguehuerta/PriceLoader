package com.nutech.priceloader.services;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.nutech.atg.entities.DcsPrice;
import com.nutech.atg.entities.WlmCampaignItem;
import com.nutech.atg.entities.WlmCampaignItemList;
import com.nutech.atg.entities.WlmPrice;
import com.nutech.atg.entities.WlmProduct;
import com.nutech.atg.repository.DcsPriceRepository;
import com.nutech.atg.repository.WlmCampaignItemListRepository;
import com.nutech.atg.repository.WlmCampaignItemRepository;
import com.nutech.atg.repository.WlmPriceRepository;
import com.nutech.atg.services.CatalogService;
import com.nutech.atg.services.DcsPriceService;
import com.nutech.atg.services.DynamoService;
import com.nutech.atg.services.WlmPriceService;
import com.nutech.atg.services.WlmProductService;
import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.PriceListProject;
import com.nutech.priceloader.entities.Project;
import com.nutech.priceloader.repository.ExcelFileRepository;
import com.nutech.priceloader.repository.PriceListProjectRepository;
import com.nutech.priceloader.utils.Helpers;
import com.nutech.priceloader.utils.Ssh;

@Service
public class PriceListProjectService {

	@Value("${priceImport.locationBccFiles}")
	private String locationBccFiles;

	@Value("${environment}")
	private String environment;

	@Autowired
	PriceListProjectRepository priceListProjectRepository;

	@Autowired
	ExcelFileRepository excelRepo;

	@Autowired
	ExcelFilelValidationHelper exHelper;

	@Autowired
	WlmProductService wlmProductService;

	@Autowired
	DcsPriceService dcsPriceService;

	@Autowired
	WlmPriceService wlmPriceService;

	@Autowired
	Helpers helpers;

	@Autowired
	CatalogService catalogService;

	@Autowired
	DcsPriceRepository dcsPriceRepository;

	@Autowired
	WlmPriceRepository wlmPriceRepository;

	@Autowired
	WlmCampaignItemRepository wlmCampaignItemRepository;
	
	@Autowired
	WlmCampaignItemListRepository wlmCampaignItemListRepository;

	@Autowired
	ProjectService projectService;

	@Autowired
	DynamoService dynService;

	@Autowired
	Ssh ssh;

	public List<PriceListProject> setPriceListFromProject(Project myProject, String name) {
		// TODO Auto-generated method stub
		System.out.println("Thread used for this is " + Thread.currentThread().getName());
		List<ExcelFile> excelFiles = excelRepo.findExcelFilesByProjectId(myProject);

		Set<PriceListProject> set = new HashSet<PriceListProject>();
		int quantityTotalRows = 0;
		for (ExcelFile file : excelFiles) {
			ArrayList<PriceListProject> priceListProjects = exHelper.getPriceListFile(file);
			quantityTotalRows += priceListProjects.size();
			set.addAll(priceListProjects);
			System.out.println("Guardando registros");

		}
		System.out.println("Cantidad de registros totales " + quantityTotalRows);
		System.out.println("Cantidad de registros no duplidados " + set.size());
		System.out.println("Cantidad de registros repetidos" + (quantityTotalRows - set.size()));
		return priceListProjectRepository.saveAllAndFlush(set);
	}

	public void deletePriceListsProjectByProject(Project myProject) {
		// TODO Auto-generated method stub
		List<PriceListProject> myPriceListsProject = priceListProjectRepository.findByProject(myProject);
		priceListProjectRepository.deleteAll(myPriceListsProject);
	}

	@SuppressWarnings("unused")
	public void doValidations(List<PriceListProject> myPriceListsProject, Project project, boolean isRollback,
			Authentication auth) throws CloneNotSupportedException, IOException {

		System.out.println("Comenzando a recopilar informacion de bd");

		projectService.setState(project, "Recopilanndo datos");
		projectService.setProgress(project, 5);

		if (environment.equals("preview")) {
			catalogService.changeCatPreview();
		}
		if (environment.equals("prod")) {
			dynService.changeToCata();
		}

		Set<String> setSkusPriceList = myPriceListsProject.stream().map(PriceListProject::getProductNbr)
				.collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());

		Set<String> setProductIds = myPriceListsProject.stream().map(item -> "PROD_" + item.getProductNbr())
				.collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());

		catalogService.changeCatB();
		List<WlmProduct> products = this.getProducts(setProductIds);

		List<DcsPrice> prices = this.getPrices(setSkusPriceList);

		Set<String> setPricesIds = prices.stream().map(item -> item.getPriceId()).collect(Collectors.toSet()).stream()
				.map(String::valueOf).collect(Collectors.toSet());

		List<WlmPrice> wlmPrices = this.getWlmPrices(setPricesIds);
		
		List<WlmCampaignItem> wlmCampaignItems = this.getWlmCampaignItem(setSkusPriceList);

		List<WlmCampaignItemList> wlmCampaignItemLists = this.getWlmCampaignItemLists(setSkusPriceList);

		Map<String, WlmProduct> productsMap = new HashMap<>();
		for (WlmProduct product : products) {
			productsMap.put(product.getProductId(), product);
		}

		Map<String, DcsPrice> pricesMap = new HashMap<>();
		for (DcsPrice price : prices) {
			pricesMap.put(price.getSkuId() + ";" + price.getPriceList(), price);
		}

		Map<String, WlmPrice> wlmPriceMap = new HashMap<>();
		for (WlmPrice wlmPrice : wlmPrices) {
			wlmPriceMap.put(wlmPrice.getPriceId(), wlmPrice);
		}
		
		Map<String, WlmCampaignItem> wlmCampaignItemMap = new HashMap<>();
		for (WlmCampaignItem wlmCampaignItem : wlmCampaignItems) {
			wlmCampaignItemMap.put(wlmCampaignItem.getSkuId() + ";" + wlmCampaignItem.getStoreId(), wlmCampaignItem);
		}

		Map<String, WlmCampaignItemList> wlmCampaignItemListMap = new HashMap<>();
		for (WlmCampaignItemList wlmCampaignItemList : wlmCampaignItemLists) {
			wlmCampaignItemListMap.put(wlmCampaignItemList.getSkuId() + ";" + wlmCampaignItemList.getStoreId() + ";"
					+ wlmCampaignItemList.getSequenceNum(), wlmCampaignItemList);
		}

		List<PriceListProject> rollbackPriceListProjectList = new ArrayList<>();

		List<PriceListProject> bccItems = new ArrayList<>();

		// prices.stream().collect(Collectors.toMap(p ->
		// p.getSkuId()+";"+p.getPriceList(), p-> p));
		projectService.setState(project, "Actualizando registros");
		projectService.setProgress(project, 14);

		int i = 0;
		int total = myPriceListsProject.size();
		int percentageUpdates = 0;
		int updatePack = 0;
		int updateBase = 0;
		int updateSales = 0;
		int totalPack = 0;
		int totalBase = 0;
		int totalSales = 0;
		int bcc = 0;

		List<DcsPrice> partialDcsPriceUpdates = new ArrayList<>();
		List<WlmPrice> partialWlmPriceUpdates = new ArrayList<>();

		List<DcsPrice> partialDcsPriceDeletes = new ArrayList<>();
		List<WlmPrice> partialWlmPriceDeletes = new ArrayList<>();
		
		List<String> partialDynamoCampaign = new ArrayList<String>();

		// Inicializando cliente sftp
		ssh.initializeClient();

		int sequence = 0;
		for (PriceListProject item : myPriceListsProject) {
			Integer storeId = item.getStoreNbr();
			Integer skuId = item.getProductNbr();
			String ProductId = "PROD_" + skuId.toString();
			Integer basePriceReference = item.getBasePriceReference();
			Integer basePriceSales = item.getBasePriceSales();
			String icon1 = item.getIcon1();
			String icon2 = item.getIcon2();
			Integer basePackSize = item.getBasePackSize();
			Integer basePackPrice = item.getBasePackPrice();

			String keyPackPrice = storeId.toString() + ";PackPrice";
			String keyBasePriceReference = storeId.toString() + ";ListPrice";
			String keyBasePriceSales = storeId.toString() + ";SalePrice";

			DcsPrice currentDcsPricePack = pricesMap.get(skuId.toString() + ";" + keyPackPrice);

			DcsPrice currentDcsPriceBasePriceReference = pricesMap.get(skuId.toString() + ";" + keyBasePriceReference);
			DcsPrice currentDcsPriceBasePriceSales = pricesMap.get(skuId.toString() + ";" + keyBasePriceSales);

			WlmPrice currentWlmPricePack = currentDcsPricePack != null
					? wlmPriceMap.get(currentDcsPricePack.getPriceId())
					: null;
			WlmPrice currentWlmBasePriceReferece = currentDcsPriceBasePriceReference != null
					? wlmPriceMap.get(currentDcsPriceBasePriceReference.getPriceId())
					: null;
			WlmPrice currentWlmBasePriceSales = currentDcsPriceBasePriceSales != null
					? wlmPriceMap.get(currentDcsPriceBasePriceSales.getPriceId())
					: null;

			WlmCampaignItem wlmCampaignItem = wlmCampaignItemMap.get(skuId+";"+storeId);
			
			WlmCampaignItemList wlmCampaignList0 = wlmCampaignItemListMap.get(skuId + ";" + storeId + ";0");
			WlmCampaignItemList wlmCampaignList1 = wlmCampaignItemListMap.get(skuId + ";" + storeId + ";1");

			boolean dcsPackPriceUpdated = false;
			boolean dcsBasePriceReferenceUpdated = false;
			boolean dcsPriceSalesUpdated = false;

			boolean wlmPackPriceUpdated = false;
			boolean wlmBasePriceReferenceUpdated = false;
			boolean wlmPriceSalesUpdated = false;
			
			boolean dcsPackPriceDeleted = false;
			boolean dcsBasePriceReferenceDeleted = false;
			boolean dcsPriceSalesDeleted = false;

			boolean wlmPackPriceDeleted = false;
			boolean wlmBasePriceReferenceDeleted = false;
			boolean wlmPriceSalesDeleted = false;
						

			String actions = "";

			PriceListProject rollbackPriceListProject = (PriceListProject) item.clone();

			if (!isRollback) {
				rollbackPriceListProject = getRollbackData(rollbackPriceListProject, currentDcsPricePack,
						currentDcsPriceBasePriceReference, currentDcsPriceBasePriceSales, currentWlmPricePack,
						currentWlmBasePriceReferece, currentWlmBasePriceSales, wlmCampaignList0, wlmCampaignList1);
				rollbackPriceListProjectList.add(rollbackPriceListProject);
			}

			if (productsMap.get(ProductId) != null) {
				boolean insert = false;

				WlmProduct product = productsMap.get(ProductId);
				String uom = product.getContentUom();
				String pricePerUm = this.calculateUom(uom, basePriceSales);

				item.setPricePerUm(pricePerUm);

				if (basePackPrice != null && basePackPrice != 0) {
					totalPack++;
					if (currentDcsPricePack != null) {
						updatePack += 1;
						if (!currentDcsPricePack.getListPrice().equals(basePackPrice)) {
							actions += actions.isEmpty() ? "" : " | ";
							actions += "update packPrice: " + currentDcsPricePack.getListPrice() + " a "
									+ basePackPrice;
							currentDcsPricePack.setListPrice(basePackPrice);
							dcsPackPriceUpdated = true;
						}

						if (currentWlmPricePack != null) {
							boolean changePackSize = false;
							boolean changeUm = false;
							if (!currentWlmPricePack.getPackSize().equals(basePackSize)) {
								actions += actions.isEmpty() ? "" : " | ";
								actions += "update packSize: " + currentWlmPricePack.getPackSize() + " a "
										+ basePackSize;

								currentWlmPricePack.setPackSize(basePackSize);
								changePackSize = true;
							}

							if (!currentWlmPricePack.getPricePerUm().isEmpty()
									&& !currentWlmPricePack.getPricePerUm().equals("0")
									&& !currentWlmPricePack.getPricePerUm().equals(pricePerUm)) {
								actions += actions.isEmpty() ? "" : " | ";
								actions += "update PricePerUm: " + currentWlmPricePack.getPricePerUm() + " a "
										+ pricePerUm;
								currentWlmPricePack.setPricePerUm(pricePerUm);
								changeUm = true;
							}
							if (changePackSize == true || changeUm == true) {
								wlmPackPriceUpdated = true;
							}
						}
					} else {
						// Es inserción
						insert = true;
					}
				} else {
					if (currentDcsPricePack != null) {
						// borrar precio dyn admin
						actions += actions.isEmpty() ? "" : " | ";
						actions += "bajando precio pack en dcs";
						dcsPackPriceDeleted=true;
					}
					if (currentWlmPricePack != null) {
						// borrar precio dyn admin
						actions += actions.isEmpty() ? "" : " | ";
						actions += "bajando precio pack en wlm";
						wlmPackPriceDeleted=true;
					}
				}

				if (basePriceReference != null && basePriceReference != 0) {
					totalBase++;
					if (currentDcsPriceBasePriceReference != null) {
						updateBase += 1;
						if (!currentDcsPriceBasePriceReference.getListPrice().equals(basePriceReference)) {
							actions += actions.isEmpty() ? "" : " | ";
							actions += "update basePriceReference: " + currentDcsPriceBasePriceReference.getListPrice()
									+ " a " + basePriceReference;
							currentDcsPriceBasePriceReference.setListPrice(basePriceReference);
							dcsBasePriceReferenceUpdated = true;
						}

						if (currentWlmBasePriceReferece != null) {
							if (!currentWlmBasePriceReferece.getPricePerUm().isEmpty()
									&& !currentWlmBasePriceReferece.getPricePerUm().equals("0")
									&& !currentWlmBasePriceReferece.getPricePerUm().equals(pricePerUm)) {

								actions += actions.isEmpty() ? "" : " | ";
								actions += "update PricePerUm: " + currentWlmBasePriceReferece.getPricePerUm() + " a "
										+ pricePerUm;
								currentWlmBasePriceReferece.setPricePerUm(pricePerUm);
								wlmBasePriceReferenceUpdated = true;
							}
						}
					} else {
						// Es inserción
						insert = true;
					}
				} else {

					if (currentDcsPriceBasePriceReference != null) {
						actions += actions.isEmpty() ? "" : " | ";
						actions += "bajando precio priceReference e dcs";
						dcsBasePriceReferenceDeleted=true;
					}
					if (currentWlmBasePriceReferece != null) {
						actions += actions.isEmpty() ? "" : " | ";
						actions += "bajando precio priceReference e wlm";
						wlmBasePriceReferenceDeleted=true;
					}
				}

				if (basePriceSales != null && basePriceSales != 0) {
					totalSales++;
					if (currentDcsPriceBasePriceSales != null) {
						updateSales += 1;
						if (!currentDcsPriceBasePriceSales.getListPrice().equals(basePriceSales)) {
							actions += actions.isEmpty() ? "" : " | ";
							actions += "update basePriceSales: " + currentDcsPriceBasePriceSales.getListPrice() + " a "
									+ basePriceSales;
							// Actualizar priceSales e bd
							currentDcsPriceBasePriceSales.setListPrice(basePriceSales);
							dcsPriceSalesUpdated = true;

						}
						if (currentWlmBasePriceSales != null) {
							if (!currentWlmBasePriceSales.getPricePerUm().isEmpty()
									&& !currentWlmBasePriceSales.getPricePerUm().equals("0")
									&& !currentWlmBasePriceSales.getPricePerUm().equals(pricePerUm)) {
								actions += actions.isEmpty() ? "" : " | ";
								actions += "update PricePerUm: " + currentWlmBasePriceSales.getPricePerUm() + " a "
										+ pricePerUm;
								currentWlmBasePriceSales.setPricePerUm(pricePerUm);
								wlmPriceSalesUpdated = true;
							}
						}
					} else {
						// Es inserción
						insert = true;
					}
				} else {
					if (currentDcsPriceBasePriceSales != null) {
						actions += actions.isEmpty() ? "" : " | ";
						actions += "bajando precio priceSales en dcs";
						dcsPriceSalesDeleted=true;
						//partialDcsPriceDeletes.add(currentDcsPriceBasePriceSales);
					}
					if (currentWlmBasePriceSales != null) {
						actions += actions.isEmpty() ? "" : " | ";
						actions += "bajando precio priceSales en wlm";
						wlmPriceSalesDeleted=true;
						//partialWlmPriceDeletes.add(currentWlmBasePriceSales);
					}
				}

				if (insert == true) {
					// nuevo registro para poder agregarlo a csv de priceList
					bcc++;
					bccItems.add(item);
				} else {
					// Se evalua si se actualizó y se agrega
					if (dcsPackPriceUpdated == true) {
						partialDcsPriceUpdates.add(currentDcsPricePack);
					}
					if (dcsBasePriceReferenceUpdated == true) {
						partialDcsPriceUpdates.add(currentDcsPriceBasePriceReference);
					}
					if (dcsPriceSalesUpdated == true) {
						partialDcsPriceUpdates.add(currentDcsPriceBasePriceSales);
					}

					if (wlmPackPriceUpdated == true) {
						partialWlmPriceUpdates.add(currentWlmPricePack);
					}
					if (wlmBasePriceReferenceUpdated == true) {
						partialWlmPriceUpdates.add(currentWlmBasePriceReferece);
					}
					if (wlmPriceSalesUpdated == true) {
						partialWlmPriceUpdates.add(currentWlmBasePriceSales);
					}
					
					if(dcsPackPriceDeleted==true) {
						partialDcsPriceDeletes.add(currentDcsPricePack);
					}
					if(wlmPackPriceDeleted==true) {
						partialWlmPriceDeletes.add(currentWlmPricePack);
					}

					if(dcsBasePriceReferenceDeleted==true) {
						partialDcsPriceDeletes.add(currentDcsPriceBasePriceReference);
					}
					if(wlmBasePriceReferenceDeleted==true) {
						partialWlmPriceDeletes.add(currentWlmBasePriceReferece);
					}

					if(dcsPriceSalesDeleted==true) {
						partialDcsPriceDeletes.add(currentDcsPriceBasePriceSales);
					}
					if(wlmPriceSalesDeleted==true) {
						partialWlmPriceDeletes.add(currentWlmBasePriceSales);
					}
					
					String icons = getIcons(icon1, icon2, wlmCampaignList0, wlmCampaignList1);

					if (!icons.isEmpty()) {
						// additem campaign

						actions += actions.isEmpty() ? "" : " | ";
						actions += "update en campaigns";
						String xmlDyn = dynService.xmlAddCampaign(storeId.toString(), skuId.toString(), icons);
						partialDynamoCampaign.add(xmlDyn);
					}

					if (icons.isEmpty() && wlmCampaignList0 != null && wlmCampaignList1 != null) {
						actions += actions.isEmpty() ? "" : " | ";
						actions += "bajado campaigns";
						String xmlDyn = dynService.xmlDeleteCampaign(skuId.toString() + ";" + storeId.toString());
						partialDynamoCampaign.add(xmlDyn);
					}
				}

				if (bccItems.size() > 5999) {
					this.generateBccFiles(bccItems, sequence, auth);
					sequence++;
					bccItems.clear();
				}



				item.setActionPeformed(actions);

			} else {
				// System.out.println("No existe el sku en atg");
				item.setError(true);
			}
			percentageUpdates = (int) i * 100 / total;
			projectService.setProgress(project, percentageUpdates);

			i++;

		}

		/*
		 * //Descomentar para aplicar changes base datos try {
		 * this.saveAllUpdates(partialWlmPriceUpdates, partialDcsPriceUpdates); } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (ExecutionException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 * 
		 * this.applyDynamoChangesCampaigns(partialDynamoCampaign);
		*/
		
		//Esto es para borrar los precios
		//this.deletePrices(partialWlmPriceDeletes, partialDcsPriceDeletes);
		
		project.setProcessedPercentage(100);

		String report = "Se encontraron " + updatePack + " de " + totalPack + " requeridos\n" + "Se encontraron "
				+ updateBase + " de " + totalBase + " requeridos\n" + "Se encontraron " + updateSales + " de "
				+ totalSales + " requeridos\n" + "Cantidad de registros hacia bcc :" + bcc;

		System.out.println(report);

		for (PriceListProject rollbackItem : rollbackPriceListProjectList) {
			rollbackItem.setId(null);
		}
		// this.generateBccFiles(bccItems, auth);
		if (bccItems.size() > 0) {
			this.generateBccFiles(bccItems, sequence, auth);
			bccItems.clear();
		}

		if (isRollback == false) {
			priceListProjectRepository.saveAll(myPriceListsProject);
			priceListProjectRepository.saveAll(rollbackPriceListProjectList);
		}
	}

	private void applyDynamoChangesPriceList(List<DcsPrice> partialDynamoPrice) {
		// TODO Auto-generated method stub

		List<DcsPrice> existingPreview = new ArrayList<>();
		List<DcsPrice> existingCata = new ArrayList<>();
		List<DcsPrice> existingCatb = new ArrayList<>();

		if (environment.equals("preview")) {
			existingPreview = dcsPriceService.findByPriceIdPreview(partialDynamoPrice);

			List<String> updatesPreview = new ArrayList<String>();

			for (DcsPrice price : existingPreview) {
				updatesPreview.add(dynService.xmlDeletePrice(price.getPriceId()));
			}
			applyDynUpdatesPrices(updatesPreview, "preview");
		}

		if (environment.equals("prod")) {

			existingCata = dcsPriceService.findByPriceIdCata(partialDynamoPrice);
			existingCatb = dcsPriceService.findByPriceIdCatb(partialDynamoPrice);

			List<String> updatesCata = new ArrayList<String>();
			List<String> updatesCatb = new ArrayList<String>();

			for (DcsPrice price : existingCata) {
				updatesCata.add(dynService.xmlDeletePrice(price.getPriceId()));
			}

			for (DcsPrice price : existingCatb) {
				updatesCatb.add(dynService.xmlDeletePrice(price.getPriceId()));
			}
			applyDynUpdatesPrices(updatesCata, "cata");
			applyDynUpdatesPrices(updatesCatb, "catb");
		}

	}

	private void applyDynUpdatesPrices(List<String> updates, String catalog) {
		// TODO Auto-generated method stub
		String partialString = "";
		int counter = 0;

		if (catalog.equals("cata")) {
			dynService.changeToCata();
		}
		if (catalog.equals("catb")) {
			dynService.changeToCatb();
		}

		for (String item : updates) {
			partialString += item;
			if (counter > 100) {
				System.out.println("actualizando delta en prod dyn price " + catalog);
				dynService.executeXmlPriceList(partialString);
				partialString = "";
				counter = 0;
			}
			counter++;
		}
		if (counter > 0) {
			dynService.executeXmlPriceList(partialString);
			partialString = "";
			counter = 0;
		}

	}

	private void applyDynamoChangesCampaigns(List<String> partialDynamoCampaign) {
		String partialString = "";
		int counter = 0;
		for (String item : partialDynamoCampaign) {
			partialString += item;
			if (counter > 950) {
				if (environment.equals("preview")) {
					System.out.println("actualizando delta en preview dyn campaigns");
					// Descomentar para actualizar en dyn admin
					dynService.executeXmlCampaign(partialString);
				}
				if (environment.equals("prod")) {
					System.out.println("actualizando delta en prod dyn campaigns");
					dynService.changeToCatb();
					// Descomentar para actualizar en dyn admin
					dynService.executeXmlCampaign(partialString);
					dynService.changeToCata();
					// Descomentar para actualizar en dyn admin
					dynService.executeXmlCampaign(partialString);
				}
				partialString = "";
				counter = 0;
			}
			counter++;
		}
		if (counter > 0) {
			if (environment.equals("preview")) {
				System.out.println("actualizando delta en preview dyn campaigns");
				// Descomentar para actualizar en dyn admin
				dynService.executeXmlCampaign(partialString);
			}
			if (environment.equals("prod")) {
				System.out.println("actualizando delta en prod dyn campaigns");
				dynService.changeToCatb();
				// Descomentar para actualizar en dyn admin
				dynService.executeXmlCampaign(partialString);
				dynService.changeToCata();
				// Descomentar para actualizar en dyn admin
				dynService.executeXmlCampaign(partialString);
			}
		}
	}

	static <T> List<List<T>> chopped(List<T> list, final int L) {
		List<List<T>> parts = new ArrayList<List<T>>();
		final int N = list.size();
		for (int i = 0; i < N; i += L) {
			parts.add(new ArrayList<T>(list.subList(i, Math.min(N, i + L))));
		}
		return parts;
	}

	private void deletePrices(List<WlmPrice> wlmPriceDeletes, List<DcsPrice> dcsPriceDeletes){
		int sizeDcs = dcsPriceDeletes.size() / 8 + 1;
		int sizeWlm = wlmPriceDeletes.size() / 8 + 1;
		List<List<WlmPrice>> wlmPriceSplited = chopped(wlmPriceDeletes, sizeWlm);
		List<List<DcsPrice>> dcsPriceSplited = chopped(dcsPriceDeletes, sizeDcs);

		if (sizeDcs > 100) {
			dcsPriceService.deletePrices(dcsPriceSplited.get(0), environment);
			dcsPriceService.deletePrices(dcsPriceSplited.get(1), environment);
			dcsPriceService.deletePrices(dcsPriceSplited.get(2), environment);
			dcsPriceService.deletePrices(dcsPriceSplited.get(3), environment);
			dcsPriceService.deletePrices(dcsPriceSplited.get(4), environment);
			dcsPriceService.deletePrices(dcsPriceSplited.get(5), environment);
			dcsPriceService.deletePrices(dcsPriceSplited.get(6), environment);
			dcsPriceService.deletePrices(dcsPriceSplited.get(7), environment);
		} else {
			dcsPriceService.deletePrices(dcsPriceDeletes, environment);
		}
		if (sizeWlm > 100) {
			wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(0), environment);
			wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(1), environment);
			wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(2), environment);
			wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(3), environment);
			wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(4), environment);
			wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(5), environment);
			wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(6), environment);
			wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(7), environment);
		} else {
			wlmPriceService.deleteWlmPrices(wlmPriceDeletes, environment);
		}

	}

	private void saveAllPriceUpdates(List<WlmPrice> partialWlmPriceUpdates, List<DcsPrice> dcsPriceUpdates)
			throws InterruptedException, ExecutionException {
		int sizeDcs = dcsPriceUpdates.size() / 8 + 1;
		int sizeWlm = partialWlmPriceUpdates.size() / 8 + 1;
		List<List<WlmPrice>> wlmPriceSplited = chopped(partialWlmPriceUpdates, sizeWlm);
		List<List<DcsPrice>> dcsPriceSplited = chopped(dcsPriceUpdates, sizeDcs);

		if (sizeDcs > 100) {
			CompletableFuture.allOf(dcsPriceService.savePrices(dcsPriceSplited.get(0), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(1), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(2), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(3), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(4), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(5), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(6), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(7), environment)).get();
		} else {
			dcsPriceService.savePrices(dcsPriceUpdates, environment).get();
		}
		if (sizeWlm > 100) {
			CompletableFuture.allOf(wlmPriceService.saveWlmPrices(wlmPriceSplited.get(0), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(1), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(2), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(3), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(4), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(5), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(6), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(7), environment)).get();
		} else {
			wlmPriceService.saveWlmPrices(partialWlmPriceUpdates, environment);
		}

	}

	private String getIcons(String icon1, String icon2, WlmCampaignItemList wlmCampaignList0,
			WlmCampaignItemList wlmCampaignList1) {
		// TODO Auto-generated method stub
		String icons = "";
		if (icon1 != null) {
			if (wlmCampaignList0 != null) {
				if (!icon1.equals(wlmCampaignList0.getCampaigns())) {
					icons += wlmCampaignList0.getCampaigns();
				}
			}
		}
		if (icon2 != null) {
			if (wlmCampaignList1 != null) {
				if (!icon1.equals(wlmCampaignList1.getCampaigns())) {
					icons += icons.isEmpty() ? wlmCampaignList1.getCampaigns() : "," + icon2;
				}
			}
		}
		return icons;
	}

	private PriceListProject getRollbackData(PriceListProject rollbackPriceListProject, DcsPrice currentDcsPricePack,
			DcsPrice currentPriceBasePriceReference, DcsPrice curretPriceBasePriceSales, WlmPrice currentWlmPricePack,
			WlmPrice wlmBasePriceReferece, WlmPrice wlmBasePriceSales, WlmCampaignItemList wlmCampaignList0,
			WlmCampaignItemList wlmCampaignList1) {

		rollbackPriceListProject.setId(null);
		rollbackPriceListProject.setRollback(true);
		if (currentDcsPricePack != null) {
			rollbackPriceListProject.setBasePackPrice(currentDcsPricePack.getListPrice());
		}

		if (currentWlmPricePack != null) {
			rollbackPriceListProject.setBasePackSize(currentWlmPricePack.getPackSize());
		}

		if (currentPriceBasePriceReference != null) {
			rollbackPriceListProject.setBasePriceReference(currentPriceBasePriceReference.getListPrice());
		}

		if (curretPriceBasePriceSales != null) {
			rollbackPriceListProject.setBasePriceSales(curretPriceBasePriceSales.getListPrice());
		}

		if (wlmCampaignList0 != null) {
			rollbackPriceListProject.setIcon1(wlmCampaignList0.getCampaigns());
		}

		if (wlmCampaignList1 != null) {
			rollbackPriceListProject.setIcon2(wlmCampaignList1.getCampaigns());
		}

		return rollbackPriceListProject;
	}

	private void generateBccFiles(List<PriceListProject> bccItems, int sequence, Authentication auth)
			throws IOException {
		List<PriceListProject> deltaPriceListProject = new ArrayList<>();
		for (PriceListProject priceListProject : bccItems) {
			if (priceListProject.getBasePackPrice() == null) {
				priceListProject.setBasePackPrice(0);
			}
			if (priceListProject.getBasePackSize() == null) {
				priceListProject.setBasePackSize(0);
			}
			if (priceListProject.getTlmPrice() == null) {
				priceListProject.setTlmPrice(0);
			}
			if (priceListProject.getCost() == null) {
				priceListProject.setCost(0);
			}

			deltaPriceListProject.add(priceListProject);
			/*
			 * if (deltaPriceListProject.size() == 6000) { writeFile(deltaPriceListProject,
			 * sequence, auth); deltaPriceListProject.clear(); sequence++; }
			 */
		}
		if (deltaPriceListProject.size() > 0) {
			String filename = writeFile(deltaPriceListProject, sequence, auth);
			ssh.sendFile(filename, "/opt/shared/priceImport");
			deltaPriceListProject.clear();
		}
	}

	private void checkFolder(String path) {
		// TODO Auto-generated method stub
		String folderPath = path;
		if (!helpers.isFolderCreated(folderPath)) {
			System.out.println("Folder no estaba creado");
			helpers.createDirectory(folderPath);

		} else {
			System.out.println("Folder ya estaba creado");
		}
	}

	private String writeFile(List<PriceListProject> bccItems, int sequence, Authentication auth) {
		String fileName = "";
		try {
			String pathFolder = locationBccFiles + auth.getName();
			this.checkFolder(pathFolder);
			long timestamp = System.currentTimeMillis() / 1000;
			fileName = pathFolder + "/PriceBase_" + timestamp + "_" + sequence + ".csv";
			ICsvBeanWriter csvWriter = new CsvBeanWriter(new FileWriter(fileName),
					CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			String[] csvHeader = { "Product Number", "Physical Store ID", "List Price", "Sale Price", "TLMC Price",
					"Pack Price", "Pack Size", "Cost", "Campaign 1", "Campaign 2", "PPUM", "Validation From",
					"Validation To", "Stock", "BundleCampaign" };

			String[] nameMapping = { "ProductNbr", "StoreNbr", "BasePriceReference", "BasePriceSales", "tlmPrice",
					"BasePackPrice", "BasePackSize", "cost", "Icon1", "Icon2", "pricePerUm", "validationFrom",
					"validationTo", "stock", "bundleCampaign" };

			csvWriter.writeHeader(csvHeader);
			for (PriceListProject row : bccItems) {
				csvWriter.write(row, nameMapping);
			}

			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}
	
	private List<WlmCampaignItem> getWlmCampaignItem (Set<String> setSkus){
		Set<String> subset = new HashSet<>();
		List<WlmCampaignItem> wlmCampaignItems = new ArrayList<>();
		int counter = 0;

		for (String skuId : setSkus) {
			subset.add(skuId);
			if (counter == 999) {
				List<WlmCampaignItem> wlmCampaignItemsT = wlmCampaignItemRepository.findBySkuIdIn(subset);
				if (wlmCampaignItemsT.size() > 0)
					wlmCampaignItems.addAll(wlmCampaignItemsT);
				counter = 0;
				subset.clear();
			}
			counter++;
		}
		if (counter > 0) {
			List<WlmCampaignItem> wlmCampaignItemsT = wlmCampaignItemRepository.findBySkuIdIn(subset);
			if (wlmCampaignItemsT.size() > 0)
				wlmCampaignItems.addAll(wlmCampaignItemsT);
		}
		return wlmCampaignItems;
	}

	private List<WlmCampaignItemList> getWlmCampaignItemLists(Set<String> setSkus) {
		// TODO Auto-generated method stub
		Set<String> subset = new HashSet<>();
		List<WlmCampaignItemList> wlmCampaignItems = new ArrayList<>();
		int counter = 0;

		for (String skuId : setSkus) {
			subset.add(skuId);
			if (counter == 999) {
				List<WlmCampaignItemList> wlmCampaignItemsT = wlmCampaignItemListRepository.findBySkuIdIn(subset);
				if (wlmCampaignItemsT.size() > 0)
					wlmCampaignItems.addAll(wlmCampaignItemsT);
				counter = 0;
				subset.clear();
			}
			counter++;
		}
		if (counter > 0) {
			List<WlmCampaignItemList> wlmCampaignItemsT = wlmCampaignItemListRepository.findBySkuIdIn(subset);
			if (wlmCampaignItemsT.size() > 0)
				wlmCampaignItems.addAll(wlmCampaignItemsT);
		}
		return wlmCampaignItems;
	}

	private List<WlmPrice> getWlmPrices(Set<String> setPricesIds) {
		Set<String> subset = new HashSet<>();
		List<WlmPrice> wlmPrices = new ArrayList<>();
		int counter = 0;
		int total = setPricesIds.size();

		for (String priceId : setPricesIds) {
			subset.add(priceId);
			if (counter == 999) {
				List<WlmPrice> wlmPricesT = wlmPriceService.findByPriceIdIn(subset);
				if (wlmPricesT.size() > 0)
					wlmPrices.addAll(wlmPricesT);
				counter = 0;
				subset.clear();
			}
			counter++;
		}
		if (counter > 0) {
			List<WlmPrice> wlmPricesT = wlmPriceService.findByPriceIdIn(subset);
			if (wlmPricesT.size() > 0)
				wlmPrices.addAll(wlmPricesT);
		}
		return wlmPrices;
	}

	private List<WlmProduct> getProducts(Set<String> setProductIds) {
		// TODO Auto-generated method stub
		Set<String> subset = new HashSet<>();
		List<WlmProduct> products = new ArrayList<>();
		int counter = 0;

		for (String poductId : setProductIds) {
			subset.add(poductId);
			if (counter == 999) {
				products.addAll(wlmProductService.findByProductIdIn(subset));
				counter = 0;
				subset.clear();
			}
			counter++;
		}
		if (counter > 0) {
			products.addAll(wlmProductService.findByProductIdIn(subset));
		}
		return products;
	}

	public List<DcsPrice> getPrices(Set<String> setSkusPriceList) {
		System.out.println("inicio " + helpers.getCurrentDate());
		Set<String> subset = new HashSet<>();
		List<DcsPrice> prices = new ArrayList<>();
		int counter = 0;
		int total = setSkusPriceList.size();

		for (String skuId : setSkusPriceList)
			subset.add(skuId);
		if (counter == 999) {
			List<DcsPrice> dcsPrices = dcsPriceService.findByskuIdIn(subset);
			if (dcsPrices.size() > 0)
				prices.addAll(dcsPrices);
			counter = 0;
			subset.clear();
		}
		counter++;
		if (counter > 0) {
			List<DcsPrice> dcsPrices = dcsPriceService.findByskuIdIn(subset);
			if (dcsPrices.size() > 0)
				prices.addAll(dcsPrices);
		}

		System.out.println("fin " + helpers.getCurrentDate());
		return prices;
	}

	public String calculateUom(String uom, int price) {
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		String calculatedUOM = "";
		String[] contentUom = null;
		float quantity = 0;
		try {
			contentUom = uom.split(" ");
			quantity = Float.parseFloat(contentUom[0].replace(",", "."));
		} catch (Exception e) {
			return "Precio x Un : $" + formatea.format(price);
		}
		switch (contentUom[1].toLowerCase()) {
		case "ml":
			int pricePerLt = (int) (1000 * price / quantity);
			calculatedUOM = "Precio x Lt : $" + formatea.format(pricePerLt);
			break;
		case "cc":
			int pricePerLt1 = (int) (1000 * price / quantity);
			calculatedUOM = "Precio x Lt : $" + formatea.format(pricePerLt1);
			break;
		case "g":
			int pricePerKg = (int) (1000 * price / quantity);
			calculatedUOM = "Precio x Kg : $" + formatea.format(pricePerKg);
			break;
		case "gr":
			int pricePerKg3 = (int) (1000 * price / quantity);
			calculatedUOM = "Precio x Kg : $" + formatea.format(pricePerKg3);
			break;
		case "lt":
			int pricePerLt2 = (int) (1000 * price / (quantity * 1000));
			calculatedUOM = "Precio x Lt : $" + formatea.format(pricePerLt2);
			break;
		case "l":
			int pricePerLt4 = (int) (1000 * price / (quantity * 1000));
			calculatedUOM = "Precio x Lt : $" + formatea.format(pricePerLt4);
			break;
		case "kg":
			int pricePerKg2 = (int) (1000 * price / (quantity * 1000));
			calculatedUOM = "Precio x Kg : $" + formatea.format(pricePerKg2);
			break;
		case "un":
			int dividedPrice = (int) Math.ceil(price / quantity);
			calculatedUOM = "Precio x Un : $" + formatea.format(dividedPrice);
			break;
		default:
			calculatedUOM = "Precio x Un : $" + formatea.format(price);
		}

		return calculatedUOM;
	}

	public List<PriceListProject> findByProject(Project myProject, boolean isRollback) {
		// TODO Auto-generated method stub
		return priceListProjectRepository.findByProjectAndIsRollback(myProject, isRollback);
	}

}
