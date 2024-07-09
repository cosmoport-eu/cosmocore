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
public class MaterialQtyKey implements Serializable {

    @Column(name = "timetable_id")
    Long timetableId;

    @Column(name = "material_id")
    Long materialId;

    // standard constructors, getters, and setters
    // hashcode and equals implementation
}