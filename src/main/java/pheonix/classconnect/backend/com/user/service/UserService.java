package pheonix.classconnect.backend.com.user.service;

import pheonix.classconnect.backend.com.user.model.UserDTO;

public interface UserService {
    UserDTO.UserInfo findOne(UserDTO.FindOne findDto);
}
