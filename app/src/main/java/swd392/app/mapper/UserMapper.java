package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import swd392.app.dto.request.UserCreationRequest;
import swd392.app.dto.request.UserUpdateRequest;
import swd392.app.dto.response.UserResponse;
import swd392.app.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}

