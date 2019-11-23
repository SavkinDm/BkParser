package com.sdm.BkParser.Repositories;


import com.sdm.BkParser.Entity.HockeyMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface hockeyMatchRepo extends JpaRepository<HockeyMatch, Long> {

}
