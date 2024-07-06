package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.TimetableMaterialEntity;
import com.cosmoport.cosmocore.model.TimetableMaterialId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableMaterialRepository extends JpaRepository<TimetableMaterialEntity, TimetableMaterialId> {  
}
