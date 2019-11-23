package com.sdm.BkParser.Repositories;


import com.sdm.BkParser.Entity.VolleyballMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface volleyballMatchRepo extends JpaRepository<VolleyballMatch, Long> {

}
