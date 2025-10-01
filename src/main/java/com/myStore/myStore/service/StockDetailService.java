package com.myStore.myStore.service;

import com.myStore.myStore.exception.CustomDataException;
import com.myStore.myStore.model.Response;
import com.myStore.myStore.model.StockDetail;
import com.myStore.myStore.repository.StockDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        return new Response(stockDetail.getItemId(), "Stock detail saved successfully");
    }

    public Response updateStockDetail(StockDetail stockDetail) {
        if (stockDetail.getItemId() == null || !stockDetailRepository.existsById(stockDetail.getItemId())) {
            throw new CustomDataException("Stock detail does not exist");
        }
        stockDetailRepository.save(stockDetail);
        return new Response(stockDetail.getItemId(), "Stock detail updated successfully");
    }

    public Response getAllStockDetails() {
        return new Response(stockDetailRepository.findAll(), "All stock details fetched successfully");
    }

    public Response deleteStockDetail(Integer itemId) {
        if (!stockDetailRepository.existsById(itemId)) {
            throw new CustomDataException("Stock detail does not exist");
        }
        stockDetailRepository.deleteById(itemId);
        return new Response(itemId, "Stock detail deleted successfully");
    }
}
