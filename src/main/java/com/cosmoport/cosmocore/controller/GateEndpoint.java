package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.repository.GateRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gates")
public class GateEndpoint {

    private final GateRepository gateRepository;

    public GateEndpoint(GateRepository gateRepository) {
        this.gateRepository = gateRepository;
    }

    @GetMapping
    public List<GateDto> getAll() {
        return gateRepository.findAll().stream()
                .map(gateEntity -> new GateDto(gateEntity.getId(), gateEntity.getNumber(), gateEntity.getGateName()))
                .toList();
    }


    public record GateDto(long id, int number, String gateName) {
    }
}
