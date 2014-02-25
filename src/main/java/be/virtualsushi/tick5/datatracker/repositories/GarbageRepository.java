package be.virtualsushi.tick5.datatracker.repositories;

import be.virtualsushi.tick5.datatracker.model.Garbage;
import be.virtualsushi.tick5.datatracker.model.TwitterUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarbageRepository extends Tick5Repository<Garbage> {

	@Query("from Garbage")
	public List<Garbage> findAll();


}
