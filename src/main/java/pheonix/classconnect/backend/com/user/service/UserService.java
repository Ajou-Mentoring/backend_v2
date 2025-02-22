package pheonix.classconnect.backend.com.user.service;

import pheonix.classconnect.backend.com.user.model.UserDTO;

public interface UserService {

    void createUser(UserDTO.Create user);
    void saveUser(UserDTO.User userDto);
    UserDTO.User findUserByStudentNo(String studentNo);
    UserDTO.User findUserById(Long id);
}
