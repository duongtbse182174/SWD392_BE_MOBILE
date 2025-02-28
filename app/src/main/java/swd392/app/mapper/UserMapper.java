package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import swd392.app.dto.request.UserCreationRequest;
import swd392.app.dto.request.UserUpdateRequest;
import swd392.app.dto.response.UserResponse;
import swd392.app.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userId", ignore = true)
//    @Mapping(target = "userName", ignore = true)
//    @Mapping(target = "userCode", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(source = "userId", target = "id")  // Đảm bảo ID được map chính xác
    @Mapping(source = "userCode", target = "userCode")
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "fullName", target = "fullName")
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}

