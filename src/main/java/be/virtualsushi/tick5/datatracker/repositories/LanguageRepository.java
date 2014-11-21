package be.virtualsushi.tick5.datatracker.repositories;

import be.virtualsushi.tick5.datatracker.model.Garbage;
import be.virtualsushi.tick5.datatracker.model.Language;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository extends Tick5Repository<Language> {

	@Query("from Language")
	public List<Language> findAll();

	@Query(value = "from Language l where l.culture=:culture")
	public List<Language> findByLanguage(@Param("culture") String culture);


}
