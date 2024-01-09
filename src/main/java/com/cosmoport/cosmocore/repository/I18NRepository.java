package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.I18NEntity;
import org.springframework.data.jpa.repository.JpaRepository;

@Deprecated
public interface I18NRepository extends JpaRepository<I18NEntity, Integer> {
}
