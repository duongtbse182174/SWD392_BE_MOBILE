package swd392.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import swd392.app.dto.request.UserCreationRequest;
import swd392.app.dto.request.UserUpdateRequest;
import swd392.app.dto.response.UserResponse;
import swd392.app.entity.Role;
import swd392.app.entity.User;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.UserMapper;
import swd392.app.repository.RoleRepository;
import swd392.app.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository ;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

//    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(UserCreationRequest request) {

        if (userRepository.existsByUserName(request.getUserName()))
            throw new AppException(ErrorCode.USER_EXIST);

        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXIST);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByRoleType("STAFF")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.setRole(role);

        return userMapper.toUserResponse(userRepository.save(user));
    }

//    public UserResponse getMyInfo(){
//        var context = SecurityContextHolder.getContext();
//        String email = context.getAuthentication().getName();
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
//
//        return userMapper.toUserResponse(user);
//    }
//
//    public UserResponse updateUser(String userId, UserUpdateRequest request){
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        userMapper.updateUser(user, request);
//
//        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
//            user.setPassword(passwordEncoder.encode(request.getPassword()));
//        }
//
//        return userMapper.toUserResponse(userRepository.save(user));
//    }
//
//    @PostAuthorize("returnObject.email == authentication.name")
//    public UserResponse getUser(String userId){
//        log.info("In method get user by Id");
//
//        return userMapper.toUserResponse(userRepository.findById(userId)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST)));
//    }

}

