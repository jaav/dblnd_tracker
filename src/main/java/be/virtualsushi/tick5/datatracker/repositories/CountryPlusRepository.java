package be.virtualsushi.tick5.datatracker.repositories;

import be.virtualsushi.tick5.datatracker.model.CountryPlus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryPlusRepository extends Tick5Repository<CountryPlus> {

	@Query("from CountryPlus")
	public List<CountryPlus> findAll();


}
