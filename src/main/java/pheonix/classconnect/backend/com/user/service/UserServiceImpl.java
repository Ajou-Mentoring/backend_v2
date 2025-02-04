package pheonix.classconnect.backend.com.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pheonix.classconnect.backend.com.auth.constant.AuthorityCode;
import pheonix.classconnect.backend.com.auth.entity.AuthorityEntity;
import pheonix.classconnect.backend.com.auth.model.AuthorityDTO;
import pheonix.classconnect.backend.com.auth.repository.AuthorityRepository;
import pheonix.classconnect.backend.com.department.entity.DepartmentEntity;
import pheonix.classconnect.backend.com.department.model.DepartmentDTO;
import pheonix.classconnect.backend.com.department.repository.DepartmentRepository;
import pheonix.classconnect.backend.com.user.constant.UserActiveStatus;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthorityRepository authorityRepository;

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
            Integer maxDepId = departmentRepository.findMaxId().orElse(0);
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
}
