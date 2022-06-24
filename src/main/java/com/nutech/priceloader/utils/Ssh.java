package com.nutech.priceloader.utils;

import java.io.IOException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

@ConfigurationProperties(prefix = "ssh")
@Configuration
@Component
@Data
public class Ssh {
	private String username;
	private String password;
	private String remoteHost;

	private SSHClient sshClient;
	private SFTPClient sftpClient;

	public Ssh() {
	}

	public void initializeClient() throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(this.remoteHost);
		client.authPassword(this.username, this.password);
		this.sshClient = client;
		this.sftpClient = client.newSFTPClient();
	}

	public void sendFile(String localFile, String remoteDir) throws IOException {
		String parts[] = localFile.split("/");
		String file = parts[parts.length - 1];

		this.sftpClient.put(localFile, remoteDir + "/" + file);
	}
	
	public void closeClient() {
		try {
			this.sftpClient.close();
			this.sshClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
