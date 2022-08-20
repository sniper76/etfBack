package com.etf.rest.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.etf.rest.entity.StockInfoCollections;

public interface StockRepository extends MongoRepository<StockInfoCollections, Long> {

}
