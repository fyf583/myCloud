package com.fyf.service.impl;

import com.fyf.dto.CursorPage;
import com.fyf.mapper.MessageMapper;
import com.fyf.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    /**
     * 
     * @param cursorPage
     */
    public void messageList(CursorPage cursorPage) {

    }
}
