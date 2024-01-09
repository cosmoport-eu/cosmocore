package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Setter
@Getter
@Entity
@Table(name = "TRANSLATIONS")
public class TranslationsEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "locale_id")
    private int localeId;
    @Basic
    @Column(name = "code")
    private String code;
    @Basic
    @Column(name = "text")
    private String text;
}
