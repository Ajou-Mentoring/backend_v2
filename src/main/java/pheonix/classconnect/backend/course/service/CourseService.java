package pheonix.classconnect.backend.course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentDomainType;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.constants.CourseStatus;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.course.entity.CourseMemberEntity;
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.course.model.CourseDetail;
import pheonix.classconnect.backend.course.model.request.RemoveMemberFromCourseDTO;
import pheonix.classconnect.backend.course.model.request.UpdateCourseDTO;
import pheonix.classconnect.backend.course.repository.CourseEntityRepository;
import pheonix.classconnect.backend.course.model.SemesterMapper;
import pheonix.classconnect.backend.course.repository.CourseMemberEntityRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {
    private final CourseEntityRepository courseEntityRepository;
    private final UserRepository userRepository;
    private final CourseMemberEntityRepository courseMemberEntityRepository;
    private final FileStorage fileStorage;

    @Transactional
    public void create(CourseDTO.Create dto, MultipartFile image) {
        Optional<CourseEntity> courseEntity = courseEntityRepository.findBySemesterAndYearAndCode(dto.getSemester(), dto.getYear(), dto.getCode());

        if (courseEntity.isPresent()) {
            throw new MainApplicationException(ErrorCode.DUPLICATED_COURSE, "해당 연도/학기에 동일한 코드를 갖는 코스가 존재합니다.");
        }

        log.info("클래스 생성 : [{}]", dto);

        CourseEntity course = CourseEntity.builder()
                .id(null)
                .name(dto.getName())
                .code(dto.getCode())
                .year(dto.getYear())
                .semester(dto.getSemester())
                .professor(dto.getProfessorName())
                .status(CourseStatus.OPEN)
                .memberCode(null)
                .build();

        // memberCode 생성
        course.updateMemberCode(generateMemberCode());

        // 코스 저장
        CourseEntity saved = courseEntityRepository.save(course);

        // 이미지가 존재한다면 저장
        fileStorage.saveFile(image, AttachmentDomainType.COURSE, saved.getId());
    }

    @Transactional
    public void delete(Long courseId) {
        // 코스가 존재하는지 체크
        if (courseEntityRepository.existsById(courseId)) {

            courseEntityRepository.deleteById(courseId);

            // 코스 이미지 삭제
            List<File> images = fileStorage.getAttachmentList(AttachmentDomainType.COURSE, courseId);
            if (!images.isEmpty()) {
                for (File image : images) {
                    fileStorage.deleteByFileId(image.getId());
                }
            }

        }
        else {
            throw new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, "삭제할 코스를 찾을 수 없습니다.");
        }


    }

//    public List<Course> getCoursesByProfessorId(Integer professorId, Integer year, Integer semester) {
//        List<CourseEntity> courses = courseEntityRepository.findAllByProfessorIdAndYearAndSemester(professorId, year, Semester.fromCode(semester));
//
//        return courses.stream()
//                .map(Course::fromEntity)
//                .collect(Collectors.toList());
//
//    }

    public List<CourseDTO.Course> getCourses(CourseDTO.Find01 findDto) {
        List<CourseEntity> courses;
        // 만약 멤버아이디가 null인 경우 모두 조회
        if (findDto.getMemberId() == null) {
            courses = courseEntityRepository.findAllByYearAndSemester(findDto.getYear(), findDto.getSemester());
        }
        else {
            courses = courseMemberEntityRepository.findByUserIdAndCourseYearAndCourseSemester(findDto.getMemberId(), findDto.getYear(), findDto.getSemester())
                    .stream().map(CourseMemberEntity::getCourse).toList();
        }

        if (courses.isEmpty()) return new ArrayList<>();
        log.info("변환: entity -> DTO");
        return courses.stream().map(CourseDTO.Course::fromEntity).toList();
    }

    public List<CourseDetail> getSemestersByStudentId(Long studentId) {
        log.info("getSemesterByStudentId : {}", studentId);

        // 유저가 참여한 모든 코스 정보를 찾는다.
        List<SemesterMapper> semesters = courseMemberEntityRepository.findDistinctSemestersByUserId(studentId);

        if (semesters.isEmpty()) {
            return new ArrayList<>();
        } else {
            return semesters.stream().map(semester -> new CourseDetail(semester.getYear(), semester.getSemester())).toList();
        }
    }

