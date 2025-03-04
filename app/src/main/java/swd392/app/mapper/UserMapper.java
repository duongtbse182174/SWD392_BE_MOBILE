package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import swd392.app.dto.request.UserCreationRequest;
import swd392.app.dto.request.UserUpdateRequest;
import swd392.app.dto.response.UserResponse;
import swd392.app.entity.User;

import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "userCode", target = "userCode")
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "fullName", target = "fullName")
    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    // Ánh xạ userCode -> User (dùng với MapStruct)
    @Named("mapUserCodeToUser")
    default User mapUserCodeToUser(String userCode) {
        if (userCode == null) return null;
        User user = new User();
        user.setUserCode(userCode);
        return user;
    }

}


