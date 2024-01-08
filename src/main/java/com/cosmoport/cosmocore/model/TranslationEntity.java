package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "TRANSLATION")
public class TranslationEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "i18n_id")
    private int i18NId;
    @Basic
    @Column(name = "locale_id")
    private int localeId;
    @Basic
    @Column(name = "tr_text")
    private String trText;
}