//    public List<CourseDetail> getSemestersByProfessorId(Integer professorId) {
//        List<Object> objects = courseEntityRepository.findDistinctSemestersByUserId(professorId);
//
//        List<CourseDetail> courseDetails = objects.stream().map(x -> {
//            Object[] obj = (Object[]) x;
//            Integer year = (Integer) obj[0];
//            String semester = obj[1].toString();
//            return new CourseDetail(year, semester);
//        }).collect(Collectors.toList());
//
//        return courseDetails;
//    }

    public CourseDTO.Course getACourseById(Long courseId) {
        return courseEntityRepository.findById(courseId).map(CourseDTO.Course::fromEntity)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, "코스를 찾을 수 없습니다."));
    }

    @Transactional
    public List<UserDTO.User> getMentorsInCourse(Long courseId) {

        log.info("멘토 조회");

        courseEntityRepository.findById(courseId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, "코스를 찾을 수 없습니다."));

        List<CourseMemberEntity> members = courseMemberEntityRepository.findByCourseIdAndRole(courseId, CourseRole.MENTOR);

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        return members.stream()
                .map(CourseMemberEntity::getUser)
                .map(UserDTO.User::fromEntity)
                .toList();

    }

//    public InvitedCourseDTO inviteStudentsToCourse(Integer courseId, InviteStudentToCourseDTO inviteStudentToCourseDTO, Integer userId) {
//        CourseEntity course = findCourseByIdElseThrow(courseId);
//        UserEntity userEntity = userService.findUserByIdElseThrow(userId);
//
//        if (userEntity.getRole() != UserRole.PROFESSOR) {
//            throw new MainApplicationException(ErrorCode.INVALID_PERMISSION, "교수만 접근할 수 있습니다.");
//        }
//
//        return courseInviteService.inviteStudentsToCourse(course, inviteStudentToCourseDTO.getStudentEmails(), inviteStudentToCourseDTO.getRole());
//    }

//    public List<ParticipantDTO> getStudentsParticipants(Integer courseId) {
//
//        CourseEntity course = findCourseByIdElseThrow(courseId);
//        //UserEntity userEntity = userService.findUserByIdElseThrow(userId);
//
//        // Todo: 해당 클래스 교수가 아니더라도 조회 가능한 거 && 같은 클래스더라도 코딩트레이너면 조회 안되는거 수정 -> 프론트 단에서 제어
////        if (userEntity.getRole() != UserRole.PROFESSOR) {
////            throw new MainApplicationException(ErrorCode.INVALID_PERMISSION, "교수만 접근할 수 있습니다.");
////        }
//
//        List<UserEntity> students = course.getStudents().stream()
//                .map(UserCourseEntity::getUser)
//                .collect(Collectors.toList());
//
//        // 교수 추가
//        students.add(0, course.getProfessor());
//
//        List<ParticipantDTO> participants = students.stream()
//                .map(mentee -> {
//                    // 필터링하여 특정 코스에 대한 메시지룸에서 해당 멘티가 보낸 메시지 수 계산
//                    int messageCountForCourse = mentee.getHostedMessageRooms().stream()
//                            .filter(room -> room.getCourse() != null && room.getCourse().getId().equals(courseId))
//                            .mapToInt(room -> (int) room.getMessages().stream()
//                                    .filter(message -> message.getSender().equals(mentee))
//                                    .count())
//                            .sum();
//
//                    Integer roleCode = mentee.getCourses().stream()
//                            .filter(userCourse -> userCourse.getCourse().getId().equals(courseId))
//                            .findFirst()
//                            .map(userCourse -> userCourse.getRole().getCode())
//                            .orElse(CourseRole.MENTEE.getCode());
//
//                    return new ParticipantDTO(
//                            mentee.getId(),
//                            mentee.getNickname(),
//                            mentee.getEmail(),
//                            (int) mentee.getComments().stream()
//                                    .filter(comment -> comment.getPost().getCourse().getId().equals(courseId))
//                                    .count(),  // 댓글 수 계산
//                            (int) mentee.getPosts().stream()
//                                    .filter(post -> post.getCourse().getId().equals(courseId))
//                                    .count(),  // 게시글 수 계산
//                            (int) mentee.getMenteeSessions().stream()
//                                    .filter(session -> {
//                                        LocalTime sessionTime = LocalTime.parse(session.getStartTime(), DateTimeFormatter.ISO_TIME);
//                                        LocalDateTime sessionDateTime = LocalDateTime.of(session.getDate(), sessionTime);
//                                        return sessionDateTime.isBefore(LocalDateTime.now());
//                                    })
//                                    .filter(session -> session.getState() == MentoringState.BOOK
//                                            && session.getCourse().getId().equals(courseId))
//                                    .count(),
//                            messageCountForCourse,  // 메시지 수 계산,
//                            roleCode
//                    );
//                })
//                .collect(Collectors.toList());
//        return participants;
//    }

