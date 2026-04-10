package com.configapp.repository;

import com.configapp.model.ConfigurationStats;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConfigurationStatsRepository extends MongoRepository<ConfigurationStats, String> {
    Optional<ConfigurationStats> findByConfigurationId(String configurationId);
}
