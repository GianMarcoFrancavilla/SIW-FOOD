package it.uniroma3.siw.repository;


import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Ingredient;

public interface IngredientRepository extends CrudRepository<Ingredient, Long> {

	public boolean existsByName(String name);	

//	@Query(value="select * "
//			+ "from artist a "
//			+ "where a.id not in "
//			+ "(select actors_id "
//			+ "from movie_actors "
//			+ "where movie_actors.starred_movies_id = :movieId)", nativeQuery=true)
//	public Iterable<Chef> findActorsNotInMovie(@Param("movieId") Long id);


}