package com.nutech.priceloader.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.hpsf.Array;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

@Component
public class Helpers {

	public Helpers() {
	}

	public LocalDateTime getCurrentDate() {
		LocalDateTime now = LocalDateTime.now();
		return now;
	}

	// ejemplo yyyy-MM-dd 'at' HH:mm:ss
	public String formatDate(String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date date = new Date(System.currentTimeMillis());
		return formatter.format(date);
	}

	public String saveFsFile(MultipartFile file, String folder) {
		String fileLocation = "";
		try {
			InputStream in = file.getInputStream();
			Date date = new Date();
			Long seconds = date.getTime();
			fileLocation = folder + "/" + "u" + seconds + "eu_" + file.getOriginalFilename();
			FileOutputStream f = new FileOutputStream(fileLocation);
			int ch = 0;
			while ((ch = in.read()) != -1) {
				f.write(ch);
			}
			f.flush();
			f.close();
		} catch (IOException ex) {
			fileLocation = "";
			ex.printStackTrace();
		}
		return fileLocation;
	}

	public boolean deleteFsFile(String filepath) {
		File myObj = new File(filepath);
		boolean wasDeleted = false;
		if (myObj.delete()) {
			System.out.println("Deleted the file: " + myObj.getName());
			wasDeleted = true;
		} else {
			System.out.println("Failed to delete the file.");
		}
		return wasDeleted;
	}

	public boolean isFolderCreated(String pathFolder) {
		boolean exists = false;
		File f = new File(pathFolder);
		if (f.exists() && f.isDirectory()) {
			exists = true;
		}
		return exists;
	}

	public boolean createDirectory(String pathFolder) {
		boolean created = true;
		try {
			Path path = Paths.get(pathFolder);
			Files.createDirectories(path);
			System.out.println("Directory is created!");
		} catch (IOException e) {
			System.err.println("Failed to create directory!" + e.getMessage());
			created = false;
		}
		return created;
	}

	public static <T, U> Set<U> convertIntSetToStringSet(Set<T> setOfInteger, Function<T, U> function) {
		return setOfInteger.stream().map(function).collect(Collectors.toSet());
	}


}
