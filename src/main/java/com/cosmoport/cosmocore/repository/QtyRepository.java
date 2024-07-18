package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.QtyEntity;
import com.cosmoport.cosmocore.model.QtyId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QtyRepository extends JpaRepository<QtyEntity, QtyId> {
    @Query(value = """
                SELECT *
                FROM TIMETABLE__MATERIAL
            """, nativeQuery = true)
    List<QtyEntity> findAll();

    @Query(value = """
                SELECT *
                FROM TIMETABLE__MATERIAL
                WHERE timetable_id = :timetableId
                ORDER BY material_id
            """, nativeQuery = true)
    List<QtyEntity> getTimetableMaterials(int timetableId);
    
    @Modifying
    @Query(value = """
        UPDATE TIMETABLE__MATERIAL 
        SET qty = :qty 
        WHERE timetable_id = :timetableId AND material_id = :materialId
    """, nativeQuery = true)
    int setQtyById(String qty, int timetableId, int materialId);
}
