package pheonix.classconnect.backend.com.user.service;

import pheonix.classconnect.backend.com.common.model.Paged;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.model.UserDTO;

public interface UserService {

    void createUser(UserDTO.Create user);
    void saveUser(UserDTO.User userDto);
    UserDTO.User findUserByStudentNo(String studentNo);
    UserDTO.User findUserById(Long id);
    Paged<UserDTO.User> findUsersByName(String name, int page, int size);
    Paged<UserDTO.User> findUsersByStudentNo(String no, int page, int size);
    Paged<UserDTO.User> findUsers(int page, int size);

    UserEntity findUserInfoById(Long id);
    void updateUserInfo(Long userId, UserDTO.Update userDto);
}
