package com.myStore.myStore.repository;

import com.myStore.myStore.model.Party;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends MongoRepository<Party, Integer> {
    Party findTopByOrderByPartyIdDesc();
    Party findByName(String name);

    boolean existsByName(@NotBlank String partyName);
}
