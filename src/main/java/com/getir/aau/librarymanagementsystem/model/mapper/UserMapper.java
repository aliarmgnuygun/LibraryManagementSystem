package com.getir.aau.librarymanagementsystem.model.mapper;

import com.getir.aau.librarymanagementsystem.model.dto.request.UserUpdateRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.UserResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().getName().name())")
    UserResponseDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    User fromRegisterRequest(RegisterRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "role", ignore = true)
    void updateUserFromDto(UserUpdateRequestDto dto, @MappingTarget User user);
}