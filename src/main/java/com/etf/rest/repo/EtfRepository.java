package com.etf.rest.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.etf.rest.entity.EtfInfoCollections;

public interface EtfRepository extends MongoRepository<EtfInfoCollections, Long> {

}
