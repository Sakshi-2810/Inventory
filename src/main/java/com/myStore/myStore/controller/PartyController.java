package com.myStore.myStore.controller;

import com.myStore.myStore.model.Party;
import com.myStore.myStore.model.Response;
import com.myStore.myStore.service.PartyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class PartyController {
    @Autowired
    private PartyService partyService;

    @GetMapping(produces = "application/json", value = "/parties" )
    public ResponseEntity<Response> getAllParties() {
        return ResponseEntity.ok(partyService.getAllParties());
    }

    @PostMapping(produces = "application/json", value = "/party/save" )
    public ResponseEntity<Response> saveParty(@RequestBody @Valid Party party) {
        return ResponseEntity.ok(partyService.saveParty(party));
    }

    @GetMapping(produces = "application/json", value = "/party" )
    public ResponseEntity<Response> getPartyById(@RequestParam String name) {
        return ResponseEntity.ok(new Response(partyService.getPartyById(name), "Party fetched successfully"));
    }

    @DeleteMapping(produces = "application/json", value = "/party/delete" )
    public ResponseEntity<Response> deleteParty(@RequestParam Integer partyId, @RequestParam Integer dueAmount) {
        return ResponseEntity.ok(partyService.deleteParty(partyId, dueAmount));
    }
}
