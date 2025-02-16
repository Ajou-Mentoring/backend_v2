package pheonix.classconnect.backend.mentoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.mentoring.entity.ScheduleEntity;
import pheonix.classconnect.backend.mentoring.entity.ScheduleId;
import pheonix.classconnect.backend.mentoring.entity.TimeTableEntity;
import pheonix.classconnect.backend.mentoring.entity.TimeTableId;
import pheonix.classconnect.backend.mentoring.model.ScheduleDTO;
import pheonix.classconnect.backend.mentoring.model.TimeTableDTO;
import pheonix.classconnect.backend.mentoring.repository.ScheduleRepository;
import pheonix.classconnect.backend.mentoring.repository.TimeTableRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final TimeTableRepository timeTableRepository;
    private final UserRepository userRepository;

    public List<ScheduleDTO.Schedule> getDuplicatedSchedule(TimeTableDTO.Create timeTable) {
        log.info("중복 일정 조회");
        List<ScheduleDTO.Schedule> duplicated = new ArrayList<>();
        // 요청한 기간 내 이미 등록된 스케줄 조회
        List<ScheduleEntity> savedSchedules = scheduleRepository.findAllById_UserIdAndId_DateBetweenOrderById_DateAscStartTimeAsc(timeTable.getUserId(), timeTable.getStartDate(), timeTable.getEndDate());

        // 등록된 스케줄이 없거나 등록할 스케줄이 없는 경우 빈 객체 배열 리턴
        if (savedSchedules.isEmpty() || timeTable.getSchedules().isEmpty()) {
            return duplicated;
        }

        // 스케줄 객체 구축
        List<ScheduleDTO.Schedule> requestedSchedules = generateSchedule(timeTable);

        for (ScheduleDTO.Schedule requestedSchedule : requestedSchedules) {
            for (ScheduleEntity savedSchedule : savedSchedules) {
                ScheduleDTO.Schedule requested = requestedSchedule;
                ScheduleDTO.Schedule saved = ScheduleDTO.Schedule.fromEntity(savedSchedule);

                // 충돌된 일정이 있다면 해당 스케줄을 충돌리스트에 추가한다.
                if (isDuplicated(requested, saved)) {
                    duplicated.add(saved);
                    break;
                }
            }
        }
//        while (idxA < requestedSchedules.size() && idxB < savedSchedules.size()) {
//            ScheduleDTO.Schedule requested = requestedSchedules.get(idxA);
//            ScheduleDTO.Schedule saved = ScheduleDTO.Schedule.fromEntity(savedSchedules.get(idxB));
//
//            // 충돌된 일정이 있다면 해당 스케줄을 충돌리스트에 추가한다.
//            if (isDuplicated(requested, saved)) {
//                duplicated.add(saved);
//            }
//
//            if (requested.getStartTime().isBefore(saved.getStartTime())) {
//                idxA++;
//            } else {
//                idxB++;
//            }
//        }

        return duplicated;
    }

    @Transactional
    public void uploadWeeklySchedule(TimeTableDTO.Create timeTable) {
        log.info("주간 일정 등록");
        UserEntity user = userRepository.findById(timeTable.getUserId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("유저 정보를 찾을 수 없습니다. [%d]", timeTable.getUserId())));

        List<ScheduleDTO.Schedule> schedules = generateSchedule(timeTable);
        //List<ScheduleEntity> saved = scheduleRepository.findAllById_UserIdAndId_DateBetweenOrderById_DateAscStartTimeAsc(user.getId(), timeTable.getStartDate(), timeTable.getEndDate());

        // 충돌되는 일정은 모두 제거
        List<ScheduleDTO.Schedule> duplicated = getDuplicatedSchedule(timeTable);
        for (ScheduleDTO.Schedule schedule : duplicated) {
            ScheduleId id = new ScheduleId(schedule.getUser().getId(), schedule.getDate(), schedule.getSerNo());
            scheduleRepository.deleteById(id);
        }

        // 나머지 일정은 모두 추가
        for (ScheduleDTO.Schedule schedule : schedules) {
            // serNo를 구한다.
            Integer serNo = scheduleRepository.findMaxSerNoByUserIdAndDate(schedule.getUser().getId(), schedule.getDate());
            Integer nextSerNo = serNo == null ? 1 : serNo + 1;

            scheduleRepository.save(ScheduleEntity.builder()
                    .id(new ScheduleId(schedule.getUser().getId(), schedule.getDate(), nextSerNo))
                    .user(user)
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .site(schedule.getSite())
                    .build()
            );
        }
    }

    public void saveTimeTable(TimeTableDTO.Create timeTable) {
        log.info("시간표 저장");

        List<TimeTableEntity> timeTableEntities = new ArrayList<>();

        // 유저
        UserEntity user = userRepository.findById(timeTable.getUserId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("유저를 찾을 수 없습니다. [%d]", timeTable.getUserId())));
        // 가장 마지막 버전 값
        Integer currVersion = timeTableRepository.findMaxVerByUserId(user.getId());
        Integer nextVersion = currVersion == null ? 1 : currVersion + 1;
        Integer serNo = 1;

        for (ScheduleDTO.Weekly slot : timeTable.getSchedules()) {
            TimeTableId id = new TimeTableId(user.getId(), nextVersion, serNo);
            TimeTableEntity entity = TimeTableEntity.builder()
                    .id(id)
                    .user(user)
                    .day((short) slot.getDayOfWeek().intValue())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .site(slot.getSite())
                    .build();

            timeTableRepository.save(entity);

            serNo++;
        }

        // 기존 버전은 삭제
        timeTableRepository.findAllById_UserIdAndId_Ver(user.getId(), currVersion).forEach(timeTableRepository::delete);
    }

    public List<TimeTableDTO.TimeTable> getTimeTable(Long userId) {
        log.info("주간 스케줄 조회");

        Integer lastVersion = timeTableRepository.findMaxVerByUserId(userId);

        return timeTableRepository.findAllById_UserIdAndId_Ver(userId, lastVersion).stream()
                .map(TimeTableDTO.TimeTable::fromEntity)
                .toList();
    }

    public List<ScheduleDTO.Schedule> getMonthlySchedule(Long userId, Integer year, Integer month) {
        log.info("월간 스케줄 조회");

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return scheduleRepository.findAllById_UserIdAndId_DateBetweenOrderById_DateAscStartTimeAsc(userId, startDate, endDate).stream()
                .map(ScheduleDTO.Schedule::fromEntity)
                .toList();
    }

    public List<ScheduleDTO.Schedule> getDailySchedule(Long userId, Integer year, Integer month, Integer day) {
        return scheduleRepository.findAllById_UserIdAndId_DateOrderByStartTime(userId, LocalDate.of(year, month, day)).stream()
                .map(ScheduleDTO.Schedule::fromEntity)
                .toList();
    }

    @Transactional
    public void saveDailySchedule(Long userId, LocalDate date, List<ScheduleDTO.Create> schedule) {
        log.info("일일 일정 등록");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("사용자 정보를 찾을 수 없습니다. [%d]", userId)));

        // 기존에 등록된 스케줄은 삭제
        scheduleRepository.deleteAllById_UserIdAndIdDate(userId, date);

        Integer lastSerNo = scheduleRepository.findMaxSerNoByUserIdAndDate(userId, date);
        Integer serNo = lastSerNo == null ? 1 : lastSerNo + 1;
        // 일일 스케줄 등록
        if(!schedule.isEmpty()) {
            for (ScheduleDTO.Create s : schedule) {
                ScheduleId id = new ScheduleId(userId, date, serNo);
                ScheduleEntity entity = ScheduleEntity.builder()
                        .id(id)
                        .user(user)
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .site(s.getMentoringType())
                        .build();

                scheduleRepository.save(entity);

                serNo++;
            }

        }
    }

    private boolean isDuplicated(ScheduleDTO.Schedule a, ScheduleDTO.Schedule b) {
        // 만약 날짜가 다르면 충돌 아님
        log.info("a: {}, b: {}", a.getDate(), b.getDate());
        if (!a.getDate().isEqual(b.getDate())) {
            return false;
        }

        // 날짜가 겹치면 무조건 밀어버림 (추후 변경 가능함)
        else if (true) {
            return true;
        }

        // a의 시작 시간이 b의 시작~종료 이내에 있거나
        // a의 종료 시간이 b의 시작~종료 이내에 있으면 충돌
        if (!a.getStartTime().isAfter(b.getStartTime()) && !a.getStartTime().isBefore(b.getEndTime())) {
            return true;
        }
        else if (!a.getEndTime().isAfter(b.getStartTime()) && !a.getEndTime().isBefore(b.getEndTime())) {
            return true;
        }

        // b의 시작 시간이 a의 시작~종료 이내에 있거나
        // b의 종료 시간이 a의 시작~종료 이내에 있으면 충돌
        else if (!b.getStartTime().isAfter(a.getStartTime()) && !b.getStartTime().isBefore(a.getEndTime())) {
            return true;
        }
        else if (!b.getEndTime().isAfter(a.getStartTime()) && !b.getEndTime().isBefore(a.getEndTime())) {
            return true;
        }

        return false;
    }

    private List<ScheduleDTO.Schedule> generateSchedule(TimeTableDTO.Create timeTable) {
        List<ScheduleDTO.Schedule> schedules = new ArrayList<>();
        if (timeTable.getSchedules().isEmpty())
            return schedules;

        LocalDate date = timeTable.getStartDate();
        UserDTO.User user = UserDTO.User.fromEntity(userRepository.findById(timeTable.getUserId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("유저 정보를 찾을 수 없습니다. [%d]", timeTable.getUserId()))));

        while (date.isBefore(timeTable.getEndDate().plusDays(1))) {
            // 요일을 구한다.
            int dayOfWeek = date.getDayOfWeek().getValue();

            // 주간 스케줄에서 같은 요일에 해당하는 스케줄을 집어 넣는다.
            for (ScheduleDTO.Weekly s : timeTable.getSchedules()) {
                if (s.getDayOfWeek() == dayOfWeek) {
                    schedules.add(ScheduleDTO.Schedule.builder()
                            .user(user)
                            .date(date)
                            .startTime(s.getStartTime())
                            .endTime(s.getEndTime())
                            .site(s.getSite())
                            .build()
                    );
                } else if (s.getDayOfWeek() > dayOfWeek)
                    break;
            }

            date = date.plusDays(1);
        }

        return schedules;
    }
}
