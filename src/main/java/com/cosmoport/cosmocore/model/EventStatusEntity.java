package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "EVENT_STATUS")
public class EventStatusEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Deprecated
    @Column(name = "i18n_status")
    private Integer i18NStatus;
    @Column(name = "i18n_code")
    private String code;
}
