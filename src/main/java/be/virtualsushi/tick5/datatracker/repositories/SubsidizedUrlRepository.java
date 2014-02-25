package be.virtualsushi.tick5.datatracker.repositories;

import be.virtualsushi.tick5.datatracker.model.Subsidized;
import be.virtualsushi.tick5.datatracker.model.SubsidizedUrl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubsidizedUrlRepository extends Tick5Repository<SubsidizedUrl> {

	@Query("from SubsidizedUrl")
	public List<SubsidizedUrl> findAll();

}
