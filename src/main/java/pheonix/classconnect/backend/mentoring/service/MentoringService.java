package pheonix.classconnect.backend.mentoring.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentDomainType;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.course.entity.CourseMemberEntity;
import pheonix.classconnect.backend.course.repository.CourseEntityRepository;
import pheonix.classconnect.backend.course.repository.CourseMemberEntityRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.mentoring.contants.MentoringSite;
import pheonix.classconnect.backend.mentoring.contants.MentoringStatus;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;
import pheonix.classconnect.backend.mentoring.entity.MentoringResultEntity;
import pheonix.classconnect.backend.mentoring.entity.ScheduleEntity;
import pheonix.classconnect.backend.mentoring.model.MentoringRequestDTO;
import pheonix.classconnect.backend.mentoring.model.MentoringResultDTO;
import pheonix.classconnect.backend.mentoring.model.ScheduleDTO;
import pheonix.classconnect.backend.mentoring.repository.MentoringRequestRepository;
import pheonix.classconnect.backend.mentoring.repository.MentoringResultRepository;
import pheonix.classconnect.backend.mentoring.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringService {
    private final UserRepository userRepository;
    private final CourseEntityRepository courseEntityRepository;
    private final CourseMemberEntityRepository courseMemberEntityRepository;
    private final FileStorage fileStorage;
    private final MentoringRequestRepository mentoringRequestRepository;
    private final MentoringResultRepository mentoringResultRepository;
    private final ScheduleRepository scheduleRepository;

    // 조회 구분
    private static final int 멘토ID기준월별조회 = 1;
    private static final int 신청자ID기준월별조회 = 2;
    private static final int 멘티ID기준월별조회 = 3;

    @Transactional
    public void createRequest(MentoringRequestDTO.Create dto) {
        log.info("멘토링 요청 생성");


        // 요청 검증
        if (dto.getSite() != MentoringSite.ONLINE && dto.getSite() != MentoringSite.OFFLINE) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, String.format("지원하지 않는 멘토링 방식 구분입니다. [%d]", dto.getSite()));
        }

        UserEntity mentor = userRepository.findById(dto.getMentorId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("사용자 정보를 찾을 수 없습니다. [%d]", dto.getMentorId())));

        UserEntity requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("사용자 정보를 찾을 수 없습니다. [%d]", dto.getRequesterId())));

        CourseEntity course = courseEntityRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, String.format("코스 정보를 찾을 수 없습니다. [%d]", dto.getCourseId())));

        if (dto.getMentees().isEmpty() || !dto.getMentees().containsKey(requester.getStudentNo())) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "요청자 학번은 필수 값입니다. => 멘티 리스트를 확인하세요.");
        }

        // 멘토와 요청자 모두 코스에 속해있는지 검증
        if (!courseMemberEntityRepository.existsByUserIdAndRole(dto.getMentorId(), CourseRole.MENTOR)) {
            throw new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, String.format("코스에서 해당 멘토를 찾을 수 없습니다. [%d]", dto.getMentorId()));
        }
        if (!courseMemberEntityRepository.existsByUserIdAndRole(dto.getRequesterId(), CourseRole.MENTEE)) {
            throw new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, String.format("코스에서 해당 멘티를 찾을 수 없습니다. [%d]", dto.getRequesterId()));
        }

        // 해당 시간에 예약 가능한지 검증
        List<ScheduleEntity> schedules = scheduleRepository.findAllById_UserIdAndId_DateOrderByStartTime(dto.getMentorId(), dto.getDate());
        if (schedules.isEmpty()) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_ERROR, "해당 시간에 멘토링을 신청할 수 없습니다.");
        }

        // 요청 시간을 15분 단위로 쪼갬
        LocalTime from = dto.getStartTime();
        LocalTime to = from.plusMinutes(15);

        while (to.isBefore(dto.getEndTime()) || to.equals(dto.getEndTime())) {
            LocalTime finalFrom = from;
            LocalTime finalTo = to;
            if (schedules.stream().noneMatch(schedule -> {
                boolean inRange = (finalFrom.equals(schedule.getStartTime()) || finalFrom.isAfter(schedule.getStartTime())) &&
                        (finalTo.equals(schedule.getEndTime()) || finalTo.isBefore(schedule.getEndTime()));
                boolean avail = (schedule.getSite() == MentoringSite.ALL) || (schedule.getSite() == dto.getSite());
                return inRange && avail;
            })) {
                throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_ERROR, "해당 시간에 멘토링을 신청할 수 없습니다.");
            }

            from = from.plusMinutes(15);
            to = to.plusMinutes(15);
        }

        // 겹치는 시간 대에 멘토링이 존재하는지 검증
        boolean conflict = !mentoringRequestRepository.findAllByUserAndDateAndStatusIn(dto.getMentorId(), dto.getDate(), List.of(MentoringStatus.승인대기, MentoringStatus.승인)).stream()
                .allMatch(request -> ((dto.getEndTime().isBefore(request.getStartTime()) || dto.getEndTime().equals(request.getStartTime())) ||
                        (dto.getStartTime().isAfter(request.getEndTime()) || dto.getStartTime().equals(request.getEndTime()))));

        if (conflict) {
            throw new MainApplicationException(ErrorCode.MENTOR_TIME_CONFLICT, "이미 다른 요청이 승인/대기 중입니다.");
        }

        // 요청 저장
        MentoringRequestEntity request = MentoringRequestEntity.builder()
                .mentor(mentor)
                .requester(requester)
                .course(course)
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .site(dto.getSite())
                .content(dto.getContent())
                .mentees(dto.getMentees())
                .status(MentoringStatus.승인대기)
                .registered(false)
                .build();

        Long reqId = mentoringRequestRepository.save(request).getId();

        // 이미지 링크
        if (!dto.getImages().isEmpty()) {
            for (Long fileId : dto.getImages()) {
                fileStorage.mapFileToDomain(fileId, AttachmentDomainType.MENTORING_REQUEST, reqId);
            }
        }

        // 파일 링크
        if (!dto.getFiles().isEmpty()) {
            for (Long fileId : dto.getFiles()) {
                fileStorage.mapFileToDomain(fileId, AttachmentDomainType.MENTORING_REQUEST, reqId);
            }
        }
    }

    @Transactional
    public void acceptRequest(Long requestId, String comment) {
        log.info("멘토링 요청 수락 : [{}]", requestId);

        // 요청 검증
        // 1. 존재하는 요청인지 검증
        MentoringRequestEntity request = this.getMentoringRequestEntityById(requestId);

        // 2. comment 필수값 검증
        if (comment.isEmpty()) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_PARAMETER_NULL, "멘토링 승인 사유는 필수 값입니다.");
        }
        // 3. 멘토링 요청 상태 검증
        if (!Objects.equals(request.getStatus(), MentoringStatus.승인대기)) {
            throw new MainApplicationException(ErrorCode.MENTORING_INVALID_STATUS_CHANGE, "승인 대기 상태인 요청만 승인할 수 있습니다.");
        }

        // 겹치는 시간 대에 멘토링이 존재하는지 검증
        Long mentorId = request.getMentor().getId();
        LocalDate date = request.getDate();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        boolean conflict = !mentoringRequestRepository.findAllByUserAndDateAndStatusIn(mentorId, date, List.of(MentoringStatus.승인)).stream()
                .allMatch(req -> (
                        endTime.isBefore(req.getStartTime()) ||
                        endTime.equals(req.getStartTime()) ||
                        startTime.isAfter(req.getEndTime()) ||
                        startTime.equals(req.getEndTime()))
                );

        if (conflict) {
            throw new MainApplicationException(ErrorCode.MENTOR_TIME_CONFLICT, "이미 다른 요청이 승인/대기 중입니다.");
        }

        request.accept(comment);

        mentoringRequestRepository.save(request);
    }

    // 멘토링 반려
    @Transactional
    public void rejectRequest(Long requestId, String comment) {
        log.info("멘토링 요청 반려 : [{}]", requestId);

        // 요청 검증
        // 1. 존재하는 요청인지 검증
        MentoringRequestEntity request = this.getMentoringRequestEntityById(requestId);;

        // 2. comment 필수값 검증
        if (comment.isEmpty()) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_PARAMETER_NULL, "멘토링 거절 사유는 필수 값입니다.");
        }
        // 3. 멘토링 요청 상태 검증
        if (!Objects.equals(request.getStatus(), MentoringStatus.승인대기)) {
            throw new MainApplicationException(ErrorCode.MENTORING_INVALID_STATUS_CHANGE, "승인 대기 상태인 요청만 거절할 수 있습니다.");
        }

        request.reject(comment);

        mentoringRequestRepository.save(request);
    }



    @Transactional
    // 멘토링 취소
    public void cancelRequest(Long requestId, Long courseId, String comment, Long userId) {
        log.info("멘토링 요청 취소 : [{}]", requestId);

        // 요청 검증
        // 1. 존재하는 요청인지 검증
        MentoringRequestEntity request = mentoringRequestRepository.findById(requestId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.MENTORING_REQUEST_NOT_FOUND, String.format("멘토링 요청 정보를 찾을 수 없습니다. [%d]", requestId)));

        // 2. comment 필수값 검증
        if (comment.isEmpty()) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_PARAMETER_NULL, "멘토링 취소 사유는 필수 값입니다.");
        }
        // 3. 멘토링 요청 상태 검증
        if (!List.of(MentoringStatus.승인대기, MentoringStatus.승인).contains(request.getStatus())) {
            throw new MainApplicationException(ErrorCode.MENTORING_INVALID_STATUS_CHANGE, "승인 대기 또는 승인 상태인 요청만 거절할 수 있습니다.");
        }
        // 4. 멘토링 요청 거절 가능 기간인지 조회
        if (Objects.equals(request.getStatus(), MentoringStatus.승인) &&
                LocalDateTime.now().isAfter(LocalDateTime.of(request.getDate(), request.getStartTime()).minusHours(24))) {
            throw new MainApplicationException(ErrorCode.MENTORING_INVALID_STATUS_CHANGE, "승인된 멘토링은 멘토링 시작 24시간 전까지 취소 가능합니다.");
        }

        CourseMemberEntity member = courseMemberEntityRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, "유저가 코스 멤버가 아닙니다."));

        request.cancel(comment, member.getRole());

        mentoringRequestRepository.saveAndFlush(request);
    }

    // 멘토링 조회
    public List<MentoringRequestDTO.MentoringRequest> getMentoringRequests(Long mentorId, Long requesterId, Long menteeId, Long courseId, int year, int month) {
        // 요청값 검증
        if (mentorId == null && requesterId == null && menteeId == null && courseId == null) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_PARAMETER_NULL, "멘토 ID 또는 요청자 ID 또는 코스 ID 중 최소 하나는 필수 값입니다.");
        }

        log.info("멘토링 요청 조회 : 멘토 = {} 요청자 = {} 코스 = {} {}년 {}월", mentorId, requesterId, courseId, year, month);

        // 조회 기준 SET
        int searchType = 0;

        if (mentorId != null && requesterId == null && menteeId == null && courseId != null && year > 0 && month > 0) {
            searchType = 멘토ID기준월별조회;
        }
        else if (mentorId == null && requesterId != null && menteeId == null && courseId != null && year > 0 && month > 0) {
            searchType = 신청자ID기준월별조회;
        }
        else if (mentorId == null && requesterId == null && menteeId != null && courseId != null && year > 0 && month > 0) {
            searchType = 멘티ID기준월별조회;
        }

        // searchType 별 조회
        List<MentoringRequestEntity> requests;
        switch (searchType) {
            case 멘토ID기준월별조회: {
                log.info("멘토 기준 월별 조회 - 멘토 ID = [{}]", mentorId);
                LocalDate from = LocalDate.of(year, month, 1);
                LocalDate to = LocalDate.of(year, month + 1, 1).minusDays(1);
                requests = mentoringRequestRepository.findAllByMentorIdAndCourseIdAndDateBetween(mentorId, courseId, from, to);
                break;
            }
            case 신청자ID기준월별조회: {
                log.info("신청자 기준 월별 조회 - 신청자 ID = [{}]", requesterId);
                LocalDate from = LocalDate.of(year, month, 1);
                LocalDate to = LocalDate.of(year, month + 1, 1).minusDays(1);
                requests = mentoringRequestRepository.findAllByRequesterIdAndCourseIdAndDateBetween(requesterId, courseId, from, to);
                break;
            }
            case 멘티ID기준월별조회: {
                log.info("멘티 기준 월별 조회 - 멘티 ID = [{}]", menteeId);
                // 멘티가 서비스에 가입되지 않은 경우 UserNotFound 에러 발생
                UserEntity mentee = userRepository.findById(menteeId)
                                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "유저 정보를 찾을 수 없습니다."));
                if (!courseMemberEntityRepository.existsByUserIdAndRole(menteeId, CourseRole.MENTEE)) {
                    throw new MainApplicationException(ErrorCode.MENTEE_NOT_FOUND, "코스의 멘티가 아닙니다.");
                }

                LocalDate from = LocalDate.of(year, month, 1);
                LocalDate to = LocalDate.of(year, month + 1, 1).minusDays(1);

                requests = mentoringRequestRepository.findAllByCourseIdAndDateBetweenAndStatus(courseId, from, to, MentoringStatus.승인).stream()
                        .filter(req -> req.getMentees().containsKey(mentee.getStudentNo()))
                        .toList();
                break;
            }

            default: {
                throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, String.format("지원하지 않는 조회 구분입니다. [%d]", searchType));
            }
        }

        if (requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<MentoringRequestDTO.MentoringRequest> requestList = requests.stream()
                .map(MentoringRequestDTO.MentoringRequest::fromEntity)
                .toList();

        // 이미지 & 파일 추가
        for (MentoringRequestDTO.MentoringRequest request : requestList) {
            List<File> images = new ArrayList<>();
            List<File> files = new ArrayList<>();

            fileStorage.getAttachmentList(AttachmentDomainType.MENTORING_REQUEST, request.getId()).forEach(
                    file -> {
                        if (fileStorage.isImage(file.getType())) {
                            images.add(file);
                        }
                        else if (fileStorage.isScript(file.getType())) {
                            files.add(file);
                        }
                    }
            );
            request.setImages(images);
            request.setFiles(files);
        }

        return requestList;
    }

    public int getMentoringRequestCount(Long mentorId, Long menteeId, Long courseId, int year, int month) {
        log.info("멘토링 횟수 Count : {}년 {}월, {} {} {}", year, month, mentorId, menteeId, courseId);

        int searchType = 0;
        int count = 0;

        // 입력값 검증
        if (mentorId == null && menteeId != null) {
            searchType = 2; /*멘티별 멘토링 횟수 Count*/
        }
        if (courseId == 0) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "코스 ID는 필수 입력값입니다.");
        }
        if (year == 0) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "연도는 필수 입력값입니다.");
        }
        if (month == 0) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "연도는 필수 입력값입니다.");
        }

        // 본처리
        switch (searchType) {
            case 2: {
                // 멘티 검증
                UserEntity mentee = userRepository.findById(menteeId)
                        .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "유저 정보를 찾을 수 없습니다."));
                if (!courseMemberEntityRepository.existsByUserIdAndRole(menteeId, CourseRole.MENTEE)) {
                    throw new MainApplicationException(ErrorCode.MENTEE_NOT_FOUND, "코스의 멘티가 아닙니다.");
                }

                LocalDate from = LocalDate.of(year, month, 1);
                LocalDate to = LocalDate.of(year, month + 1, 1).minusDays(1);

                List<MentoringRequestEntity> requests = mentoringRequestRepository.findAllByCourseIdAndDateBetweenAndStatus(courseId, from, to, MentoringStatus.승인);

                // 멘티별 멘토링 개수 count
                if (!requests.isEmpty()) {
                    for (MentoringRequestEntity request : requests) {
                        if (request.getMentees().containsKey(mentee.getStudentNo()))
                            count++;
                    }
                }
                break;
            }

            default:
                throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "지원하지 않는 조회 구분입니다.");
        }

        return count;
    }


    // 멘토링 상세 조회
    public MentoringRequestDTO.MentoringRequest getMentoringRequest(Long id) {
        log.info("멘토링 요청 상세 조회 : [{}]", id);

        MentoringRequestDTO.MentoringRequest request = MentoringRequestDTO.MentoringRequest.fromEntity(this.getMentoringRequestEntityById(id));


        // 이미지 및 파일 조회
        List<File> images = new ArrayList<>();
        List<File> files = new ArrayList<>();

        fileStorage.getAttachmentList(AttachmentDomainType.MENTORING_REQUEST, request.getId()).forEach(
                file -> {
                    if (fileStorage.isImage(file.getType())) {
                        images.add(file);
                    }
                    else if (fileStorage.isScript(file.getType())) {
                        files.add(file);
                    }
                }
        );
        request.setImages(images);
        request.setFiles(files);

        return request;
    }

    // 멘토링 수정
    @Transactional
    public void updateRequest(Long id, MentoringRequestDTO.Update dto) {
        log.info("멘토링 요청 정보 수정 : [{}]", id);

        MentoringRequestEntity request = this.getMentoringRequestEntityById(id);


        // 멘토링 내용 및 멘티정보 수정
        // 1. 입력값 검증
        if (dto.getContent().isEmpty() || dto.getContent().isBlank()) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "멘토링 요청 내용이 비어있습니다.");
        }
        else if (dto.getContent().length() > 600) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "멘토링 요청 내용은 한글 기준 최대 300자까지 작성 가능 합니다.");
        }
        if (dto.getMentees().isEmpty() || !dto.getMentees().containsKey(request.getRequester().getStudentNo())) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "요청자 학번은 필수 값입니다. => 멘티 리스트를 확인하세요.");
        }
        if (dto.getSite() == null) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "멘토링 방식이 지정되지 않았습니다.");
        }
        else if (
                !Objects.equals(dto.getSite(), MentoringSite.ONLINE) &&
                !Objects.equals(dto.getSite(), MentoringSite.OFFLINE) &&
                !Objects.equals(dto.getSite(), MentoringSite.ALL))
        {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "지원되지 않는 멘토링 방식입니다.");
        }

        // 처리 검증
        // 해당 시간에 멘토링 방식이 제한되어있으면 site를 변경하지 않음
        // ToDO

        // 승인 대기 상태의 요청만 수정 가능
        if (!Objects.equals(request.getStatus(), MentoringStatus.승인대기)) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_FORBIDDEN_REQUEST, "승인대기 상태의 요청만 수정 가능합니다.");
        }

        // 본처리
        // 1. 기본 내용 변경
        request.updateRequest(dto.getContent(), dto.getMentees(), dto.getSite());

        // 2. 이미지/파일 변경
        fileStorage.changeImages(AttachmentDomainType.MENTORING_REQUEST, id, dto.getImages());
        fileStorage.changeFiles(AttachmentDomainType.MENTORING_REQUEST, id, dto.getFiles());

    }


    // 멘토링 가능한 일정을 보여주는 메서드
    public List<ScheduleDTO.Schedule> getAvailableSchedule(Long userId, LocalDate date) {
        log.info("멘토링 가능 일정 조회");

        userRepository.findById(userId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("유저를 찾을 수 없습니다. [%d]", userId)));

        // 유저 아이디와 날짜 기준으로 가능한 스케줄을 우선 모두 조회
        List<ScheduleEntity> schedules = scheduleRepository.findAllById_UserIdAndId_DateOrderByStartTime(userId, date);
        if (schedules.isEmpty()) return new ArrayList<>();

        // TimeSlot 생성
        List<ScheduleDTO.Schedule> timeSlots = generateTimeSlots(schedules);

        // 유저 아이디와 날짜 기준으로 승인대기/승인 상태인 요청 내역을 모두 조회
        List<MentoringRequestEntity> requests = mentoringRequestRepository.findAllByUserAndDateAndStatusIn(userId, date, List.of(MentoringStatus.승인대기, MentoringStatus.승인));

        // 유효한 멘토링 요청이 있는 시간 슬롯 제외
        return timeSlots.stream()
                .filter(slot -> requests.stream().noneMatch(request -> isSlotOverlapping(slot, request)))
                .toList();
    }

    private List<ScheduleDTO.Schedule> generateTimeSlots(List<ScheduleEntity> schedules) {
        List<ScheduleDTO.Schedule> timeSlots = new ArrayList<>();

        for (int i = 0; i < schedules.size(); i++) {
            ScheduleEntity current = schedules.get(i);
            log.info("{} ~ {}", current.getStartTime(), current.getEndTime());

            LocalTime slotStart = current.getStartTime().withMinute(0).withSecond(0).withNano(0);
            if (current.getStartTime().getMinute() % 15 != 0) {
                slotStart = slotStart.plusMinutes(((current.getStartTime().getMinute() / 15) + 1) * 15);
            }
            else {
                slotStart = slotStart.plusMinutes(current.getStartTime().getMinute());
            }
            LocalTime slotEnd = slotStart.plusMinutes(30);

            // 만약 다음 스케줄이 존재하고 두 스케줄이 완전 붙어있으면서 이전 스케줄이 "전체"라면
            if (i + 1 < schedules.size()) {
                ScheduleEntity next = schedules.get(i+1);
                if (current.getEndTime().equals(next.getStartTime()) &&
                    Objects.equals(current.getSite(), MentoringSite.ALL)) {
                    boolean endOfDay = false;
                    // 1. current가 site=1(전체)라면 겹치는 구간의 slot은 next의 site를 따라간다.
                    while (slotEnd.isBefore(next.getEndTime()) || slotEnd.equals(next.getEndTime())) {
                        short site = current.getSite();

                        // 예외 처리
                        if (slotEnd.equals(LocalTime.of(0, 0))) {
                            slotEnd = LocalTime.of(23, 59);
                            endOfDay = true;
                        }

                        if (slotEnd.isAfter(next.getStartTime())) {
                            site = next.getSite();
                        }

                        timeSlots.add(ScheduleDTO.Schedule.builder()
                                .date(current.getId().getDate())
                                .startTime(slotStart)
                                .endTime(slotEnd)
                                .site(site)
                                .build());

                        if (site != current.getSite()) break;
                        slotStart = slotStart.plusMinutes(15);
                        slotEnd = slotStart.plusMinutes(30);

                        if (endOfDay)
                            break;
                    }
                } else {
                    boolean endOfDay = false;
                    while (slotEnd.isBefore(current.getEndTime()) || slotEnd.equals(current.getEndTime())) {
                        short site = current.getSite();

                        // 예외 처리
                        if (slotEnd.equals(LocalTime.of(0, 0))) {
                            slotEnd = LocalTime.of(23, 59);
                            endOfDay = true;
                        }

                        timeSlots.add(ScheduleDTO.Schedule.builder()
                                .date(current.getId().getDate())
                                .startTime(slotStart)
                                .endTime(slotEnd)
                                .site(site)
                                .build());
                        slotStart = slotStart.plusMinutes(15);
                        slotEnd = slotStart.plusMinutes(30);

                        if (endOfDay)
                            break;
                    }
                }


            }
            else {
                boolean endOfDay = false;
                while (slotEnd.isBefore(current.getEndTime()) || slotEnd.equals(current.getEndTime())) {
                    short site = current.getSite();

                    // 예외 처리
                    if (slotEnd.equals(LocalTime.of(0, 0))) {
                        slotEnd = LocalTime.of(23, 59);
                        endOfDay = true;
                    }


                    timeSlots.add(ScheduleDTO.Schedule.builder()
                            .date(current.getId().getDate())
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .site(site)
                            .build());

                    if (endOfDay)
                        break;
                    slotStart = slotStart.plusMinutes(15);
                    slotEnd = slotStart.plusMinutes(30);
                }
            }
        }

        return timeSlots;
    }

    // 멘토링 요청과 시간 슬롯이 겹치는지 확인하는 헬퍼 메서드
    private static boolean isSlotOverlapping(ScheduleDTO.Schedule slot, MentoringRequestEntity request) {
        // request의 시작 시간과 endTime이 동일한 경우는 겹치지 않음
        if (slot.getEndTime().equals(request.getStartTime())) {
            return false;
        }
        // Slot이 request와 겹치는지 확인
        return !(slot.getEndTime().isBefore(request.getStartTime()) || slot.getStartTime().isAfter(request.getEndTime()) || slot.getStartTime().equals(request.getEndTime()));
    }

    // 증빙자료 관련 메서드
    public List<MentoringResultDTO.MentoringResult> getMentoringResults(@NotNull Long courseId, @NotNull Long mentorId, @NotNull int year, @NotNull int month) {
        log.info("멘토링 증빙자료 조회 - course: {} mentor: {} month: {}", courseId, mentorId, month);
        List<MentoringResultDTO.MentoringResult> mentoringResults = new ArrayList<>();
        try {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = LocalDate.of(year, month+1, 1).minusDays(1);
            List<MentoringResultEntity> mentoringLogEntityList = mentoringResultRepository.findAllByCourseIdAndMentorIdAndDateBetweenOrderByDateAscTimeAsc(courseId, mentorId, start, end);
            if (!mentoringLogEntityList.isEmpty()) {
                for (MentoringResultEntity result : mentoringLogEntityList) {
                    MentoringResultDTO.MentoringResult dto = MentoringResultDTO.MentoringResult.fromEntity(result);
                    // 이미지
                    dto.setImages(fileStorage.getAttachmentList(AttachmentDomainType.MENTORING_RESULT, result.getId()));
                    mentoringResults.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mentoringResults;
    }

    @Transactional
    public void patchMentoringLogs(Long courseId, Long mentorId, List<MentoringResultDTO.Update> dto) {
        log.info("멘토링 증빙자료 생성/수정/삭제");

        CourseMemberEntity member = courseMemberEntityRepository.findByUserIdAndCourseId(mentorId, courseId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, "멘토가 코스 멤버가 아닙니다."));
        CourseEntity courseEntity = member.getCourse();
        UserEntity mentorEntity = member.getUser();

        for (MentoringResultDTO.Update resultDTO : dto) {
            // action = 0 : 동작 없음, action = 1 : 생성/수정, action = -1 : 삭제
            int action = resultDTO.getAction();
            // action = 0 : 동작 없음
            if (action == 0)
                continue;
            else if (action == 1) {
                // id == null 이면 증빙자료 생성
                if (resultDTO.getId() == null || resultDTO.getId() == 0L) {
                    MentoringResultEntity newLog = MentoringResultEntity.builder()
                            .date(resultDTO.getDate())
                            .time(resultDTO.getTime())
                            .length(resultDTO.getLength())
                            .content(resultDTO.getContent())
                            .location(resultDTO.getLocation())
                            .mentor(mentorEntity)
                            .course(courseEntity)
                            .mentees(resultDTO.getMentees())
                            .build();

                    newLog = mentoringResultRepository.save(newLog);

                    // 이미지 증빙자료 매핑
                    if (!resultDTO.getImages().isEmpty()) {
                        for (Long imgId : resultDTO.getImages()) {
                            fileStorage.mapFileToDomain(imgId, AttachmentDomainType.MENTORING_RESULT, newLog.getId());
                        }
                    }
                }
                // id != null 이면 증빙자료 수정
                else {
                    MentoringResultEntity result = mentoringResultRepository.findById(resultDTO.getId())
                            .orElseThrow(() -> new MainApplicationException(ErrorCode.MENTORING_RESULT_NOT_FOUND, "증빙자료를 찾을 수 없습니다."));

                    result.updateResult(
                            resultDTO.getDate(),
                            resultDTO.getTime(),
                            resultDTO.getLength(),
                            result.getLocation(),
                            result.getContent(),
                            resultDTO.getMentees()
                    );

                    mentoringResultRepository.save(result);

                    // 이미지 증빙자료 매핑 (필요시)
                    fileStorage.changeImages(AttachmentDomainType.MENTORING_RESULT, result.getId(), resultDTO.getImages());
                }
            }
            // 증빙자료 삭제
            else if (action == -1) {
                MentoringResultEntity result = mentoringResultRepository.findById(resultDTO.getId())
                        .orElseThrow(() -> new MainApplicationException(ErrorCode.MENTORING_RESULT_NOT_FOUND, "증빙자료를 찾을 수 없습니다."));


                MentoringRequestEntity request = this.getMentoringRequestEntityById(result.getRequest().getId());

                request.setRegistered(false);
                mentoringRequestRepository.save(request);

                // 이미지 파일 삭제
                fileStorage.deleteAllFilesIn(AttachmentDomainType.MENTORING_RESULT, resultDTO.getId());

                // 증빙자료 삭제
                mentoringResultRepository.delete(result);

            }
        }
    }

    @Transactional
    public void createLog(MentoringResultDTO.Create dto) {
        log.info("증빙자료 생성");

        CourseMemberEntity member = courseMemberEntityRepository.findByUserIdAndCourseId(dto.getMentorId(), dto.getCourseId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, "멘토가 코스 멤버가 아닙니다."));

        CourseEntity course = member.getCourse();
        UserEntity mentor = member.getUser();

        MentoringRequestEntity request = dto.getRequestId() == null ? null :    this.getMentoringRequestEntityById(dto.getRequestId());


        if (request != null && !Objects.equals(request.getStatus(), MentoringStatus.승인)) {
            throw new MainApplicationException(ErrorCode.MENTORING_RESULT_INVALID_PARAMETER, "승인 상태의 신청 내역만 증빙자료 작성이 가능합니다.");
        }

        MentoringResultEntity created = MentoringResultEntity.builder()
                .date(dto.getDate())
                .time(dto.getTime())
                .length(dto.getLength())
                .content(dto.getContent())
                .location(dto.getLocation())
                .mentor(mentor)
                .course(course)
                .request(request)
                .mentees(dto.getMentees())
                .build();

        Long resultId = mentoringResultRepository.save(created).getId();

        if (request != null) {
            request.setRegistered(true);
            mentoringRequestRepository.save(request);
        }

        // 이미지 매핑
        if (!dto.getImages().isEmpty()) {
            for (Long imgId : dto.getImages()) {
                fileStorage.mapFileToDomain(imgId, AttachmentDomainType.MENTORING_RESULT, resultId);
            }
        }
    }

//    @Override
//    public MentoringLog updateLog(MentoringLog updatedLog) {
//        log.info("멘토링 증빙자료 수정 : {} ", updatedLog.getId());
//        MentoringResultEntity updated = mentoringResultRepository.findById(updatedLog.getId()).orElseThrow(() -> new MainApplicationException(ErrorCode.MENTORING_NOT_FOUND, "MentoringLog Not Found"));
//
//        updated.updateMentoringLogDetails(updatedLog);
//
//        return MentoringLog.fromEntity(mentoringResultRepository.save(updated));
//    }

    public void deleteLog(Long logId) {
        log.info("멘토링 증빙자료 삭제 : {}", logId);
        MentoringResultEntity deleted = mentoringResultRepository.findById(logId).orElseThrow(() -> new MainApplicationException(ErrorCode.MENTORING_RESULT_NOT_FOUND, "증빙자료를 찾을 수 없습니다."));

        mentoringResultRepository.delete(deleted);
    }

    public int getMentoringResultCount(Long mentorId, Long courseId, int year, int month) {
        // 입력값 검증
        if (mentorId == null) {
            throw new MainApplicationException(ErrorCode.MENTORING_RESULT_PARAMETER_NULL, "멘토 아이디가 널입니다.");
        }
        if (courseId == 0) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "코스 ID는 필수 입력값입니다.");
        }
        if (year == 0) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "연도는 필수 입력값입니다.");
        }
        if (month == 0) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "연도는 필수 입력값입니다.");
        }

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = LocalDate.of(year, month + 1, 1).minusDays(1);

        return mentoringResultRepository.findAllByCourseIdAndMentorIdAndDateBetweenOrderByDateAscTimeAsc(courseId, mentorId, from, to).size();
    }

    public MentoringRequestEntity getMentoringRequestByMentorAndMentee(Long mentorId, Long requesterId) {
            return mentoringRequestRepository.findTopByMentorIdAndRequesterIdOrderByIdDesc(mentorId, requesterId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 멘토와 멘티 간의 멘토링 요청이 존재하지 않습니다."));
    }


    public MentoringRequestEntity getMentoringRequestEntityById(Long requestId) {
        return mentoringRequestRepository.findById(requestId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.MENTORING_REQUEST_NOT_FOUND, String.format("멘토링 요청 내역을 찾을 수 없습니다. [%d]", requestId)));
    }
}