//    public OffsetPaginationResponse<CourseResponse> fetchAllCourses(CourseFetchRequestDTO courseFetchRequestDTO, OffsetPaginationRequestDTO offsetPaginationRequestDTO) {
//        Pageable pageable = PageRequest.of(offsetPaginationRequestDTO.getPage() - 1, offsetPaginationRequestDTO.getLimit());
//        Semester convertedSemester = Semester.fromCode(courseFetchRequestDTO.getSemester());
//
//        Page<CourseEntity> coursePages = courseEntityRepository.findByYearAndSemester(courseFetchRequestDTO.getYear(), convertedSemester, pageable);
//
//        List<CourseResponse> courseResponses = coursePages.getContent().stream()
//                .map(Course::fromEntity)
//                .map(CourseResponse::fromCourse)
//                .collect(Collectors.toList());
//
//        return new OffsetPaginationResponse<>(courseResponses, (int) coursePages.getTotalElements());
//    }

//    public List<CourseDetail> getAllSemesters() {
//        // 모든 학기를 조회하는 새로운 메서드 호출
//        List<Object> objects = courseEntityRepository.findDistinctSemesters();
//
//        List<CourseDetail> courseDetails = objects.stream().map(x -> {
//            Object[] obj = (Object[]) x;
//            Integer year = (Integer) obj[0];
//            String semester = obj[1].toString();
//            return new CourseDetail(year, semester);
//        }).collect(Collectors.toList());
//
//        return courseDetails;
//    }


    @Transactional
    public void updateCourse(Long courseId, UpdateCourseDTO updateCourseDTO) {
        CourseEntity course = courseEntityRepository.findById(courseId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, String.format("코스 수정 대상을 찾을 수 없습니다. [%d]", courseId)));

        course.setCode(updateCourseDTO.getCourseCode());
        course.setName(updateCourseDTO.getName());

        courseEntityRepository.save(course);
    }


    @Transactional
    public void removeMemberFromCourse(Long courseId, RemoveMemberFromCourseDTO removeMemberFromCourseDTO) {

        CourseEntity course = courseEntityRepository.findById(courseId)
                        .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND));

        if (removeMemberFromCourseDTO.getMemberIds().isEmpty()) {
            throw new MainApplicationException(ErrorCode.BAK_LOGIC_ERROR, "삭제할 멤버 리스트가 비어있습니다.");
        }

        removeMemberFromCourseDTO.getMemberIds().forEach(userId -> {
            CourseMemberEntity member = courseMemberEntityRepository.findByUserIdAndCourseId(userId, courseId).orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, "삭제할 코스 멤버 정보가 없습니다."));
            courseMemberEntityRepository.delete(member);
        });

    }

//    public void updateMemberRoleInCourse(Long courseId, Long memberId, UpdateMemberRoleDTO updateMemberRoleDTO) {
//        UserEntity user = userRepository.findById(memberId)
//                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, ""))
//        CourseEntity course = findCourseByIdElseThrow(courseId);
//        userCourseService.updateMemberRoleInCourse(course, user, updateMemberRoleDTO);
//    }

//    @Transactional
//    public InvitedCourseDTO inviteStudentsToCourse(Integer courseId, AdminInviteStudentToCourseDTO adminInviteStudentToCourseDTO) {
//        CourseEntity course = findCourseByIdElseThrow(courseId);
//
//        List<UserEntity> validMembers = userService.findAllUsersById(adminInviteStudentToCourseDTO.getMemberIds());
//        CourseRole role = CourseRole.fromCode(adminInviteStudentToCourseDTO.getRole());
//        List<UserCourseEntity> userCourseEntities = new ArrayList<>();
//        for (UserEntity member : validMembers) {
//
//            UserCourseId userCourseId = new UserCourseId(member.getId(), course.getId());
//            UserCourseEntity courseMember = UserCourseEntity.builder()
//                    .id(userCourseId)
//                    .course(course)
//                    .user(member)
//                    .role(role)
//                    .build();
//
//            userCourseEntities.add(courseMember);
//        }
//
//        List<InvitedUserDTO> users = new ArrayList<>();
//
//        if (!userCourseEntities.isEmpty()) {
//            userCourseService.saveAll(userCourseEntities);
//            users = userCourseEntities.stream()
//                    .map(userCourseEntity -> new InvitedUserDTO(User.fromEntity(userCourseEntity.getUser()), userCourseEntity.getRole()))
//                    .collect(Collectors.toList());
//        }
//
//        return new InvitedCourseDTO(Course.fromEntity(course), users, new ArrayList<>());
//
//    }

    public String generateMemberCode() {
        int maxCount = 10;
        String generatedCode;
        while(true) {
            generatedCode = RandomStringUtils.randomAlphanumeric(6);

            if(!courseEntityRepository.existsByMemberCode(generatedCode)) {
                return generatedCode;
            }

            maxCount--;

            if (maxCount <= 0)
                throw new MainApplicationException(ErrorCode.BAK_LOGIC_ERROR, "참여 코드 생성 횟수 제한을 초과했습니다.");
        }

    }
}
