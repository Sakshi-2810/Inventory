package com.myStore.myStore.controller;

import com.myStore.myStore.model.Response;
import com.myStore.myStore.model.StockDetail;
import com.myStore.myStore.service.StockDetailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class StockDetailController {
    @Autowired
    private StockDetailService stockDetailService;

    @GetMapping(produces = "application/json", value = "/stock/summary" )
    public ResponseEntity<Response> getAllStocks() {
        return ResponseEntity.ok(stockDetailService.getAllStockDetails());
    }

    @PostMapping(produces = "application/json", value = "/stock/add" )
    public ResponseEntity<Response> saveStock(@RequestBody @Valid StockDetail stockDetail) {
        return ResponseEntity.ok(stockDetailService.saveStockDetail(stockDetail));
    }

    @PutMapping(produces = "application/json", value = "/stock/update" )
    public ResponseEntity<Response> updateStock(@RequestBody @Valid StockDetail stockDetail) {
        return ResponseEntity.ok(stockDetailService.updateStockDetail(stockDetail));
    }

    @DeleteMapping(produces = "application/json", value = "/stock/delete" )
    public ResponseEntity<Response> deleteStock(@RequestParam Integer stockId) {
        return ResponseEntity.ok(stockDetailService.deleteStockDetail(stockId));
    }
}
