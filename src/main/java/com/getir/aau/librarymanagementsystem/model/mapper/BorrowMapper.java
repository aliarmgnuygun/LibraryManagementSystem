package com.getir.aau.librarymanagementsystem.model.mapper;

import com.getir.aau.librarymanagementsystem.model.dto.BorrowItemResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.BorrowRecordResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.BorrowItem;
import com.getir.aau.librarymanagementsystem.model.entity.BorrowRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BorrowMapper {

    BorrowMapper INSTANCE = Mappers.getMapper(BorrowMapper.class);

    // BorrowItem Mappings
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    BorrowItemResponseDto toItemDto(BorrowItem item);

    List<BorrowItemResponseDto> toItemDtoList(List<BorrowItem> items);

    // BorrowRecord Mappings
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", expression = "java(record.getUser().getFirstName() + \" \" + record.getUser().getLastName())")
    BorrowRecordResponseDto toRecordDto(BorrowRecord record);

    List<BorrowRecordResponseDto> toRecordDtoList(List<BorrowRecord> records);
}