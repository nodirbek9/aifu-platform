package uz.aifu.lms.scheduling.app;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.iam.app.user.StudentDirectory;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupStudentView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.HolidayRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.HolidayView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomView;
import uz.aifu.lms.scheduling.domain.Holiday;
import uz.aifu.lms.scheduling.domain.PairSlot;
import uz.aifu.lms.scheduling.domain.Room;
import uz.aifu.lms.scheduling.domain.StudentGroup;
import uz.aifu.lms.scheduling.persistence.HolidayRepository;
import uz.aifu.lms.scheduling.persistence.PairSlotRepository;
import uz.aifu.lms.scheduling.persistence.RoomRepository;
import uz.aifu.lms.scheduling.persistence.StudentGroupRepository;
import uz.aifu.lms.scheduling.persistence.TimetableEntryRepository;

/**
 * The reference data a timetable is built from: student groups and their rosters, rooms, the bell
 * schedule (pair slots) and holidays.
 */
@Service
public class SchedulingReferenceService {

    private final StudentGroupRepository groups;
    private final RoomRepository rooms;
    private final PairSlotRepository pairSlots;
    private final HolidayRepository holidays;
    private final TimetableEntryRepository entries;
    private final StudentDirectory students;
    private final AuditService audit;

    public SchedulingReferenceService(
            StudentGroupRepository groups,
            RoomRepository rooms,
            PairSlotRepository pairSlots,
            HolidayRepository holidays,
            TimetableEntryRepository entries,
            StudentDirectory students,
            AuditService audit) {
        this.groups = groups;
        this.rooms = rooms;
        this.pairSlots = pairSlots;
        this.holidays = holidays;
        this.entries = entries;
        this.students = students;
        this.audit = audit;
    }

    // ------------------------------------------------------------------ groups

    @Transactional(readOnly = true)
    public List<GroupView> listGroups() {
        return toGroupViews(groups.findAllByOrderByCode());
    }

    @Transactional(readOnly = true)
    public GroupView getGroup(UUID id) {
        return toGroupViews(List.of(requireGroup(id))).get(0);
    }

    @Transactional(readOnly = true)
    public StudentGroup requireGroup(UUID id) {
        return groups.findById(id).orElseThrow(() -> Exceptions.notFound("StudentGroup", id));
    }

    /** The group a student belongs to, if they have one. */
    @Transactional(readOnly = true)
    public Optional<UUID> groupIdOfStudent(UUID userId) {
        return students.groupCodeOf(userId).flatMap(groups::findByCode).map(StudentGroup::getId);
    }

