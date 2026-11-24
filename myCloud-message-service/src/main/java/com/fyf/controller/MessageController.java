package com.fyf.controller;

import com.fyf.dto.MessageDTO;
import com.fyf.result.CursorPageResult;
import com.fyf.result.Result;
import com.fyf.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping()
    public Result<CursorPageResult<MessageDTO>> messageList(@RequestBody com.fyf.dto.CursorPage cursorPage) {
        messageService.messageList(cursorPage);
        return null;
    }
}
