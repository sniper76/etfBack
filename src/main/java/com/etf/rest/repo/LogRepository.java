package com.etf.rest.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.etf.rest.entity.LogCollections;

public interface LogRepository extends MongoRepository<LogCollections, Long> {

}
