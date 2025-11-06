package com.myStore.myStore.repository;

import com.myStore.myStore.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, Integer> {
    Invoice findTopByOrderByInvoiceIdDesc();

    List<Invoice> findByPartyName(String partyName);

    void deleteByPartyName(String name);

    @Query(value = "{ $expr: { $and: [ { $gte: [ { $dateFromString: { dateString: '$date', format: '%d-%m-%Y' } }, { $dateFromString: { dateString: ?0, format: '%d-%m-%Y' } } ] }, { $lte: [ { $dateFromString: { dateString: '$date', format: '%d-%m-%Y' } }, { $dateFromString: { dateString: ?1, format: '%d-%m-%Y' } } ] } ] } }")
    List<Invoice> findByDateBetween(String fromDate, String toDate);

}
