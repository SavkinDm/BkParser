package com.sdm.BkParser.Repositories;


import com.sdm.BkParser.Entity.FootballMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface footballMatchRepo extends JpaRepository<FootballMatch, Long> {


}
