package pheonix.classconnect.backend.com.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    /*
        PK 또는 Unique 키로 유저를 조회한다.
        만약 조회된 유저가 없다면 null 을 반환한다.
        키값 : Long id, String studentNo, String email
    */
    @Override
    public UserDTO.UserInfo findOne(UserDTO.FindOne findDto) {
        UserEntity found = null;

        log.info("UserService.findOne()");
        log.debug(findDto.toString());
        if (findDto.getId() != null) {
            log.debug("ID로 조회: [{}]", findDto.getId());
            found = userRepository.findById(findDto.getId())
                    .orElse(null);
        }
        else if (findDto.getStudentNo() != null) {
            log.debug("StudentNo로 조회: [{}]", findDto.getStudentNo());
            found = userRepository.findByStudentNo(findDto.getStudentNo())
                    .orElse(null);
        }
        else if (findDto.getEmail() != null) {
            log.debug("Email로 조회: [{}]", findDto.getEmail());
            found = userRepository.findByEmail(findDto.getEmail())
                    .orElse(null);
        }

        return found == null ? null : UserDTO.UserInfo.fromEntity(found);
    }
}
