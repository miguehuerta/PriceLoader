package com.nutech.priceloader.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.nutech.priceloader.entities.ExcelFile;
import com.nutech.priceloader.entities.ExcelFileConstants;
import com.nutech.priceloader.entities.ExcelValidationFile;
import com.nutech.priceloader.entities.FileErrorLog;
import com.nutech.priceloader.entities.PriceListProject;
import com.nutech.priceloader.utils.Helpers;

import lombok.Data;

@Component
@Data
public class ExcelFilelValidationHelper {
	@Autowired
	ExcelFileConstants cons;

	private String fileLocation;

	@Autowired
	Helpers helpers;

	public ExcelFilelValidationHelper() {
	}

	public ExcelFilelValidationHelper(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	private List<FileErrorLog> getErrors(List<String> errors, ExcelValidationFile ef) {
		// TODO Auto-generated method stub
		List<FileErrorLog> fileErrorLogList = new ArrayList<FileErrorLog>();
		for (String error : errors) {
			FileErrorLog fel = new FileErrorLog(error, ef);
			fileErrorLogList.add(fel);
		}
		return fileErrorLogList;
	}

	private boolean validateField(Cell cell, List<CellType> types, String fieldWaited) {
		CellType typeCell = cell.getCellType();
		try {
			if (!types.contains(typeCell)) {
				return false;
			} else {
				if (!fieldWaited.equals(""))
					if (!fieldWaited.equals(cell.getRichStringCellValue().toString().replaceAll(" ", ""))) {
						return false;
					}
			}
		} catch (IllegalStateException ex) {
			System.out.println("Fallo conversión");
		}

		return true;
	}

	public ArrayList<PriceListProject> getPriceListFile(ExcelFile excelFile) {
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		ArrayList<PriceListProject> priceListProjectList = new ArrayList<PriceListProject>();
		try {
			file = new FileInputStream(new File(excelFile.getLocation()));

			workbook = new XSSFWorkbook(file);

			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			System.out.println("Reevisando archivo " + excelFile.getLocation());
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				Cell StoreNbr = row.getCell(0);
				Cell ProductNbr = row.getCell(1);
				Cell BasePriceReference = row.getCell(2);
				Cell BasePriceSales = row.getCell(3);
				Cell Icon1 = row.getCell(4);
				Cell Icon2 = row.getCell(5);
				Cell BasePackSize = row.getCell(6);
				Cell BasePackPrice = row.getCell(7);

				boolean minGoodOk = false;
				PriceListProject priceListProject = new PriceListProject();

				if (row.getRowNum() > 0 && StoreNbr != null && ProductNbr != null) {
					priceListProject.setStoreNbr((int) StoreNbr.getNumericCellValue());
					priceListProject.setProductNbr((int) ProductNbr.getNumericCellValue());
					if (BasePriceReference != null && BasePriceSales != null) {
						priceListProject.setBasePriceReference((int) BasePriceReference.getNumericCellValue());
						priceListProject.setBasePriceSales((int) BasePriceSales.getNumericCellValue());
						minGoodOk = true;
					}
					if (minGoodOk) {
						if (Icon1 != null && validateField(Icon1, cons.STR(), "")) {
							priceListProject.setIcon1(Icon1.getRichStringCellValue().toString());
						}
						if (Icon2 != null && validateField(Icon2, cons.STR(), "")) {
							priceListProject.setIcon2(Icon2.getRichStringCellValue().toString());
						}

						if (BasePackSize != null && BasePackPrice != null) {
							priceListProject.setBasePackPrice((int) BasePackPrice.getNumericCellValue());
							priceListProject.setBasePackSize((int) BasePackSize.getNumericCellValue());
						}
						priceListProject.setProject(excelFile.getProject());
						priceListProjectList.add(priceListProject);
					}

				}

			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException ex) {
			System.out.println("Error leyendo archivo " + this.fileLocation);
		} catch (NoSuchElementException ex) {
			System.out.println("La cantidad de campos del archivo no es la esperada");
		} finally {
			try {
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return priceListProjectList;

	}

	private List<String> validateBody() {
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		final List<String> errors = new ArrayList<String>();
		try {
			file = new FileInputStream(new File(this.fileLocation));
			workbook = new XSSFWorkbook(file);

			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			while (rowIterator.hasNext()) {

				Row row = rowIterator.next();
				if (row.getRowNum() > 0) {
					Cell StoreNbr = row.getCell(0);
					Cell ProductNbr = row.getCell(1);
					Cell BasePriceReference = row.getCell(2);
					Cell BasePriceSales = row.getCell(3);
					Cell Icon1 = row.getCell(4);
					Cell Icon2 = row.getCell(5);
					Cell BasePackSize = row.getCell(6);
					Cell BasePackPrice = row.getCell(7);

					// ProductNbr BasePriceReference BasePriceSales Icon1 Icon2 BasePackSize
					// BasePackPrice
					boolean minGoodOk = false;
					int fila = row.getRowNum() + 1;
					if (row.getRowNum() > 0 && StoreNbr != null && ProductNbr != null) {
						if (!validateField(StoreNbr, cons.NUM(), ""))
							errors.add(String.format("El campo con coordenadas A%s es incorrecto, se esperaba %s", fila,
									"Número"));
						if (!validateField(ProductNbr, cons.NUM(), ""))
							errors.add(String.format("El campo con coordenadas B%s es incorrecto, se esperaba %s", fila,
									"Número"));
						if (BasePriceReference != null && BasePriceSales != null) {
							if (!validateField(BasePriceReference, cons.NUM(), "")) {
								errors.add(String.format(
										"El campo con coordenadas B%s es incorrecto, se esperaba Número", fila));
							} else {
								if (!validateField(BasePriceSales, cons.NUM(), "")) {
									errors.add(String.format(
											"El campo con coordenadas C%s es incorrecto, se esperaba Número", fila));
								} else {
									// Aquí son número y existen ambos precios
									int BasePriceReferenceValue = (int) BasePriceReference.getNumericCellValue();
									int BasePriceSalesValue = (int) BasePriceSales.getNumericCellValue();
									if (BasePriceSalesValue > BasePriceReferenceValue) {
										errors.add(String.format(
												"El precio con fila %s es incorrecto, el BasePriceSales no puede ser mayor al BasePriceReferenceValue",
												fila));
									} else {
										minGoodOk = true;
									}
								}
							}

						}
						if (minGoodOk) {
							if (Icon1 != null && !validateField(Icon1, cons.STR_OR_EMPTY(), "")) {
								errors.add(String.format(
										"El campo con coordenadas E%s es incorrecto, se esperaba Texto o vacío", fila));
							}
							if (Icon2 != null && !validateField(Icon2, cons.STR_OR_EMPTY(), "")) {
								errors.add(String.format(
										"El campo con coordenadas E%s es incorrecto, se esperaba Texto o vacío", fila));
							}
							if (BasePackSize != null || BasePackPrice != null) {
								if (BasePackSize != null && BasePackPrice != null) {
									if (!validateField(BasePackSize, cons.NUM_OR_EMPTY(), "")) {
										errors.add(String.format(
												"El campo con coordenadas G%s es incorrecto, se esperaba Número",
												fila));
									} else {
										if (!validateField(BasePackPrice, cons.NUM_OR_EMPTY(), "")) {
											errors.add(String.format(
													"El campo con coordenadas H%s es incorrecto, se esperaba Número",
													fila));
										} else {
											// Aquí son número y existen ambos
											int BasePackSizeValue = (int) BasePackSize.getNumericCellValue();
											int BasePackPriceValue = (int) BasePackPrice.getNumericCellValue();

											int BasePriceReferenceValue = (int) BasePriceReference
													.getNumericCellValue();
											int BasePriceSalesValue = (int) BasePriceSales.getNumericCellValue();

											if ((BasePackPriceValue!=0 && BasePackPriceValue < BasePriceSalesValue)
													|| BasePackPriceValue!=0 && BasePackPriceValue < BasePriceReferenceValue) {
												errors.add(String.format("El precio pack en la fila %s más bajo que el BasePriceSales o el BasePriceReferenceValue",fila));
											} else {
												if (BasePackSizeValue!=0 && BasePackSizeValue < 2) {
													errors.add(String.format(
															"La cantidad en campo BasePackSize en la fila %s debe ser mayor que 1, ya que se está especificando un precio de tipo BasePackPrice",
															fila));
												}
											}
										}
									}
								} else {
									errors.add(String.format(
											"La fila %s es incorrecta, los campos BasePackSize y BasePackPrice son dependientes y no puede ir nulo alguno si el otro está lleno",
											fila));
								}
							}
						}

					} else {
						String message = String.format(
								"Los campos mínimos requeridos para la fila %s no están que son local y producto",
								fila);
						errors.add(message);
					}

				}
			}
		} catch (IOException ex) {
			System.out.println("Error leyendo archivo " + this.fileLocation);
		} catch (NoSuchElementException ex) {
			errors.add("La cantidad de campos del archivo no es la esperada");
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return errors;

	}


	private List<String> validateHeaders() {
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		final List<String> errors = new ArrayList<String>();
		try {
			file = new FileInputStream(new File(this.fileLocation));
			workbook = new XSSFWorkbook(file);

			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			Row firstRow = rowIterator.next();
			Iterator<Cell> cellIterator = firstRow.cellIterator();

			for (int i = 0; i < 8; i++) {
				Cell cell = cellIterator.next();
				switch (i) {
				case 0:
					if (!validateField(cell, cons.STR(), cons.getStoreField()))
						errors.add(String
								.format("El campo con coordennadas A1 es incorrecto, se esperaba %s y contiene %s",
										cons.getStoreField(), cell.getRichStringCellValue().toString())
								.toString());
					break;
				case 1:
					if (!validateField(cell, cons.STR(), cons.getProductField()))
						errors.add(String
								.format("El campo con coordennadas B1 es incorrecto, se esperaba %s y contiene %s",
										cons.getProductField(), cell.getRichStringCellValue().toString())
								.toString());
					break;
				case 2:
					if (!validateField(cell, cons.STR(), cons.getBasePriceField()))
						errors.add(String
								.format("El campo con coordennadas C1 es incorrecto, se esperaba %s y contiene %s",
										cons.getBasePriceField(), cell.getRichStringCellValue().toString())
								.toString());
					break;
				case 3:
					if (!validateField(cell, cons.STR(), cons.getSalePriceField()))
						errors.add(String
								.format("El campo con coordennadas D1 es incorrecto, se esperaba %s y contiene %s",
										cons.getSalePriceField(), cell.getRichStringCellValue().toString())
								.toString());
					break;
				case 4:
					if (!validateField(cell, cons.STR(), cons.getIcon1Field()))
						errors.add(String
								.format("El campo con coordennadas E1 es incorrecto, se esperaba %s y contiene %s",
										cons.getIcon1Field(), cell.getRichStringCellValue().toString())
								.toString());
					break;
				case 5:
					if (!validateField(cell, cons.STR(), cons.getIcon2Field()))
						errors.add(String
								.format("El campo con coordennadas F1 es incorrecto, se esperaba %s y contiene %s",
										cons.getIcon2Field(), cell.getRichStringCellValue().toString())
								.toString());
					break;
				case 6:
					if (!validateField(cell, cons.STR(), cons.getPackSize()))
						errors.add(String
								.format("El campo con coordennadas G1 es incorrecto, se esperaba %s y contiene %s",
										cons.getPackSize(), cell.getRichStringCellValue().toString())
								.toString());
					break;
				case 7:
					if (!validateField(cell, cons.STR(), cons.getPackPrice()))
						errors.add(String
								.format("El campo con coordennadas H1 es incorrecto, se esperaba %s y contiene %s",
										cons.getPackPrice(), cell.getRichStringCellValue().toString())
								.toString());
					break;
				}
			}

		} catch (IOException ex) {
			System.out.println("Error leyendo archivo " + this.fileLocation);
		} catch (NoSuchElementException ex) {
			errors.add("La cantidad de campos del archivo no es la esperada");
		} finally {
			try {
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return errors;
	}

	public void printDetails(Cell cell) {
		CellType type = cell.getCellType();
		if (type == CellType.STRING) {
			System.out.printf("[%d, %d] = STRING; Value = %s%n", cell.getRowIndex(), cell.getColumnIndex(),
					cell.getRichStringCellValue().toString());
		} else if (type == CellType.NUMERIC) {
			System.out.printf("[%d, %d] = NUMERIC; Value = %f%n", cell.getRowIndex(), cell.getColumnIndex(),
					cell.getNumericCellValue());
		} else if (type == CellType.BOOLEAN) {
			System.out.printf("[%d, %d] = BOOLEAN; Value = %b%n", cell.getRowIndex(), cell.getColumnIndex(),
					cell.getBooleanCellValue());
		} else if (type == CellType.BLANK) {
			System.out.printf("[%d, %d] = BLANK CELL%n", cell.getRowIndex(), cell.getColumnIndex());
		}
	}

	public List<FileErrorLog> validate(ExcelValidationFile excelValidationFile) {
		List<String> errorsHeaders = this.validateHeaders();
		List<FileErrorLog> fileErrors = null;
		if (errorsHeaders.size() > 0) {
			fileErrors = this.getErrors(errorsHeaders, excelValidationFile);
		} else {
			List<String> errorsBody = this.validateBody();
			if (errorsBody.size() > 0) {
				fileErrors = this.getErrors(errorsBody, excelValidationFile);
			}
		}
		return fileErrors;
	}

	public void initialize(Authentication authentication, MultipartFile file) {
		// TODO Auto-generated method stub
		this.checkFolder(authentication.getName());
		this.uploadFsFile(authentication.getName(), file);
		System.out.println(this.fileLocation);
	}

	private void uploadFsFile(String username, MultipartFile file) {
		this.fileLocation = helpers.saveFsFile(file, cons.getLocationExcelFiles() + username);
	}

	private void checkFolder(String username) {
		// TODO Auto-generated method stub
		String folderPath = cons.getLocationExcelFiles() + username;
		if (!helpers.isFolderCreated(folderPath)) {
			System.out.println("Folder no estaba creado");
			helpers.createDirectory(folderPath);

		} else {
			System.out.println("Folder ya estaba creado");
		}
	}
}
