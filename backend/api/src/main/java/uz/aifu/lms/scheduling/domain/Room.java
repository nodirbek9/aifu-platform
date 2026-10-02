package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import uz.aifu.lms.common.domain.VersionedEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/** A teaching room ("auditoriya"). */
@Entity
@Table(name = "rooms")
public class Room extends VersionedEntity {

    @Column(name = "building", nullable = false)
    private String building = "";

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "name")
    private String name;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType = RoomType.CLASSROOM;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Room() {}

    public Room(String building, String number, String name, int capacity, RoomType roomType) {
        update(building, number, name, capacity, roomType, true);
    }

    public final void update(
            String building,
            String number,
            String name,
            int capacity,
            RoomType roomType,
            boolean active) {
        if (capacity <= 0) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "capacity must be positive");
        }
        this.building = building == null ? "" : building.trim();
        this.number = Guards.requiredText(number, "number").trim();
        this.name = (name == null || name.isBlank()) ? null : name.trim();
        this.capacity = capacity;
        this.roomType = Guards.required(roomType, "roomType");
        this.active = active;
    }

    /** "A-204" style label used in timetable cells. */
    public String label() {
        return building.isEmpty() ? number : building + "-" + number;
    }

    public String getBuilding() {
        return building;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public boolean isActive() {
        return active;
    }
}
