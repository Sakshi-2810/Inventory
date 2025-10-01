package com.myStore.myStore.repository;

import com.myStore.myStore.model.StockDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDetailRepository extends MongoRepository<StockDetail, Integer> {
    StockDetail findTopByOrderByItemIdDesc();
}
