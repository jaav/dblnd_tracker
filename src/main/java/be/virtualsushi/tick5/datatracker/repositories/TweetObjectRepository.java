package be.virtualsushi.tick5.datatracker.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.virtualsushi.tick5.datatracker.model.TweetObject;
import be.virtualsushi.tick5.datatracker.model.TweetObjectTypes;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TweetObjectRepository extends Tick5Repository<TweetObject> {

	public TweetObject findByValueAndType(String value, TweetObjectTypes type);

	public List<TweetObject> findByType(TweetObjectTypes type);



	@Transactional
	@Modifying
	@Query(value = "delete from TweetObject where id in ?1")
	void deleteTweetObjects(List<Long> ids);

}
