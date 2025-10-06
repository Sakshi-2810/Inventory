package com.myStore.myStore.service;

import com.myStore.myStore.exception.CustomDataException;
import com.myStore.myStore.model.Response;
import com.myStore.myStore.model.StockBill;
import com.myStore.myStore.model.StockDetail;
import com.myStore.myStore.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StockDetailService {

    @Autowired
    private StockDetailRepository stockDetailRepository;

    public Integer generateNewStockDetailId() {
        Integer maxId = Optional.ofNullable(stockDetailRepository.findTopByOrderByItemIdDesc()).map(StockDetail::getItemId).orElse(0);
        return maxId + 1;
    }

    public Response saveStockDetail(StockDetail stockDetail) {
        stockDetail.setItemId(generateNewStockDetailId());
        stockDetailRepository.save(stockDetail);
        log.info("Saved new stock detail: {}", stockDetail);
        return new Response(stockDetail.getItemId(), "Stock detail saved successfully");
    }

    public Response updateStockDetail(StockDetail stockDetail) {
        if (stockDetail.getItemId() == null || !stockDetailRepository.existsById(stockDetail.getItemId())) {
            throw new CustomDataException("Stock detail does not exist");
        }
        stockDetailRepository.save(stockDetail);
        log.info("Updated stock detail: {}", stockDetail);
        return new Response(stockDetail.getItemId(), "Stock detail updated successfully");
    }

    public Response getAllStockDetails() {
        log.info("Fetching all stock details");
        return new Response(stockDetailRepository.findAll(), "All stock details fetched successfully");
    }

    public Response deleteStockDetail(Integer itemId) {
        if (!stockDetailRepository.existsById(itemId)) {
            throw new CustomDataException("Stock detail does not exist");
        }
        stockDetailRepository.deleteById(itemId);
        log.info("Deleted stock detail with itemId: {}", itemId);
        return new Response(itemId, "Stock detail deleted successfully");
    }

    public void saveStocks(List<StockBill> stockBills) {
        List<String> existingStocks = stockDetailRepository.findItemNameByItemNameIn(stockBills.stream().map(StockBill::getItemName).toList()).stream().map(StockDetail::getItemName).toList();
        List<StockDetail> newStocks = stockBills.stream().filter(stockBill -> !existingStocks.contains(stockBill.getItemName())).map(stockBill -> {
            StockDetail stockDetail = new StockDetail();
            stockDetail.setItemId(generateNewStockDetailId());
            stockDetail.setItemName(stockBill.getItemName());
            stockDetail.setPrice(stockBill.getPrice());
            stockDetail.setGst(stockBill.getGst());
            stockDetail.setUnit(stockBill.getUnit());
            return stockDetail;
        }).toList();
        if (newStocks.isEmpty()) {
            return;
        }
        log.info("Saving new stocks: {}", newStocks);
        stockDetailRepository.saveAll(newStocks);
    }
}
