package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.MaterialQtyEntity;
import com.cosmoport.cosmocore.model.MaterialQtyKey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MaterialQtyRepository extends JpaRepository<MaterialQtyEntity, MaterialQtyKey> {
    @Query(value = """
                SELECT *
                FROM TIMETABLE__MATERIAL
            """, nativeQuery = true)
    List<MaterialQtyEntity> findAll();

    @Query(value = """
                SELECT *
                FROM TIMETABLE__MATERIAL
                WHERE timetable_id = :timetableId
                ORDER BY material_id
            """, nativeQuery = true)
    List<MaterialQtyEntity> getTimetableMaterials(int timetableId);
}
