package com.sdm.BkParser.Repositories;


import com.sdm.BkParser.Entity.TennisMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface tennisMatchRepo extends JpaRepository<TennisMatch, Long>{

}
