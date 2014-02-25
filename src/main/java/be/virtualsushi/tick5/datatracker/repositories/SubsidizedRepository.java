package be.virtualsushi.tick5.datatracker.repositories;

import be.virtualsushi.tick5.datatracker.model.Garbage;
import be.virtualsushi.tick5.datatracker.model.Subsidized;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubsidizedRepository extends Tick5Repository<Subsidized> {

	@Query("from Subsidized")
	public List<Subsidized> findAll();


}
