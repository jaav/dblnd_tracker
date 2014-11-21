package be.virtualsushi.tick5.datatracker.repositories;

import be.virtualsushi.tick5.datatracker.model.CountryMin;
import be.virtualsushi.tick5.datatracker.model.CountryPlus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryMinRepository extends Tick5Repository<CountryMin> {

	@Query("from CountryMin")
	public List<CountryMin> findAll();


}
