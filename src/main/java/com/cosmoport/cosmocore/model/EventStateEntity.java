package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "EVENT_STATE")
public class EventStateEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Deprecated
    @Column(name = "i18n_state")
    private Integer i18NState;
    @Column(name = "i18n_code")
    private String code;
}
