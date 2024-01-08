package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "I18N")
public class I18NEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "tag")
    private String tag;
    @Basic
    @Column(name = "external")
    private boolean external;
    @Basic
    @Column(name = "description")
    private String description;
    @Basic
    @Column(name = "params")
    private String params;
}
