package com.etf.rest.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.etf.rest.entity.KrxDataCollections;

public interface KrxDataRepository extends MongoRepository<KrxDataCollections, Long> {

}
