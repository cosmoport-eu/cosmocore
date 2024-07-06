package com.cosmoport.cosmocore.model;

import lombok.AllArgsConstructor;  
import lombok.Data;  
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
@Embeddable
public class TimetableMaterialId implements Serializable {  
    @Column(name = "material_id")
    private Integer materialId;

    @Column(name = "timetable_id")
    private Integer timetableId;
}
