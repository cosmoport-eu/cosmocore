package com.cosmoport.cosmocore.model;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.Getter;  
import lombok.NoArgsConstructor;  
import lombok.Setter;


@NoArgsConstructor
@Entity
@Table(name = "TIMETABLE__MATERIAL")
public class MaterialQtyEntity {  
    @EmbeddedId
    MaterialQtyKey id;

    // @ManyToOne
    // @JoinColumn(name = "timetable_id")
    // TimetableEntity timetable;

    // @ManyToOne
    // @JoinColumn(name = "material_id")
    // MaterialEntity material;

    String qty;
    
    // standard constructors, getters, and setters
}