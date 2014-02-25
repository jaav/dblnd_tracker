package be.virtualsushi.tick5.datatracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Tick5Repository<E> extends JpaRepository<E, Long> {

}
