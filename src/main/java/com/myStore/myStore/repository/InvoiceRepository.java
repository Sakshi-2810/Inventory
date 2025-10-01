package com.myStore.myStore.repository;

import com.myStore.myStore.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, Integer> {
    Invoice findTopByOrderByInvoiceIdDesc();

    List<Invoice> findByPartyName(String partyName);

    void deleteByPartyName(String name);
}
