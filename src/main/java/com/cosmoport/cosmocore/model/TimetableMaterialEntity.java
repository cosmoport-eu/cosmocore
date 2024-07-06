package com.cosmoport.cosmocore.model;
import jakarta.persistence.*;
import lombok.Getter;  
import lombok.NoArgsConstructor;  
import lombok.Setter;


@Setter
@Getter
@Entity
@Table(name = "TIMETABLE__MATERIAL")
public class TimetableMaterialEntity {  
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EmbeddedId
    private TimetableMaterialId id;

    @ManyToOne
    @MapsId("materialId")
    @JoinColumn(name = "material_id")
    private MaterialEntity material;

    @ManyToOne
    @MapsId("timetableId")
    @JoinColumn(name = "timetable_id")
    private TimetableEntity timetable;

    @Column(name = "qty")
    private String qty;

    public TimetableMaterialEntity(MaterialEntity material, TimetableEntity timetable, String qty) {
        this.id = new TimetableMaterialId(material.getId(), timetable.getId());
        this.material = material;
        this.timetable = timetable;
        this.qty = qty;
    }
}