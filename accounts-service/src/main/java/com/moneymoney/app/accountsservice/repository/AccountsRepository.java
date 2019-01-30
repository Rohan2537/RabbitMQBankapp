package com.moneymoney.app.accountsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.moneymoney.app.accountsservice.entity.Accounts;

@Repository
public interface AccountsRepository extends MongoRepository<Accounts, Integer>{
}
