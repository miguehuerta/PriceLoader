package com.nutech.priceloader.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
@Table(name="user", schema = "public")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private int id;
	@Column(name="firstname", nullable = true)
	private String firstname;
	@Column(name="lastname", nullable = true)
	private String lastname;
	@Column(name="username", nullable = true, unique=true)
	private String username;
	@Column(name="password", nullable=true)
	private String password;
	@Column(name="role", nullable=true)
	private String role;
	@Column(name="enabled", nullable=true)
	private boolean enabled;


	public User(){}

	public User(String name, String lastname, String username, String passsword, String role, boolean enabled) {
		super();
		this.firstname = name;
		this.lastname = lastname;
		this.username = username;
		this.password = passsword;
		this.role = role;
		this.enabled = enabled;
	}

}