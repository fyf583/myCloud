package com.fyf.mapper;

import com.fyf.dto.MessageDTO;
import com.fyf.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessageMapper {
    List<MessageDTO> getMessages(Integer pageSize, Long cursorId, LocalDateTime cursorTime,Long userId,Integer status);

    Integer insertMessage(Message message);

    Integer updateMessage(Message message);

}
