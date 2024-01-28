package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "EVENT_TYPE_CATEGORY")
public class EventTypeCategoryEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Deprecated
    @Column(name = "i18n_event_type_category_name")
    private Integer i18NEventTypeCategoryName;
    @Basic
    @Column(name = "parent")
    private Integer parent;
    @Basic
    @Column(name = "COLOR")
    private String color;
    @Column(name = "i18n_code")
    private String code;
}
