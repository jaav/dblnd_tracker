package be.virtualsushi.tick5.datatracker.repositories;

import be.virtualsushi.tick5.datatracker.model.Garbage;
import be.virtualsushi.tick5.datatracker.model.PopularSource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularSourceRepository extends Tick5Repository<PopularSource> {

	@Query("from PopularSource")
	public List<PopularSource> findAll();


}
