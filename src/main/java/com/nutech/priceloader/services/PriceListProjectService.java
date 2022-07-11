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
import org.springframework.scheduling.annotation.Async;
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
import com.nutech.atg.services.WlmCampaignItemListService;
import com.nutech.atg.services.WlmCampaignItemService;
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
	WlmCampaignItemService wlmCampaignItemService;

	@Autowired
	WlmCampaignItemListService wlmCampaignItemListService;

	@Autowired
	Ssh ssh;

	private String actions = "";
	private int i = 0;
	private int total = 0;
	private int percentageUpdates = 0;
	private int updatePack = 0;
	private int updateBase = 0;
	private int updateSales = 0;
	private int totalPack = 0;
	private int totalBase = 0;
	private int totalSales = 0;
	private int bcc = 0;

	private List<PriceListProject> rollbackPriceListProjectList = new ArrayList<>();
	private List<PriceListProject> bccItems = new ArrayList<>();
	private List<DcsPrice> partialDcsPriceUpdates = new ArrayList<>();
	private List<DcsPrice> partialDcsPriceDeletes = new ArrayList<>();
	private List<WlmPrice> partialWlmPriceUpdates = new ArrayList<>();
	private List<WlmPrice> partialWlmPriceDeletes = new ArrayList<>();
	private List<WlmCampaignItem> partialWlmCampaignItemInserts = new ArrayList<>();
	private List<WlmCampaignItemList> partialWlmCampaignItemListDeletes = new ArrayList<>();
	private List<WlmCampaignItemList> partialWlmCampaignItemListInserts = new ArrayList<>();

	private void initialize() {
		this.actions = "";
		this.i = 0;
		this.total = 0;
		this.percentageUpdates = 0;
		this.updatePack = 0;
		this.updateBase = 0;
		this.updateSales = 0;
		this.totalPack = 0;
		this.totalBase = 0;
		this.totalSales = 0;
		this.bcc = 0;

		this.rollbackPriceListProjectList.clear();
		this.bccItems.clear();
		this.partialDcsPriceUpdates.clear();
		this.partialDcsPriceDeletes.clear();
		this.partialWlmPriceUpdates.clear();
		this.partialWlmPriceDeletes.clear();
		this.partialWlmCampaignItemInserts.clear();
		this.partialWlmCampaignItemListDeletes.clear();
		this.partialWlmCampaignItemListInserts.clear();
	}

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

	public void doValidations(List<PriceListProject> myPriceListsProject, Project project, boolean isRollback,
			Authentication auth)
			throws CloneNotSupportedException, IOException, InterruptedException, ExecutionException {
		this.initialize();
		this.total = myPriceListsProject.size();
		System.out.println("Comenzando a recopilar informacion de bd");

		projectService.setState(project, "Recopilanndo datos");
		projectService.setProgress(project, 5);

		if (environment.equals("preview")) {
			catalogService.changeCatPreview();
		}
		if (environment.equals("prod")) {
			dynService.changeToCatb();
		}

		Set<String> setSkusPriceList = myPriceListsProject.stream().map(PriceListProject::getProductNbr)
				.collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());

		Set<String> setProductIds = myPriceListsProject.stream().map(item -> "PROD_" + item.getProductNbr())
				.collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());
		List<WlmProduct> products = this.getProducts(setProductIds);
		List<DcsPrice> prices = this.getPrices(setSkusPriceList);
		Set<String> setPricesIds = prices.stream().map(item -> item.getPriceId()).collect(Collectors.toSet()).stream()
				.map(String::valueOf).collect(Collectors.toSet());
		List<WlmPrice> wlmPrices = this.getWlmPrices(setPricesIds);
		List<WlmCampaignItem> wlmCampaignItems = this.getWlmCampaignItem(setSkusPriceList);
		List<WlmCampaignItemList> wlmCampaignItemLists = this.getWlmCampaignItemLists(setSkusPriceList);
		Map<String, WlmProduct> productsMap = new HashMap<>();
		Map<String, DcsPrice> pricesMap = new HashMap<>();
		Map<String, WlmPrice> wlmPriceMap = new HashMap<>();
		Map<String, WlmCampaignItem> wlmCampaignItemMap = new HashMap<>();
		Map<String, WlmCampaignItemList> wlmCampaignItemListMap = new HashMap<>();

		for (WlmProduct product : products) {
			productsMap.put(product.getProductId(), product);
		}
		for (DcsPrice price : prices) {
			pricesMap.put(price.getSkuId() + ";" + price.getPriceList(), price);
		}
		for (WlmPrice wlmPrice : wlmPrices) {
			wlmPriceMap.put(wlmPrice.getPriceId(), wlmPrice);
		}
		for (WlmCampaignItem wlmCampaignItem : wlmCampaignItems) {
			wlmCampaignItemMap.put(wlmCampaignItem.getSkuId() + ";" + wlmCampaignItem.getStoreId(), wlmCampaignItem);
		}
		for (WlmCampaignItemList wlmCampaignItemList : wlmCampaignItemLists) {
			wlmCampaignItemListMap.put(wlmCampaignItemList.getSkuId() + ";" + wlmCampaignItemList.getStoreId() + ";"
					+ wlmCampaignItemList.getSequenceNum(), wlmCampaignItemList);
		}

		projectService.setState(project, "Analizando registros");
		projectService.setProgress(project, 14);

		/////////////////////// PAARTE de analizar data/////////////////77

		int size = myPriceListsProject.size() / 10 + 1;
		List<List<PriceListProject>> splitedPriceListProject = chopped(myPriceListsProject, size);
		if (myPriceListsProject.size() > 100) {
			List<CompletableFuture<Integer>> futures = new ArrayList<>();

			for (int i = 0; i < splitedPriceListProject.size(); i++) {
				int finalI = i;
				futures.add(CompletableFuture.supplyAsync(() -> {
					try {
						this.priceValidations(splitedPriceListProject.get(finalI), pricesMap, wlmPriceMap,
								wlmCampaignItemMap, wlmCampaignItemListMap, auth, isRollback, project, productsMap);
					} catch (CloneNotSupportedException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return 3;
				}));
			}
			CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[3]));
			allOf.join();

		} else {
			this.priceValidations(myPriceListsProject, pricesMap, wlmPriceMap, wlmCampaignItemMap,
					wlmCampaignItemListMap, auth, isRollback, project, productsMap);
		}

		System.out.println("Esperando 15 segundos");
		Thread.sleep(5000);

		///////////////////////// FIN parte analizar data/////////////////

		//////////////////////// ACTUALIZACION/////////////////////////

		// Esto es para actualizar los precios
		this.saveAllPriceUpdates(this.partialWlmPriceUpdates, this.partialDcsPriceUpdates);

		Thread.sleep(5000);

		// Esto es para borrar los precios
		this.deletePrices(this.getUniqWlmPrice(this.partialWlmPriceDeletes),
				this.getUniqDcsPrice(this.partialDcsPriceDeletes));

		Thread.sleep(5000);

		// Borrar CampaignItemnList
		List<WlmCampaignItemList> uniqDeleteList = this.getUniqCampaignsList(this.partialWlmCampaignItemListDeletes);
		System.out.println("size before " + this.partialWlmCampaignItemListDeletes.size() + ", size after "
				+ uniqDeleteList.size());
		this.deleteCampaignItemsList(uniqDeleteList);

		Thread.sleep(5000);

		// Guardar nuevos CampaingItem

		try {
			this.saveCampaignItems(this.partialWlmCampaignItemInserts);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		Thread.sleep(5000);

		// Guardar nuevos CampaignItemList primero sacando únicos y validando no existen
		List<WlmCampaignItemList> uniqimsertsList = this.getUniqCampaignsList(this.partialWlmCampaignItemListInserts);
		try {

			this.saveCampaignItemsList(uniqimsertsList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			wlmCampaignItemListService.saveCampaignListItemsOneByOne(uniqimsertsList, environment);
		}

		/////////////////////// FIN ACTUALIZACION/////////////////////////////////

		project.setProcessedPercentage(100);

		String report = "Se encontraron " + this.updatePack + " de " + this.totalPack + " requeridos\n"
				+ "Se encontraron " + this.updateBase + " de " + this.totalBase + " requeridos\n" + "Se encontraron "
				+ this.updateSales + " de " + this.totalSales + " requeridos\n" + "Cantidad de registros hacia bcc :"
				+ this.bcc;

		System.out.println(report);
		List<PriceListProject> uniqPlp = getUniqPriceListProject(this.bccItems);
		projectService.setState(project,
				"<span>Se encontraron " + this.updatePack + " packPrice de " + this.totalPack + " requeridos</span><br>"
						+ "<span>Se encontraron " + this.updateBase + " basePrice de " + this.totalBase
						+ " requeridos</span><br>" + "<span>Se encontraron " + this.updateSales + "priceSale de "
						+ this.totalSales + " requeridos</span><br>" + "<span>Cantidad de registros hacia bcc :"
						+ uniqPlp.size() + "</span>");
		if (this.bccItems.size() > 0) {
			System.out.println("Cantidad antes de bcc items " + this.bccItems.size());
			System.out.println("Cantidad después de bcc items " + uniqPlp.size());
			this.generateBccFiles(uniqPlp, auth);
		}

		try {
			for (PriceListProject rollbackItem : this.rollbackPriceListProjectList) {
				if (rollbackItem != null && rollbackItem.getId() != null) {
					rollbackItem.setId(null);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error guardando rollback");
		}
		if (isRollback == false) {
			priceListProjectRepository.saveAll(myPriceListsProject);
			priceListProjectRepository.saveAll(this.rollbackPriceListProjectList);
		}
	}

	@Async
	private void priceValidations(List<PriceListProject> myPriceListsProject, Map<String, DcsPrice> pricesMap,
			Map<String, WlmPrice> wlmPriceMap, Map<String, WlmCampaignItem> wlmCampaignItemMap,
			Map<String, WlmCampaignItemList> wlmCampaignItemListMap, Authentication auth, boolean isRollback,
			Project project, Map<String, WlmProduct> productsMap) throws CloneNotSupportedException, IOException {
		System.out.println("Ejecutando una validación");
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

			WlmCampaignItemList currentwlmCampaignItemList1 = wlmCampaignItemListMap.get(skuId + ";" + storeId + ";0");
			WlmCampaignItemList currentwlmCampaignItemList2 = wlmCampaignItemListMap.get(skuId + ";" + storeId + ";1");

			PriceListProject rollbackPriceListProject = (PriceListProject) item.clone();

			if (!isRollback) {
				rollbackPriceListProject = getRollbackData(rollbackPriceListProject, currentDcsPricePack,
						currentDcsPriceBasePriceReference, currentDcsPriceBasePriceSales, currentWlmPricePack,
						currentWlmBasePriceReferece, currentWlmBasePriceSales, currentwlmCampaignItemList1,
						currentwlmCampaignItemList2);
				this.rollbackPriceListProjectList.add(rollbackPriceListProject);
			}

			if (productsMap.get(ProductId) != null) {
				boolean insert = false;

				WlmProduct product = productsMap.get(ProductId);
				String uom = product.getContentUom();
				String pricePerUm = this.calculateUom(uom, basePriceSales);

				item.setPricePerUm(pricePerUm);
				boolean dcsPackPriceUpdated = false, wlmPackSizeUpdated = false, wlmPackPricePerUmUpdated = false,
						dcsPackPriceDeleted = false, wlmPackPriceDeleted = false;
				if (basePackPrice != null && basePackPrice != 0) {
					this.totalPack++;
					if (currentDcsPricePack != null && currentWlmPricePack != null) {
						this.updatePack += 1;
						if (!currentDcsPricePack.getListPrice().equals(basePackPrice)) {
							dcsPackPriceUpdated = true;
						}

						if (currentWlmPricePack != null) {
							if (!currentWlmPricePack.getPackSize().equals(basePackSize)) {
								wlmPackSizeUpdated = true;
							}

							if (!currentWlmPricePack.getPricePerUm().isEmpty()
									&& !currentWlmPricePack.getPricePerUm().equals("0")
									&& !currentWlmPricePack.getPricePerUm().equals(pricePerUm)) {
								wlmPackPricePerUmUpdated = true;
							}
						}
					} else {
						// Es inserción
						insert = true;
					}
				} else {
					if (currentDcsPricePack != null) {
						// borrar precio
						dcsPackPriceDeleted = true;
					}
					if (currentWlmPricePack != null) {
						// borrar precio
						wlmPackPriceDeleted = true;
					}
				}

				boolean dcsBasePriceReferenceUpdated = false, wlmBasePriceReferencePerUmUpdated = false,
						dcsBasePriceReferenceDeleted = false, wlmBasePriceReferenceDeleted = false;
				if (basePriceReference != null && basePriceReference != 0) {
					this.totalBase++;
					if (currentDcsPriceBasePriceReference != null && currentWlmBasePriceReferece != null) {
						this.updateBase += 1;
						if (!currentDcsPriceBasePriceReference.getListPrice().equals(basePriceReference)) {
							dcsBasePriceReferenceUpdated = true;
						}

						if (currentWlmBasePriceReferece != null) {
							if (!currentWlmBasePriceReferece.getPricePerUm().isEmpty()
									&& !currentWlmBasePriceReferece.getPricePerUm().equals("0")
									&& !currentWlmBasePriceReferece.getPricePerUm().equals(pricePerUm)) {
								wlmBasePriceReferencePerUmUpdated = true;
							}
						}
					} else {
						// Es inserción
						insert = true;
					}
				} else {

					if (currentDcsPriceBasePriceReference != null) {
						dcsBasePriceReferenceDeleted = true;
					}
					if (currentWlmBasePriceReferece != null) {
						wlmBasePriceReferenceDeleted = true;
					}
				}

				boolean dcsPriceSalesUpdated = false, wlmPriceSalesPerUmUpdated = false, dcsPriceSalesDeleted = false,
						wlmPriceSalesDeleted = false;

				if (basePriceSales != null && basePriceSales != 0) {
					this.totalSales++;
					if (currentDcsPriceBasePriceSales != null && currentWlmBasePriceSales != null) {
						this.updateSales += 1;
						if (!currentDcsPriceBasePriceSales.getListPrice().equals(basePriceSales)) {
							dcsPriceSalesUpdated = true;
						}
						if (currentWlmBasePriceSales != null) {
							if (!currentWlmBasePriceSales.getPricePerUm().isEmpty()
									&& !currentWlmBasePriceSales.getPricePerUm().equals("0")
									&& !currentWlmBasePriceSales.getPricePerUm().equals(pricePerUm)) {
								wlmPriceSalesPerUmUpdated = true;
							}
						}
					} else {
						// Es inserció
						insert = true;
					}
				} else {
					if (currentDcsPriceBasePriceSales != null) {
						dcsPriceSalesDeleted = true;
					}
					if (currentWlmBasePriceSales != null) {
						wlmPriceSalesDeleted = true;
					}
				}

				boolean wlmCampaignItemList1Inserted = false, wlmCampaignItemList1Updated = false,
						wlmCampaignItemList1Deleted = false, wlmCampaignItemList2Inserted = false,
						wlmCampaignItemList2Updated = false, wlmCampaignItemList2Deleted = false;

				if (icon1 != null) {
					if (currentwlmCampaignItemList1 != null) {
						// update
						if (!icon1.toString().equals(currentwlmCampaignItemList1.getCampaigns().toString())) {
							// Es distinto, actualizar
							wlmCampaignItemList1Updated = true;
						}
					} else {
						// insert currentwlmCampaignItemList1 es nulo
						wlmCampaignItemList1Inserted = true;
					}
				} else {
					if (currentwlmCampaignItemList1 != null) {
						// delete
						wlmCampaignItemList1Deleted = true;
					}
				}
				if (icon2 != null) {
					if (currentwlmCampaignItemList2 != null) {
						// update
						if (!icon2.toString().equals(currentwlmCampaignItemList2.getCampaigns().toString())) {
							// Es distinto, actualizar
							wlmCampaignItemList2Updated = true;
						}
					} else {
						// insert
						wlmCampaignItemList2Inserted = true;
					}
				} else {
					if (currentwlmCampaignItemList2 != null) {
						// delete
						wlmCampaignItemList2Deleted = true;
					}
				}

				if (insert == true) {
					// Se evalua si se actualizó y se agrega
					if (dcsPackPriceUpdated == true && !currentDcsPricePack.getPriceId().isEmpty()) {
						if (currentDcsPricePack != null) {
							this.addAction(
									"update packPrice: " + currentDcsPricePack.getListPrice() + " a " + basePackPrice);
							currentDcsPricePack.setListPrice(basePackPrice);
							this.partialDcsPriceUpdates.add(currentDcsPricePack);
						} else {
							System.out.println("Nulo");
						}

					}
					if (wlmPackSizeUpdated == true) {
						if (currentWlmPricePack != null) {
							this.addAction(
									"update PricePack: " + currentWlmPricePack.getPackSize() + " a " + basePackSize);
							currentWlmPricePack.setPackSize(basePackSize);
							this.partialWlmPriceUpdates.add(currentWlmPricePack);
						} else {
							System.out.println("Nulo");
						}
					}
					if (wlmPackPricePerUmUpdated == true) {
						if (currentWlmPricePack != null) {
							this.addAction(
									"update PricePerUm: " + currentWlmPricePack.getPricePerUm() + " a " + pricePerUm);
							currentWlmPricePack.setPricePerUm(pricePerUm);
							this.partialWlmPriceUpdates.add(currentWlmPricePack);
						} else {
							System.out.println("Nulo");
						}
					}
					if (dcsPackPriceDeleted == true) {
						if (currentDcsPricePack != null) {
							this.addAction("bajando pricePack en dcs");
							this.partialDcsPriceDeletes.add(currentDcsPricePack);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmPackPriceDeleted == true) {
						if (currentWlmPricePack != null) {
							this.addAction("bajando pricePack en wlm");
							this.partialWlmPriceDeletes.add(currentWlmPricePack);
						} else {
							System.out.println("nulo");
						}
					}

					if (dcsBasePriceReferenceUpdated == true) {
						if (currentDcsPriceBasePriceReference != null) {
							this.addAction("update basePriceReference: "
									+ currentDcsPriceBasePriceReference.getListPrice() + " a " + basePriceReference);
							currentDcsPriceBasePriceReference.setListPrice(basePriceReference);
							this.partialDcsPriceUpdates.add(currentDcsPriceBasePriceReference);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmBasePriceReferencePerUmUpdated == true) {
						if (currentWlmBasePriceReferece != null) {
							this.addAction("update PricePerUm: " + currentWlmBasePriceReferece.getPricePerUm() + " a "
									+ pricePerUm);
							currentWlmBasePriceReferece.setPricePerUm(pricePerUm);
							this.partialWlmPriceUpdates.add(currentWlmBasePriceReferece);
						} else {
							System.out.println("nulo");
						}
					}
					if (dcsBasePriceReferenceDeleted == true) {
						if (currentDcsPriceBasePriceReference != null) {
							this.addAction("bajando priceReference en dcs");
							this.partialDcsPriceDeletes.add(currentDcsPriceBasePriceReference);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmBasePriceReferenceDeleted == true) {
						if (currentWlmBasePriceReferece != null) {
							this.addAction("bajando priceReference en wlm");
							this.partialWlmPriceDeletes.add(currentWlmBasePriceReferece);
						} else {
							System.out.println("nulo");
						}
					}

					if (dcsPriceSalesUpdated == true) {
						if (currentDcsPriceBasePriceSales != null) {
							this.addAction("update basePriceSales: " + currentDcsPriceBasePriceSales.getListPrice()
									+ " a " + basePriceSales);
							currentDcsPriceBasePriceSales.setListPrice(basePriceSales);
							this.partialDcsPriceUpdates.add(currentDcsPriceBasePriceSales);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmPriceSalesPerUmUpdated == true) {
						if (currentWlmBasePriceSales != null) {
							this.addAction("update PricePerUm: " + currentWlmBasePriceSales.getPricePerUm() + " a "
									+ pricePerUm);
							currentWlmBasePriceSales.setPricePerUm(pricePerUm);
							this.partialWlmPriceUpdates.add(currentWlmBasePriceSales);
						} else {
							System.out.println("nulo");
						}
					}
					if (dcsPriceSalesDeleted == true) {
						if (currentDcsPriceBasePriceSales != null) {
							this.addAction("bajando precio priceSales en dcs");
							this.partialDcsPriceDeletes.add(currentDcsPriceBasePriceSales);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmPriceSalesDeleted == true) {
						if (currentWlmBasePriceSales != null) {
							this.addAction("bajando precio priceSales en wlm");
							this.partialWlmPriceDeletes.add(currentWlmBasePriceSales);
						} else {
							System.out.println("nulo");
						}
					}

					if (wlmCampaignItemList1Updated == true) {
						this.addAction("update icon1: " + currentwlmCampaignItemList1.getCampaigns() + " a " + icon1);
						this.partialWlmCampaignItemListDeletes
								.add((WlmCampaignItemList) currentwlmCampaignItemList1.clone());
						currentwlmCampaignItemList1.setCampaigns(icon1);
						this.partialWlmCampaignItemListInserts.add(currentwlmCampaignItemList1);
					}
					if (wlmCampaignItemList1Inserted == true) {
						this.addAction("insertando icon1");
						WlmCampaignItemList wlmCampaignItemList1 = new WlmCampaignItemList(skuId.toString(),
								storeId.toString(), 0, icon1);
						currentwlmCampaignItemList1 = wlmCampaignItemList1;
						this.partialWlmCampaignItemListInserts.add(currentwlmCampaignItemList1);
					}
					if (wlmCampaignItemList1Deleted == true) {
						this.addAction("Bajando icon1");
						this.partialWlmCampaignItemListDeletes.add(currentwlmCampaignItemList1);
					}

					if (wlmCampaignItemList2Updated == true) {
						this.addAction("update icon2: " + currentwlmCampaignItemList2.getCampaigns() + " a " + icon2);
						this.partialWlmCampaignItemListDeletes
								.add((WlmCampaignItemList) currentwlmCampaignItemList2.clone());
						currentwlmCampaignItemList2.setCampaigns(icon2);
						this.partialWlmCampaignItemListInserts.add(currentwlmCampaignItemList2);
					}
					if (wlmCampaignItemList2Inserted == true) {
						this.addAction("insertando icon2");
						WlmCampaignItemList wlmCampaignItemList2 = new WlmCampaignItemList(skuId.toString(),
								storeId.toString(), 1, icon2);
						currentwlmCampaignItemList2 = wlmCampaignItemList2;

						this.partialWlmCampaignItemListInserts.add(currentwlmCampaignItemList2);
					}

					if (wlmCampaignItemList2Deleted == true) {
						this.addAction("Bajando icon2");
						this.partialWlmCampaignItemListDeletes.add(currentwlmCampaignItemList2);
					}
				}
				item.setActionPeformed(this.actions);
				this.actions = "";
			} else {
				// System.out.println("No existe el sku en atg");
				item.setError(true);
			}
			this.percentageUpdates = (int) this.i * 100 / this.total;
			projectService.setProgress(project, this.percentageUpdates);
			this.i++;
		}
	}

	private List<PriceListProject> getUniqPriceListProject(List<PriceListProject> plp) {
		Set<PriceListProject> set = new HashSet<PriceListProject>();
		for (PriceListProject pricelp : plp) {
			set.add(pricelp);
		}
		List<PriceListProject> list = new ArrayList<>();
		list.addAll(set);
		return list;
	}

	private List<DcsPrice> getUniqDcsPrice(List<DcsPrice> prices) {
		Set<DcsPrice> set = new HashSet<DcsPrice>();
		for (DcsPrice price : prices) {
			set.add(price);
		}
		List<DcsPrice> list = new ArrayList<>();
		list.addAll(set);
		return list;
	}

	private List<WlmPrice> getUniqWlmPrice(List<WlmPrice> prices) {
		Set<WlmPrice> set = new HashSet<WlmPrice>();
		for (WlmPrice price : prices) {
			set.add(price);
		}
		List<WlmPrice> list = new ArrayList<>();
		list.addAll(set);
		return list;
	}

	private List<WlmCampaignItemList> getUniqCampaignsList(List<WlmCampaignItemList> campaignsList) {
		Set<WlmCampaignItemList> set = new HashSet<WlmCampaignItemList>();
		for (WlmCampaignItemList cList : campaignsList) {
			set.add(cList);
		}
		List<WlmCampaignItemList> list = new ArrayList<>();
		list.addAll(set);
		return list;
	}

	private String addAction(String action) {
		this.actions += this.actions.isEmpty() ? "" : " | ";
		this.actions += action;
		return this.actions;
	}

	static <T> List<List<T>> chopped(List<T> list, final int L) {
		List<List<T>> parts = new ArrayList<List<T>>();
		final int N = list.size();
		for (int i = 0; i < N; i += L) {
			parts.add(new ArrayList<T>(list.subList(i, Math.min(N, i + L))));
		}
		return parts;
	}

	private void deleteCampaignItemsList(List<WlmCampaignItemList> campaignItems)
			throws InterruptedException, ExecutionException {
		int size = campaignItems.size() / 8 + 1;
		List<List<WlmCampaignItemList>> campaignItemsListSplited = chopped(campaignItems, size);

		if (campaignItems.size() > 10) {
			List<CompletableFuture<Integer>> futures = new ArrayList<>();

			for (int i = 0; i < campaignItemsListSplited.size(); i++) {
				int finalI = i;
				futures.add(CompletableFuture.supplyAsync(() -> {
					wlmCampaignItemListService.deleteCampaignListItems(campaignItemsListSplited.get(finalI),
							environment);
					return 3;
				}));
			}

			System.out.println("Deletes en campaignList");
			CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[3]));

			allOf.join();
		} else {
			wlmCampaignItemListService.deleteCampaignListItems(campaignItems, environment);
		}
	}

	private void saveCampaignItemsList(List<WlmCampaignItemList> campaignItems)
			throws InterruptedException, ExecutionException {
		int size = campaignItems.size() / 8 + 1;
		List<List<WlmCampaignItemList>> campaignItemsListSplited = chopped(campaignItems, size);

		if (campaignItems.size() > 10) {
			CompletableFuture.allOf(
					wlmCampaignItemListService.saveCampaignListItems(campaignItemsListSplited.get(0), environment),
					wlmCampaignItemListService.saveCampaignListItems(campaignItemsListSplited.get(1), environment),
					wlmCampaignItemListService.saveCampaignListItems(campaignItemsListSplited.get(2), environment),
					wlmCampaignItemListService.saveCampaignListItems(campaignItemsListSplited.get(3), environment),
					wlmCampaignItemListService.saveCampaignListItems(campaignItemsListSplited.get(4), environment),
					wlmCampaignItemListService.saveCampaignListItems(campaignItemsListSplited.get(5), environment),
					wlmCampaignItemListService.saveCampaignListItems(campaignItemsListSplited.get(6), environment),
					wlmCampaignItemListService.saveCampaignListItems(campaignItemsListSplited.get(7), environment))
					.join();
		} else {
			wlmCampaignItemListService.saveCampaignListItems(campaignItems, environment).join();
		}
	}

	private void saveCampaignItems(List<WlmCampaignItem> campaignItems)
			throws InterruptedException, ExecutionException {
		int size = campaignItems.size() / 8 + 1;
		List<List<WlmCampaignItem>> campaignItemsSplited = chopped(campaignItems, size);

		if (campaignItems.size() > 10) {
			CompletableFuture.allOf(wlmCampaignItemService.saveCampaignItems(campaignItemsSplited.get(0), environment),
					wlmCampaignItemService.saveCampaignItems(campaignItemsSplited.get(1), environment),
					wlmCampaignItemService.saveCampaignItems(campaignItemsSplited.get(2), environment),
					wlmCampaignItemService.saveCampaignItems(campaignItemsSplited.get(3), environment),
					wlmCampaignItemService.saveCampaignItems(campaignItemsSplited.get(4), environment),
					wlmCampaignItemService.saveCampaignItems(campaignItemsSplited.get(5), environment),
					wlmCampaignItemService.saveCampaignItems(campaignItemsSplited.get(6), environment),
					wlmCampaignItemService.saveCampaignItems(campaignItemsSplited.get(7), environment)).join();
		} else {
			wlmCampaignItemService.saveCampaignItems(campaignItems, environment).join();
		}
	}

	private void saveAllPriceUpdates(List<WlmPrice> partialWlmPriceUpdates, List<DcsPrice> dcsPriceUpdates)
			throws InterruptedException, ExecutionException {
		int sizeDcs = dcsPriceUpdates.size() / 8 + 1;
		int sizeWlm = partialWlmPriceUpdates.size() / 8 + 1;
		List<List<WlmPrice>> wlmPriceSplited = chopped(partialWlmPriceUpdates, sizeWlm);
		List<List<DcsPrice>> dcsPriceSplited = chopped(dcsPriceUpdates, sizeDcs);

		if (dcsPriceUpdates.size() > 10) {
			CompletableFuture.allOf(dcsPriceService.savePrices(dcsPriceSplited.get(0), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(1), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(2), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(3), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(4), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(5), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(6), environment),
					dcsPriceService.savePrices(dcsPriceSplited.get(7), environment)).join();
		} else {
			dcsPriceService.savePrices(dcsPriceUpdates, environment).join();
		}
		if (partialWlmPriceUpdates.size() > 10) {
			CompletableFuture.allOf(wlmPriceService.saveWlmPrices(wlmPriceSplited.get(0), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(1), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(2), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(3), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(4), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(5), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(6), environment),
					wlmPriceService.saveWlmPrices(wlmPriceSplited.get(7), environment)).join();
		} else {
			wlmPriceService.saveWlmPrices(partialWlmPriceUpdates, environment).join();
		}

	}

	private void deletePrices(List<WlmPrice> wlmPriceDeletes, List<DcsPrice> dcsPriceDeletes)
			throws InterruptedException, ExecutionException {
		int sizeDcs = dcsPriceDeletes.size() / 8 + 1;
		int sizeWlm = wlmPriceDeletes.size() / 8 + 1;
		List<List<WlmPrice>> wlmPriceSplited = chopped(wlmPriceDeletes, sizeWlm);
		List<List<DcsPrice>> dcsPriceSplited = chopped(dcsPriceDeletes, sizeDcs);

		if (dcsPriceDeletes.size() > 10) {
			List<CompletableFuture<Integer>> futures = new ArrayList<>();

			for (int i = 0; i < dcsPriceSplited.size(); i++) {
				int finalI = i;
				futures.add(CompletableFuture.supplyAsync(() -> {
					dcsPriceService.deletePrices(dcsPriceSplited.get(finalI), environment);
					return 3;
				}));
			}

			System.out.println("Deletes en dcsPrices");
			CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[3]));

			allOf.join();
		} else {
			dcsPriceService.deletePrices(dcsPriceDeletes, environment);
		}
		if (wlmPriceDeletes.size() > 10) {

			List<CompletableFuture<Integer>> futures = new ArrayList<>();

			for (int i = 0; i < wlmPriceSplited.size(); i++) {
				int finalI = i;
				futures.add(CompletableFuture.supplyAsync(() -> {
					wlmPriceService.deleteWlmPrices(wlmPriceSplited.get(finalI), environment);
					return 3;
				}));
			}

			System.out.println("Deletes en dcsPrices");
			CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[3]));

			allOf.join();
		} else {
			wlmPriceService.deleteWlmPrices(wlmPriceDeletes, environment);
		}

	}

	private PriceListProject getRollbackData(PriceListProject rollbackPriceListProject, DcsPrice currentDcsPricePack,
			DcsPrice currentPriceBasePriceReference, DcsPrice curretPriceBasePriceSales, WlmPrice currentWlmPricePack,
			WlmPrice wlmBasePriceReferece, WlmPrice wlmBasePriceSales, WlmCampaignItemList wlmCampaignList1,
			WlmCampaignItemList wlmCampaignList2) {

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

		if (wlmCampaignList1 != null) {
			rollbackPriceListProject.setIcon1(wlmCampaignList1.getCampaigns());
		}

		if (wlmCampaignList2 != null) {
			rollbackPriceListProject.setIcon2(wlmCampaignList2.getCampaigns());
		}

		return rollbackPriceListProject;
	}

	private void generateBccFiles(List<PriceListProject> bccItemsT, Authentication auth) throws IOException {

		// Inicializando cliente sftp
		ssh.initializeClient();
		List<PriceListProject> deltaPriceListProject = new ArrayList<>();
		int sequence = 0;
		for (PriceListProject priceListProject : bccItemsT) {
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

			if (deltaPriceListProject.size() == 5999) {
				String filename = writeFile(deltaPriceListProject, sequence, auth);
				ssh.sendFile(filename, "/opt/shared/priceImport");
				deltaPriceListProject.clear();
				sequence++;

			}

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
			fileName = pathFolder + "/PriceLoader_" + timestamp + "_" + sequence + ".csv";
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

	private List<WlmCampaignItem> getWlmCampaignItem(Set<String> setSkus) {
		Set<String> subset = new HashSet<>();
		List<WlmCampaignItem> wlmCampaignItems = new ArrayList<>();
		int counter = 0;

		for (String skuId : setSkus) {
			subset.add(skuId);
			if (counter > 950) {
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
			if (counter > 950) {
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

		for (String priceId : setPricesIds) {
			subset.add(priceId);
			if (counter > 950) {
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
			if (counter > 950) {
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

		for (String skuId : setSkusPriceList) {
			subset.add(skuId);
			if (counter > 950) {
				List<DcsPrice> dcsPrices = dcsPriceService.findByskuIdIn(subset);
				if (dcsPrices.size() > 0)
					prices.addAll(dcsPrices);
				counter = 0;
				subset.clear();
			}
			counter++;
		}
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

	public void generateBccFiles(List<PriceListProject> myPriceListsProject, Project project, boolean isRollback,
			Authentication auth)
			throws CloneNotSupportedException, IOException, InterruptedException, ExecutionException {
		this.initialize();
		this.total = myPriceListsProject.size();
		System.out.println("Comenzando a recopilar informacion de bd");

		projectService.setState(project, "Recopilanndo datos");
		projectService.setProgress(project, 5);

		if (environment.equals("preview")) {
			catalogService.changeCatPreview();
		}
		if (environment.equals("prod")) {
			dynService.changeToCatb();
		}

		Set<String> setSkusPriceList = myPriceListsProject.stream().map(PriceListProject::getProductNbr)
				.collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());

		Set<String> setProductIds = myPriceListsProject.stream().map(item -> "PROD_" + item.getProductNbr())
				.collect(Collectors.toSet()).stream().map(String::valueOf).collect(Collectors.toSet());
		List<WlmProduct> products = this.getProducts(setProductIds);
		List<DcsPrice> prices = this.getPrices(setSkusPriceList);
		Set<String> setPricesIds = prices.stream().map(item -> item.getPriceId()).collect(Collectors.toSet()).stream()
				.map(String::valueOf).collect(Collectors.toSet());
		List<WlmPrice> wlmPrices = this.getWlmPrices(setPricesIds);
		List<WlmCampaignItem> wlmCampaignItems = this.getWlmCampaignItem(setSkusPriceList);
		List<WlmCampaignItemList> wlmCampaignItemLists = this.getWlmCampaignItemLists(setSkusPriceList);
		Map<String, WlmProduct> productsMap = new HashMap<>();
		Map<String, DcsPrice> pricesMap = new HashMap<>();
		Map<String, WlmPrice> wlmPriceMap = new HashMap<>();
		Map<String, WlmCampaignItem> wlmCampaignItemMap = new HashMap<>();
		Map<String, WlmCampaignItemList> wlmCampaignItemListMap = new HashMap<>();

		for (WlmProduct product : products) {
			productsMap.put(product.getProductId(), product);
		}
		for (DcsPrice price : prices) {
			pricesMap.put(price.getSkuId() + ";" + price.getPriceList(), price);
		}
		for (WlmPrice wlmPrice : wlmPrices) {
			wlmPriceMap.put(wlmPrice.getPriceId(), wlmPrice);
		}
		for (WlmCampaignItem wlmCampaignItem : wlmCampaignItems) {
			wlmCampaignItemMap.put(wlmCampaignItem.getSkuId() + ";" + wlmCampaignItem.getStoreId(), wlmCampaignItem);
		}
		for (WlmCampaignItemList wlmCampaignItemList : wlmCampaignItemLists) {
			wlmCampaignItemListMap.put(wlmCampaignItemList.getSkuId() + ";" + wlmCampaignItemList.getStoreId() + ";"
					+ wlmCampaignItemList.getSequenceNum(), wlmCampaignItemList);
		}

		projectService.setState(project, "Analizando registros");
		projectService.setProgress(project, 14);

		/////////////////////// PAARTE de analizar data/////////////////77

		int size = myPriceListsProject.size() / 10 + 1;
		List<List<PriceListProject>> splitedPriceListProject = chopped(myPriceListsProject, size);
		if (myPriceListsProject.size() > 100) {
			List<CompletableFuture<Integer>> futures = new ArrayList<>();

			for (int i = 0; i < splitedPriceListProject.size(); i++) {
				int finalI = i;
				futures.add(CompletableFuture.supplyAsync(() -> {
					try {
						this.generateBccData(splitedPriceListProject.get(finalI), pricesMap, wlmPriceMap,
								wlmCampaignItemMap, wlmCampaignItemListMap, auth, isRollback, project, productsMap);
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return 3;
				}));
			}
			CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[3]));
			allOf.join();

		} else {
			this.generateBccData(myPriceListsProject, pricesMap, wlmPriceMap, wlmCampaignItemMap,
					wlmCampaignItemListMap, auth, isRollback, project, productsMap);
		}

	}

	private void generateBccData(List<PriceListProject> myPriceListsProject, Map<String, DcsPrice> pricesMap,
			Map<String, WlmPrice> wlmPriceMap, Map<String, WlmCampaignItem> wlmCampaignItemMap,
			Map<String, WlmCampaignItemList> wlmCampaignItemListMap, Authentication auth, boolean isRollback,
			Project project, Map<String, WlmProduct> productsMap) throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		System.out.println("Ejecutando una validación");
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

			WlmCampaignItem currentwlmCampaignItem = wlmCampaignItemMap.get(skuId + ";" + storeId);

			WlmCampaignItemList currentwlmCampaignItemList1 = wlmCampaignItemListMap.get(skuId + ";" + storeId + ";0");
			WlmCampaignItemList currentwlmCampaignItemList2 = wlmCampaignItemListMap.get(skuId + ";" + storeId + ";1");

			PriceListProject rollbackPriceListProject = (PriceListProject) item.clone();

			if (!isRollback) {
				rollbackPriceListProject = getRollbackData(rollbackPriceListProject, currentDcsPricePack,
						currentDcsPriceBasePriceReference, currentDcsPriceBasePriceSales, currentWlmPricePack,
						currentWlmBasePriceReferece, currentWlmBasePriceSales, currentwlmCampaignItemList1,
						currentwlmCampaignItemList2);
				this.rollbackPriceListProjectList.add(rollbackPriceListProject);
			}

			if (productsMap.get(ProductId) != null) {
				boolean insert = false;

				WlmProduct product = productsMap.get(ProductId);
				String uom = product.getContentUom();
				String pricePerUm = this.calculateUom(uom, basePriceSales);

				item.setPricePerUm(pricePerUm);
				boolean dcsPackPriceUpdated = false, wlmPackSizeUpdated = false, wlmPackPricePerUmUpdated = false,
						dcsPackPriceDeleted = false, wlmPackPriceDeleted = false;
				if (basePackPrice != null && basePackPrice != 0) {
					this.totalPack++;
					if (currentDcsPricePack != null && currentWlmPricePack != null) {
						this.updatePack += 1;
						if (!currentDcsPricePack.getListPrice().equals(basePackPrice)) {
							dcsPackPriceUpdated = true;
						}

						if (currentWlmPricePack != null) {
							if (!currentWlmPricePack.getPackSize().equals(basePackSize)) {
								wlmPackSizeUpdated = true;
							}

							if (!currentWlmPricePack.getPricePerUm().isEmpty()
									&& !currentWlmPricePack.getPricePerUm().equals("0")
									&& !currentWlmPricePack.getPricePerUm().equals(pricePerUm)) {
								wlmPackPricePerUmUpdated = true;
							}
						}
					} else {
						// Es inserción
						this.bcc++;
						this.bccItems.add(item);
						insert = true;
						this.addAction("select * from wlm_prod_cata.dcs_price where sku_id='" + skuId
								+ "' and price_list='" + storeId.toString() + ";PackPrice' union");
					}
				} else {
					if (currentDcsPricePack != null) {
						// borrar precio
						dcsPackPriceDeleted = true;
					}
					if (currentWlmPricePack != null) {
						// borrar precio
						wlmPackPriceDeleted = true;
					}
				}

				boolean dcsBasePriceReferenceUpdated = false, wlmBasePriceReferencePerUmUpdated = false,
						dcsBasePriceReferenceDeleted = false, wlmBasePriceReferenceDeleted = false;
				if (basePriceReference != null && basePriceReference != 0) {
					this.totalBase++;
					if (currentDcsPriceBasePriceReference != null && currentWlmBasePriceReferece != null) {
						this.updateBase += 1;
						if (!currentDcsPriceBasePriceReference.getListPrice().equals(basePriceReference)) {
							dcsBasePriceReferenceUpdated = true;
						}

						if (currentWlmBasePriceReferece != null) {
							if (!currentWlmBasePriceReferece.getPricePerUm().isEmpty()
									&& !currentWlmBasePriceReferece.getPricePerUm().equals("0")
									&& !currentWlmBasePriceReferece.getPricePerUm().equals(pricePerUm)) {
								wlmBasePriceReferencePerUmUpdated = true;
							}
						}
					} else {
						// Es inserción
						this.bcc++;
						this.bccItems.add(item);
						insert = true;
						this.addAction("select * from wlm_prod_cata.dcs_price where sku_id='" + skuId
								+ "' and price_list='" + storeId.toString() + ";ListPrice' union");
					}
				} else {

					if (currentDcsPriceBasePriceReference != null) {
						dcsBasePriceReferenceDeleted = true;
					}
					if (currentWlmBasePriceReferece != null) {
						wlmBasePriceReferenceDeleted = true;
					}
				}

				boolean dcsPriceSalesUpdated = false, wlmPriceSalesPerUmUpdated = false, dcsPriceSalesDeleted = false,
						wlmPriceSalesDeleted = false;

				if (basePriceSales != null && basePriceSales != 0) {
					this.totalSales++;
					if (currentDcsPriceBasePriceSales != null && currentWlmBasePriceSales != null) {
						this.updateSales += 1;
						if (!currentDcsPriceBasePriceSales.getListPrice().equals(basePriceSales)) {
							dcsPriceSalesUpdated = true;
						}
						if (currentWlmBasePriceSales != null) {
							if (!currentWlmBasePriceSales.getPricePerUm().isEmpty()
									&& !currentWlmBasePriceSales.getPricePerUm().equals("0")
									&& !currentWlmBasePriceSales.getPricePerUm().equals(pricePerUm)) {
								wlmPriceSalesPerUmUpdated = true;
							}
						}
					} else {
						// Es inserción
						this.bcc++;
						this.bccItems.add(item);
						insert = true;
						this.addAction("select * from wlm_prod_cata.dcs_price where sku_id='" + skuId
								+ "' and price_list='" + storeId.toString() + ";SalePrice' union");
					}
				} else {
					if (currentDcsPriceBasePriceSales != null) {
						dcsPriceSalesDeleted = true;
					}
					if (currentWlmBasePriceSales != null) {
						wlmPriceSalesDeleted = true;
					}
				}

				boolean wlmCampaignItemInserted = false, wlmCampaignItemList1Inserted = false,
						wlmCampaignItemList1Updated = false, wlmCampaignItemList1Deleted = false,
						wlmCampaignItemList2Inserted = false, wlmCampaignItemList2Updated = false,
						wlmCampaignItemList2Deleted = false;

				if (icon1 != null) {
					if (currentwlmCampaignItemList1 != null) {
						// update
						if (!icon1.toString().equals(currentwlmCampaignItemList1.getCampaigns().toString())) {
							// Es distinto, actualizar
							wlmCampaignItemList1Updated = true;
						}
					} else {
						// insert currentwlmCampaignItemList1 es nulo
						wlmCampaignItemList1Inserted = true;
					}
				} else {
					if (currentwlmCampaignItemList1 != null) {
						// delete
						wlmCampaignItemList1Deleted = true;
					}
				}
				if (icon2 != null) {
					if (currentwlmCampaignItemList2 != null) {
						// update
						if (!icon2.toString().equals(currentwlmCampaignItemList2.getCampaigns().toString())) {
							// Es distinto, actualizar
							wlmCampaignItemList2Updated = true;
						}
					} else {
						// insert
						wlmCampaignItemList2Inserted = true;
					}
				} else {
					if (currentwlmCampaignItemList2 != null) {
						// delete
						wlmCampaignItemList2Deleted = true;
					}
				}

				if (currentwlmCampaignItem == null
						&& (wlmCampaignItemList1Inserted == true || wlmCampaignItemList2Inserted == true)) {
					WlmCampaignItem wlmCampaignItem = new WlmCampaignItem(skuId.toString(), storeId.toString());
					currentwlmCampaignItem = wlmCampaignItem;
					wlmCampaignItemInserted = true;
				}

				if (insert == false) {
					// Se evalua si se actualizó y se agrega
					if (dcsPackPriceUpdated == true && !currentDcsPricePack.getPriceId().isEmpty()) {
						if (currentDcsPricePack != null) {
							this.addAction(
									"update packPrice: " + currentDcsPricePack.getListPrice() + " a " + basePackPrice);
							currentDcsPricePack.setListPrice(basePackPrice);
							this.partialDcsPriceUpdates.add(currentDcsPricePack);
						} else {
							System.out.println("Nulo");
						}

					}
					if (wlmPackSizeUpdated == true) {
						if (currentWlmPricePack != null) {
							this.addAction(
									"update PricePack: " + currentWlmPricePack.getPackSize() + " a " + basePackSize);
							currentWlmPricePack.setPackSize(basePackSize);
							this.partialWlmPriceUpdates.add(currentWlmPricePack);
						} else {
							System.out.println("Nulo");
						}
					}
					if (wlmPackPricePerUmUpdated == true) {
						if (currentWlmPricePack != null) {
							this.addAction(
									"update PricePerUm: " + currentWlmPricePack.getPricePerUm() + " a " + pricePerUm);
							currentWlmPricePack.setPricePerUm(pricePerUm);
							this.partialWlmPriceUpdates.add(currentWlmPricePack);
						} else {
							System.out.println("Nulo");
						}
					}
					if (dcsPackPriceDeleted == true) {
						if (currentDcsPricePack != null) {
							this.addAction("bajando pricePack en dcs");
							this.partialDcsPriceDeletes.add(currentDcsPricePack);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmPackPriceDeleted == true) {
						if (currentWlmPricePack != null) {
							this.addAction("bajando pricePack en wlm");
							this.partialWlmPriceDeletes.add(currentWlmPricePack);
						} else {
							System.out.println("nulo");
						}
					}

					if (dcsBasePriceReferenceUpdated == true) {
						if (currentDcsPriceBasePriceReference != null) {
							this.addAction("update basePriceReference: "
									+ currentDcsPriceBasePriceReference.getListPrice() + " a " + basePriceReference);
							currentDcsPriceBasePriceReference.setListPrice(basePriceReference);
							this.partialDcsPriceUpdates.add(currentDcsPriceBasePriceReference);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmBasePriceReferencePerUmUpdated == true) {
						if (currentWlmBasePriceReferece != null) {
							this.addAction("update PricePerUm: " + currentWlmBasePriceReferece.getPricePerUm() + " a "
									+ pricePerUm);
							currentWlmBasePriceReferece.setPricePerUm(pricePerUm);
							this.partialWlmPriceUpdates.add(currentWlmBasePriceReferece);
						} else {
							System.out.println("nulo");
						}
					}
					if (dcsBasePriceReferenceDeleted == true) {
						if (currentDcsPriceBasePriceReference != null) {
							this.addAction("bajando priceReference en dcs");
							this.partialDcsPriceDeletes.add(currentDcsPriceBasePriceReference);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmBasePriceReferenceDeleted == true) {
						if (currentWlmBasePriceReferece != null) {
							this.addAction("bajando priceReference en wlm");
							this.partialWlmPriceDeletes.add(currentWlmBasePriceReferece);
						} else {
							System.out.println("nulo");
						}
					}

					if (dcsPriceSalesUpdated == true) {
						if (currentDcsPriceBasePriceSales != null) {
							this.addAction("update basePriceSales: " + currentDcsPriceBasePriceSales.getListPrice()
									+ " a " + basePriceSales);
							currentDcsPriceBasePriceSales.setListPrice(basePriceSales);
							this.partialDcsPriceUpdates.add(currentDcsPriceBasePriceSales);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmPriceSalesPerUmUpdated == true) {
						if (currentWlmBasePriceSales != null) {
							this.addAction("update PricePerUm: " + currentWlmBasePriceSales.getPricePerUm() + " a "
									+ pricePerUm);
							currentWlmBasePriceSales.setPricePerUm(pricePerUm);
							this.partialWlmPriceUpdates.add(currentWlmBasePriceSales);
						} else {
							System.out.println("nulo");
						}
					}
					if (dcsPriceSalesDeleted == true) {
						if (currentDcsPriceBasePriceSales != null) {
							this.addAction("bajando precio priceSales en dcs");
							this.partialDcsPriceDeletes.add(currentDcsPriceBasePriceSales);
						} else {
							System.out.println("nulo");
						}
					}
					if (wlmPriceSalesDeleted == true) {
						if (currentWlmBasePriceSales != null) {
							this.addAction("bajando precio priceSales en wlm");
							this.partialWlmPriceDeletes.add(currentWlmBasePriceSales);
						} else {
							System.out.println("nulo");
						}
					}

					if (wlmCampaignItemList1Updated == true) {
						this.addAction("update icon1: " + currentwlmCampaignItemList1.getCampaigns() + " a " + icon1);
						this.partialWlmCampaignItemListDeletes
								.add((WlmCampaignItemList) currentwlmCampaignItemList1.clone());
						currentwlmCampaignItemList1.setCampaigns(icon1);
						this.partialWlmCampaignItemListInserts.add(currentwlmCampaignItemList1);
					}
					if (wlmCampaignItemList1Inserted == true) {
						this.addAction("insertando icon1");
						WlmCampaignItemList wlmCampaignItemList1 = new WlmCampaignItemList(skuId.toString(),
								storeId.toString(), 0, icon1);
						currentwlmCampaignItemList1 = wlmCampaignItemList1;
						this.partialWlmCampaignItemListInserts.add(currentwlmCampaignItemList1);
					}
					if (wlmCampaignItemList1Deleted == true) {
						this.addAction("Bajando icon1");
						this.partialWlmCampaignItemListDeletes.add(currentwlmCampaignItemList1);
					}

					if (wlmCampaignItemList2Updated == true) {
						this.addAction("update icon2: " + currentwlmCampaignItemList2.getCampaigns() + " a " + icon2);
						this.partialWlmCampaignItemListDeletes
								.add((WlmCampaignItemList) currentwlmCampaignItemList2.clone());
						currentwlmCampaignItemList2.setCampaigns(icon2);
						this.partialWlmCampaignItemListInserts.add(currentwlmCampaignItemList2);
					}
					if (wlmCampaignItemList2Inserted == true) {
						this.addAction("insertando icon2");
						WlmCampaignItemList wlmCampaignItemList2 = new WlmCampaignItemList(skuId.toString(),
								storeId.toString(), 1, icon2);
						currentwlmCampaignItemList2 = wlmCampaignItemList2;

						this.partialWlmCampaignItemListInserts.add(currentwlmCampaignItemList2);
					}

					if (wlmCampaignItemList2Deleted == true) {
						this.addAction("Bajando icon2");
						this.partialWlmCampaignItemListDeletes.add(currentwlmCampaignItemList2);
					}

					if (wlmCampaignItemInserted == true) {
						this.partialWlmCampaignItemInserts.add(currentwlmCampaignItem);
					}
				}
				item.setActionPeformed(this.actions);
				this.actions = "";
			} else {
				// System.out.println("No existe el sku en atg");
				item.setError(true);
			}
			this.percentageUpdates = (int) this.i * 100 / this.total;
			projectService.setProgress(project, this.percentageUpdates);
			this.i++;
		}
	}

}
