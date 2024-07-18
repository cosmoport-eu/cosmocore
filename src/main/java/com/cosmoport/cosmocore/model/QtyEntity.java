package com.cosmoport.cosmocore.model;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;  
import lombok.NoArgsConstructor;  
import lombok.Setter;


// @NoArgsConstructor
// @Entity
// @Table(name = "TIMETABLE__MATERIAL")
// public class QtyEntity {  
//     @EmbeddedId
//     QtyKey id;

//     // @ManyToOne
//     // @JoinColumn(name = "timetable_id")
//     // TimetableEntity timetable;

//     // @ManyToOne
//     // @JoinColumn(name = "material_id")
//     // MaterialEntity material;

//     String qty;
    
//     // standard constructors, getters, and setters
// }

@Entity
@Setter
@Getter
@Table(name = "TIMETABLE__MATERIAL")
@IdClass(QtyId.class)
public class QtyEntity {

    public int id;
    public int timetable_id;
    public int material_id;
    public String qty;

    @Id
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", referencedColumnName = "id")
    public TimetableEntity timetable;

    @Id
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "material_id", referencedColumnName = "id")
    public MaterialEntity material;
}