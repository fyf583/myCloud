package com.fyf.mapper;

import com.fyf.dto.MessageDTO;
import com.fyf.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessageMapper {
    List<MessageDTO> getMessages(@Param("pageSize") Integer pageSize,
                                 @Param("cursorId") Long cursorId,
                                 @Param("cursorTime") LocalDateTime cursorTime,
                                 @Param("userId") Long userId,
                                 @Param("status") Integer status);

    Integer insertMessage(Message message);

    Integer updateMessage(Message message);

}
