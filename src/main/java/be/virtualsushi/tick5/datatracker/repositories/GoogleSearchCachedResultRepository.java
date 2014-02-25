package be.virtualsushi.tick5.datatracker.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.virtualsushi.tick5.datatracker.model.GoogleSearchCachedResult;

@Repository
public interface GoogleSearchCachedResultRepository extends Tick5Repository<GoogleSearchCachedResult> {

	@Query("from GoogleSearchCachedResult where queryString like %?1")
	public List<GoogleSearchCachedResult> findByQuery(String query);

}
