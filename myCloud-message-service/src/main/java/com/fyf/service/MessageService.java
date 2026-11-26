package com.fyf.service;

import com.fyf.dto.CursorPage;
import com.fyf.dto.MessageDTO;
import com.fyf.result.CursorPageResult;

public interface MessageService {
    CursorPageResult<MessageDTO> messageList(CursorPage cursorPage);

    void sendMessage(MessageDTO messageDTO);

    void deleteMessage(Long messageId);

    CursorPageResult<MessageDTO> getMessagesById(Long userId,CursorPage cursorPage);
}