    @Transactional
    public GroupView createGroup(GroupRequest request) {
        String code = request.code().trim();
        if (groups.existsByCodeIgnoreCase(code)) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This group code already exists");
        }
        StudentGroup group = new StudentGroup(code);
        apply(group, request);
        return toGroupViews(List.of(groups.save(group))).get(0);
    }

    @Transactional
    public GroupView updateGroup(UUID id, GroupRequest request) {
        StudentGroup group = requireGroup(id);
        String code = request.code().trim();
        if (!group.getCode().equalsIgnoreCase(code) && groups.existsByCodeIgnoreCase(code)) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This group code already exists");
        }
        // Renaming is safe: student_profiles.group_code follows it (ON UPDATE CASCADE).
        apply(group, request);
        groups.flush();
        return toGroupViews(List.of(group)).get(0);
    }

    @Transactional
    public GroupView setGroupArchived(UUID id, boolean archived) {
        StudentGroup group = requireGroup(id);
        if (archived) {
            group.archive();
        } else {
            group.activate();
        }
        return toGroupViews(List.of(group)).get(0);
    }

    /** Only an empty group that no class uses can be deleted; otherwise archive it. */
    @Transactional
    public void deleteGroup(UUID id) {
        StudentGroup group = requireGroup(id);
        if (!students.studentsOfGroup(group.getCode()).isEmpty() || entries.existsForGroup(id)) {
            throw Exceptions.conflict(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "The group has students or timetable classes; archive it instead");
        }
        groups.delete(group);
    }

    @Transactional(readOnly = true)
    public List<GroupStudentView> listStudents(UUID groupId) {
        return students.studentsOfGroup(requireGroup(groupId).getCode()).stream()
                .map(
                        s ->
                                new GroupStudentView(
                                        s.userId(), s.fullName(), s.username(), s.studentNumber()))
                .toList();
    }

    @Transactional
    public List<GroupStudentView> addStudents(
            UUID groupId, Collection<UUID> userIds, UUID actorId, String ip) {
        StudentGroup group = requireGroup(groupId);
        int changed = students.assignGroup(userIds, group.getCode());
        audit.record(
                AuditAction.GROUP_CHANGED,
                actorId,
                "StudentGroup",
                groupId,
                ip,
                null,
                Map.of("added", changed));
        return listStudents(groupId);
    }

    @Transactional
    public void removeStudent(UUID groupId, UUID userId, UUID actorId, String ip) {
        StudentGroup group = requireGroup(groupId);
        if (!Objects.equals(students.groupCodeOf(userId).orElse(null), group.getCode())) {
            throw Exceptions.notFound("GroupStudent", userId);
        }
        students.assignGroup(List.of(userId), null);
        audit.record(
                AuditAction.GROUP_CHANGED,
                actorId,
                "StudentGroup",
                groupId,
                ip,
                null,
                Map.of("removed", 1));
    }

    private void apply(StudentGroup group, GroupRequest request) {
        if (request.curatorUserId() != null && !students.userExists(request.curatorUserId())) {
            throw Exceptions.notFound("User", request.curatorUserId());
        }
        group.update(
                request.code(),
                request.programmeId(),
                request.courseYear(),
                request.admissionYear(),
                request.studyForm(),
                request.instructionLanguage(),
                request.plannedSize(),
                request.curatorUserId());
    }

    private List<GroupView> toGroupViews(List<StudentGroup> found) {
        Map<String, Long> sizes =
                students.groupSizes(found.stream().map(StudentGroup::getCode).toList());
        Map<UUID, String> curators =
                students.fullNames(
                        found.stream()
                                .map(StudentGroup::getCuratorUserId)
                                .filter(Objects::nonNull)
                                .toList());
        return found.stream()
                .map(
                        g ->
                                new GroupView(
                                        g.getId(),
                                        g.getCode(),
                                        g.getProgrammeId(),
                                        g.getCourseYear(),
                                        g.getAdmissionYear(),
                                        g.getStudyForm(),
                                        g.getInstructionLanguage(),
                                        g.getPlannedSize(),
                                        sizes.getOrDefault(g.getCode(), 0L),
                                        g.getCuratorUserId(),
                                        g.getCuratorUserId() == null
                                                ? null
                                                : curators.get(g.getCuratorUserId()),
                                        g.getStatus()))
                .toList();
    }

    // ------------------------------------------------------------------ rooms

    @Transactional(readOnly = true)
    public List<RoomView> listRooms() {
        return rooms.findAllByOrderByBuildingAscNumberAsc().stream()
                .map(SchedulingReferenceService::toRoomView)
                .toList();
    }

    @Transactional(readOnly = true)
    public Room requireRoom(UUID id) {
        return rooms.findById(id).orElseThrow(() -> Exceptions.notFound("Room", id));
    }

    @Transactional
    public RoomView createRoom(RoomRequest request) {
        String building = request.building() == null ? "" : request.building().trim();
        if (rooms.existsByBuildingAndNumber(building, request.number().trim())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This room already exists");
        }
        Room room =
                new Room(
                        building,
                        request.number(),
                        request.name(),
                        request.capacity(),
                        request.roomType());
        return toRoomView(rooms.save(room));
    }

    @Transactional
    public RoomView updateRoom(UUID id, RoomRequest request) {
        Room room = requireRoom(id);
        String building = request.building() == null ? "" : request.building().trim();
        boolean sameKey =
                room.getBuilding().equals(building)
                        && room.getNumber().equals(request.number().trim());
        if (!sameKey && rooms.existsByBuildingAndNumber(building, request.number().trim())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This room already exists");
        }
        room.update(
                building,
                request.number(),
                request.name(),
                request.capacity(),
                request.roomType(),
                request.active() == null || request.active());
        return toRoomView(room);
    }

    @Transactional
    public void deleteRoom(UUID id) {
        Room room = requireRoom(id);
        if (entries.existsByRoom_Id(id)) {
            throw Exceptions.conflict(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "Classes are scheduled in this room; mark it inactive instead");
        }
        rooms.delete(room);
    }

    static RoomView toRoomView(Room room) {
        return new RoomView(
                room.getId(),
                room.getBuilding(),
                room.getNumber(),
                room.getName(),
                room.label(),
                room.getCapacity(),
                room.getRoomType(),
                room.isActive());
    }

    // ------------------------------------------------------------------ bell schedule

    @Transactional(readOnly = true)
    public List<PairSlotView> listPairSlots() {
        return pairSlots.findAllByOrderByPairNo().stream()
                .map(SchedulingReferenceService::toSlotView)
                .toList();
    }

    @Transactional(readOnly = true)
    public PairSlot requirePairSlot(UUID id) {
        return pairSlots.findById(id).orElseThrow(() -> Exceptions.notFound("PairSlot", id));
    }

    @Transactional
    public PairSlotView createPairSlot(PairSlotRequest request) {
        if (pairSlots.existsByPairNo(request.pairNo())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This pair number already exists");
        }
        return toSlotView(
                pairSlots.save(
                        new PairSlot(request.pairNo(), request.startsAt(), request.endsAt())));
    }

    @Transactional
    public PairSlotView updatePairSlot(UUID id, PairSlotRequest request) {
        PairSlot slot = requirePairSlot(id);
        if (slot.getPairNo() != request.pairNo() && pairSlots.existsByPairNo(request.pairNo())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This pair number already exists");
        }
        slot.update(request.pairNo(), request.startsAt(), request.endsAt());
        return toSlotView(slot);
    }

    @Transactional
    public void deletePairSlot(UUID id) {
        PairSlot slot = requirePairSlot(id);
        if (entries.existsByPairSlot_Id(id)) {
            throw Exceptions.conflict(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "Classes are scheduled in this pair; move them first");
        }
        pairSlots.delete(slot);
    }

    static PairSlotView toSlotView(PairSlot slot) {
        return new PairSlotView(
                slot.getId(), slot.getPairNo(), slot.getStartsAt(), slot.getEndsAt());
    }

    // ------------------------------------------------------------------ holidays

    @Transactional(readOnly = true)
    public List<HolidayView> listHolidays() {
        return holidays.findAllByOrderByOnDate().stream()
                .map(h -> new HolidayView(h.getId(), h.getOnDate(), h.getName()))
                .toList();
    }

    @Transactional
    public HolidayView createHoliday(HolidayRequest request) {
        if (holidays.existsByOnDate(request.date())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This date is already a holiday");
        }
        Holiday saved = holidays.save(new Holiday(request.date(), request.name()));
        return new HolidayView(saved.getId(), saved.getOnDate(), saved.getName());
    }

    @Transactional
    public void deleteHoliday(UUID id) {
        Holiday holiday =
                holidays.findById(id).orElseThrow(() -> Exceptions.notFound("Holiday", id));
        holidays.delete(holiday);
    }
}
