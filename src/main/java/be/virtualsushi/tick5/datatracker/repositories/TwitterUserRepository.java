package be.virtualsushi.tick5.datatracker.repositories;

import java.util.List;

import be.virtualsushi.tick5.datatracker.model.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.virtualsushi.tick5.datatracker.model.TwitterUser;

@Repository
public interface TwitterUserRepository extends Tick5Repository<TwitterUser> {

	@Query("from TwitterUser")
	public List<TwitterUser> findAll();

	/*@Query(value = "from TwitterUser where type=be.virtualsushi.tick5.datatracker.model.TweepTypes.MEMBER and LANGUAGE = :trackingLanguage", countQuery = "select count(id) from TwitterUser where type=be.virtualsushi.tick5.datatracker.model.TweepTypes.MEMBER and LANGUAGE = :trackingLanguage")
	public Page<TwitterUser> findAllListMembers(@Param("trackingLanguage") String trackingLanguage, Pageable pageable);*/

	@Query(value = "from TwitterUser where type=be.virtualsushi.tick5.datatracker.model.TweepTypes.MEMBER", countQuery = "select count(id) from TwitterUser where type=be.virtualsushi.tick5.datatracker.model.TweepTypes.MEMBER")
		public Page<TwitterUser> findAllListMembers(Pageable pageable);



}
