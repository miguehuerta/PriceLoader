package com.nutech.priceloader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutech.priceloader.entities.CatchingControl;

@Repository
public interface CatchingControlRepository extends JpaRepository<CatchingControl, Integer>{

	CatchingControl findByName(String name);

}
