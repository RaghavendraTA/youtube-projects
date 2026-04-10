package com.configapp.repository;

import com.configapp.model.Configuration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends MongoRepository<Configuration, String> {
    Optional<Configuration> findByConfigurationIdAndActive(String configurationId, Boolean active);
    
    List<Configuration> findByConfigurationId(String configurationId);
    
    @Query("{ 'configurationId': ?0, 'owner': ?1 }")
    Optional<Configuration> findByConfigurationIdAndOwner(String configurationId, String owner);
    
    @Query("{ '$or': [ { 'owner': ?0 }, { 'adminIds': ?0 } ], 'active': true }")
    List<Configuration> findActiveConfigurationsByUser(String userId);
}
