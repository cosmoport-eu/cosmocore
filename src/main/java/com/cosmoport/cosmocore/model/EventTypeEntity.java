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
    @Column(name = "default_duration")
    private int defaultDuration;
    @Basic
    @Column(name = "default_repeat_interval")
    private int defaultRepeatInterval;
    @Basic
    @Column(name = "default_cost")
    private double defaultCost;
    @Column(name = "i18n_name_code")
    private String nameCode;
    @Column(name = "i18n_desc_code")
    private String descCode;
    @Column(name = "parent_id")
    private Integer parentId;
}
