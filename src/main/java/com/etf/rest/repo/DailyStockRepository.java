package com.etf.rest.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.etf.rest.entity.DailyStockCollections;

public interface DailyStockRepository extends MongoRepository<DailyStockCollections, Long> {

}
