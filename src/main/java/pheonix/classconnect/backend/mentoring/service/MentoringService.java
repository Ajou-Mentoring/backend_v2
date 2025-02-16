package pheonix.classconnect.backend.mentoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.mentoring.contants.MentoringSite;
import pheonix.classconnect.backend.mentoring.contants.MentoringStatus;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;
import pheonix.classconnect.backend.mentoring.entity.ScheduleEntity;
import pheonix.classconnect.backend.mentoring.model.MentoringRequestDTO;
import pheonix.classconnect.backend.mentoring.model.ScheduleDTO;
import pheonix.classconnect.backend.mentoring.repository.MentoringRequestRepository;
import pheonix.classconnect.backend.mentoring.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringService {
    private final UserRepository userRepository;
    private final MentoringRequestRepository mentoringRequestRepository;
    private final ScheduleRepository scheduleRepository;

    // 멘토링 가능한 일정을 보여주는 서비스
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

            LocalTime slotStart = current.getStartTime().withMinute(0).withSecond(0).withNano(0);
            if (current.getStartTime().getMinute() % 15 != 0) {
                slotStart = slotStart.plusMinutes(((current.getStartTime().getMinute() / 15) + 1) * 15);
            }
            LocalTime slotEnd = slotStart.plusMinutes(30);

            // 만약 다음 스케줄이 존재하고 두 스케줄이 완전 붙어있으면서 이전 스케줄이 "전체"라면
            if (i + 1 < schedules.size()) {
                ScheduleEntity next = schedules.get(i+1);
                if (current.getEndTime().equals(next.getStartTime()) &&
                    Objects.equals(current.getSite(), MentoringSite.ALL)) {
                    // 1. current가 site=1(전체)라면 겹치는 구간의 slot은 next의 site를 따라간다.
                    while (slotEnd.isBefore(next.getEndTime()) || slotEnd.equals(next.getEndTime())) {
                        short site = current.getSite();
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

                    }
                } else {
                    while (slotEnd.isBefore(current.getEndTime()) || slotEnd.equals(current.getEndTime())) {
                        short site = current.getSite();

                        timeSlots.add(ScheduleDTO.Schedule.builder()
                                .date(current.getId().getDate())
                                .startTime(slotStart)
                                .endTime(slotEnd)
                                .site(site)
                                .build());
                        slotStart = slotStart.plusMinutes(15);
                        slotEnd = slotStart.plusMinutes(30);
                    }
                }


            }
            else {
                while (slotEnd.isBefore(current.getEndTime()) || slotEnd.equals(current.getEndTime())) {
                    short site = current.getSite();

                    timeSlots.add(ScheduleDTO.Schedule.builder()
                            .date(current.getId().getDate())
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .site(site)
                            .build());
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
        return !(slot.getEndTime().isBefore(request.getStartTime()) || slot.getStartTime().isAfter(request.getEndTime()));
    }
}
