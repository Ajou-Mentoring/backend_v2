package pheonix.classconnect.backend.com.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.auth.constant.AuthorityCode;
import pheonix.classconnect.backend.com.auth.entity.AuthorityEntity;
import pheonix.classconnect.backend.com.auth.repository.AuthorityRepository;
import pheonix.classconnect.backend.com.common.model.Paged;
import pheonix.classconnect.backend.com.department.entity.DepartmentEntity;
import pheonix.classconnect.backend.com.department.repository.DepartmentRepository;
import pheonix.classconnect.backend.com.user.constant.UserActiveStatus;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthorityRepository authorityRepository;
    private final FileStorage fileStorage;

    @Override
    public void createUser(UserDTO.Create createDto) {
        log.info("{}.createUser()", this.getClass());

        /*검증*/
        // 이미 가입된 유저인지 체크
        if (
            userRepository.findByEmail(createDto.getEmail()).isPresent() ||
            userRepository.findByStudentNo(createDto.getStudentNo()).isPresent()
        ) {
            throw new MainApplicationException(ErrorCode.DUPLICATED_USER, "이미 가입된 유저입니다.");
        }

        // 가입 정보로 유저 생성
        UserEntity newUser = UserEntity.builder()
                .id(null)
                .name(createDto.getName())
                .email(createDto.getEmail())
                .studentNo(createDto.getStudentNo())
                .activeState(UserActiveStatus.ACTIVE)
                .build();

        // 학과 세팅
        DepartmentEntity dep = departmentRepository.findByName(createDto.getDepartmentName()).orElse(null);
        // 임시 로직 : 없다면 DB에 새로운 학과 저장 (삭제 예정)
        if (dep == null) {
            Integer maxDepId = departmentRepository.findTopByOrderByIdDesc().orElse(0);
            dep = departmentRepository.save(new DepartmentEntity(maxDepId+1, createDto.getDepartmentName()));
        }
        newUser.updateDepartment(dep);

        // 권한 세팅 - 학생
        AuthorityEntity auth = authorityRepository.findById(AuthorityCode.STUDENT)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.AUTH_NOT_FOUND, "학생 권한(1)을 찾을 수 없습니다."));
        newUser.addAuthority(auth);

        userRepository.save(newUser);
    }

    @Override
    public void saveUser(UserDTO.User userDto) {

    }

    @Override
    public UserDTO.User findUserByStudentNo(String studentNo) {
        return userRepository.findByStudentNo(studentNo)
                .map(UserDTO.User::fromEntity)
                .orElse(null);
    }

    @Override
    public UserDTO.User findUserById(Long id) {
        log.info("유저 조회 : {}", id);

        return UserDTO.User.fromEntity(userRepository.findById(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "유저 정보가 없습니다.")));
    }

    @Override
    public Paged<UserDTO.User> findUsersByName(String name, int page, int size) {
        log.info("유저 페이지 조회 : 이름");
        Pageable pageable = PageRequest.of(page, size, Sort.by(List.of(Sort.Order.asc("name"))));
        Page<UserEntity> users = userRepository.findAllByNameContainsIgnoreCase(name, pageable);

        return Paged.<UserDTO.User>builder()
                .currentPage(users.getNumber())
                .size(users.getSize())
                .numberOfElements(users.getNumberOfElements())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .items(users.getContent().stream()
                        .map(UserDTO.User::fromEntity)
                        .toList())
                .build();
    }

    @Override
    public Paged<UserDTO.User> findUsersByStudentNo(String no, int page, int size) {
        log.info("유저 페이지 조회 : 학번");
        Pageable pageable = PageRequest.of(page, size, Sort.by(List.of(Sort.Order.asc("name"))));
        Page<UserEntity> users = userRepository.findAllByStudentNoContains(no, pageable);

        return Paged.<UserDTO.User>builder()
                .currentPage(users.getNumber())
                .size(users.getSize())
                .numberOfElements(users.getNumberOfElements())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .items(users.getContent().stream()
                        .map(UserDTO.User::fromEntity)
                        .toList())
                .build();
    }

    @Override
    public Paged<UserDTO.User> findUsers(int page, int size) {
        log.info("유저 페이지 조회");
        Pageable pageable = PageRequest.of(page, size, Sort.by(List.of(Sort.Order.asc("name"))));
        Page<UserEntity> users = userRepository.findAll(pageable);

        return Paged.<UserDTO.User>builder()
                .currentPage(users.getNumber())
                .size(users.getSize())
                .numberOfElements(users.getNumberOfElements())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .items(users.getContent().stream()
                        .map(UserDTO.User::fromEntity)
                        .toList())
                .build();
    }
}
