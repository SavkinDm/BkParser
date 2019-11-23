package com.sdm.BkParser.Repositories;


import com.sdm.BkParser.Entity.BasketballMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface basketballMatchRepo extends JpaRepository<BasketballMatch, Long> {
}
