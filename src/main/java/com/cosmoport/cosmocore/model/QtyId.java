package com.cosmoport.cosmocore.model;

import lombok.AllArgsConstructor;  
import lombok.Data;  
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

// @Data 
// @AllArgsConstructor 
// @NoArgsConstructor
// @Embeddable
// public class QtyId implements Serializable {

//     @Column(name = "timetable_id")
//     Long timetableId;

//     @Column(name = "material_id")
//     Long materialId;

//     // standard constructors, getters, and setters
//     // hashcode and equals implementation
// }
@Embeddable
public class QtyId implements Serializable {

    public int timetable;
    public int material;

    // getters/setters and most importantly equals() and hashCode()

    public int getMaterial() {
        return material;
    }

    public void setMaterial(int material) {
        this.material = material;
    }

    public int getTimetable() {
        return timetable;
    }

    public void setTimetable(int timetable) {
        this.timetable = timetable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QtyId)) return false;
        QtyId qtyId = (QtyId) o;
        return getMaterial() == qtyId.getMaterial() &&
                getTimetable() == qtyId.getTimetable();
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(getMaterial(), getTimetable());
    }
}