package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "EVENT_TYPE")
public class EventTypeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "category_id")
    private Integer categoryId;
    @Basic
    @Column(name = "i18n_event_type_name")
    private int i18NEventTypeName;
    @Basic
    @Column(name = "i18n_event_type_description")
    private Integer i18NEventTypeDescription;
    @Basic
    @Column(name = "default_duration")
    private int defaultDuration;
    @Basic
    @Column(name = "default_repeat_interval")
    private int defaultRepeatInterval;
    @Basic
    @Column(name = "default_cost")
    private double defaultCost;
}
