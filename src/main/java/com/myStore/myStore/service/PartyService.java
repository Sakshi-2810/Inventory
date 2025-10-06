package com.myStore.myStore.service;

import com.myStore.myStore.dto.InvoiceDto;
import com.myStore.myStore.dto.PartyDto;
import com.myStore.myStore.exception.CustomDataException;
import com.myStore.myStore.model.Party;
import com.myStore.myStore.model.Response;
import com.myStore.myStore.repository.InvoiceRepository;
import com.myStore.myStore.repository.PartyRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
public class PartyService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PartyRepository partyRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;

    public Response getAllParties() {
        List<Party> parties = partyRepository.findAll();
        Map<String, List<InvoiceDto>> invoices = invoiceRepository.findAll().stream().map(invoice -> modelMapper.map(invoice, InvoiceDto.class)).collect(groupingBy(InvoiceDto::getPartyName));

        List<PartyDto> partyDtos = parties.stream().map(party -> {
            PartyDto partyDto = modelMapper.map(party, PartyDto.class);
            Integer totalDue = invoices.getOrDefault(party.getName(), new ArrayList<>()).stream().map(invoice -> {
                if (invoice.getTransactionType().equalsIgnoreCase("BUY")) {
                    return -invoice.getDueAmount();
                } else {
                    return invoice.getDueAmount();
                }
            }).reduce(0, Integer::sum);
            partyDto.setTotalDue(totalDue - party.getAmountAdded());
            partyDto.setInvoices(invoices.getOrDefault(party.getName(), new ArrayList<>()));
            return partyDto;
        }).toList();
        log.info("Fetched {} parties", partyDtos.size());
        return new Response(partyDtos, "All parties fetched successfully");
    }

    public Integer generateNewPartyId() {
        Integer maxId = Optional.ofNullable(partyRepository.findTopByOrderByPartyIdDesc()).map(Party::getPartyId).orElse(0);
        return maxId + 1;
    }

    public Response saveParty(Party party) {
        Party existingParty = partyRepository.findByName(party.getName());
        if (existingParty == null) {
            party.setPartyId(generateNewPartyId());
        } else {
            party.setPartyId(existingParty.getPartyId());
            party.setAmountAdded(existingParty.getAmountAdded() + party.getAmountAdded());
        }
        partyRepository.save(party);
        log.info("Party saved with ID: {}", party.getPartyId());
        return new Response(party.getPartyId(), "Party saved successfully");
    }

    public Response deleteParty(Integer partyId, Integer dueAmount) {
        if (dueAmount != 0) {
            throw new CustomDataException("Party cannot be deleted as it has due amount");
        }
        if (!partyRepository.existsById(partyId)) {
            throw new CustomDataException("Party does not exist");
        }
        invoiceRepository.deleteByPartyName(partyRepository.findById(partyId).get().getName());
        partyRepository.deleteById(partyId);
        log.info("Party deleted with ID: {}", partyId);
        return new Response(partyId, "Party deleted successfully");
    }

    public Response getPartyById(String name) {
        Party party = partyRepository.findByName(name);
        if (party == null) {
            throw new CustomDataException("Party does not exist");
        }
        PartyDto partyDto = modelMapper.map(party, PartyDto.class);
        Integer totalDue = invoiceRepository.findByPartyName(name).stream().map(invoice -> {
            if (invoice.getTransactionType().equalsIgnoreCase("SELL")) {
                return invoice.getDueAmount();
            } else {
                return -invoice.getDueAmount();
            }
        }).reduce(0, Integer::sum);

        partyDto.setTotalDue(totalDue);
        log.info("Fetched party with name: {}", name);
        return new Response(partyDto, "Party fetched successfully");
    }
}
