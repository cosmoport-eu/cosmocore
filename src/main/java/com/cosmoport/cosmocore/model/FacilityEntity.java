package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "FACILITY")
public class FacilityEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "i18n_code")
    private String code = UUID.randomUUID().toString();

    @Column(name = "is_disabled")
    private boolean isDisabled;
}
