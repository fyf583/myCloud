package com.fyf.controller;

import com.fyf.dto.CursorPage;
import com.fyf.dto.MessageDTO;
import com.fyf.result.CursorPageResult;
import com.fyf.result.Result;
import com.fyf.service.MessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/message")
public class MessageController {
    @Resource
    private MessageService messageService;

    @GetMapping
    public Result<CursorPageResult<MessageDTO>> messageList(CursorPage cursorPage) {
        CursorPageResult<MessageDTO> pageResult = messageService.messageList(cursorPage);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Void> sendMessage(@RequestBody MessageDTO messageDTO) {
        messageService.sendMessage(messageDTO);
        return Result.success();
    }

    @PostMapping("/{messageId}")
    public Result<Void> deleteMessage(@PathVariable("messageId") Long messageId) {
        messageService.deleteMessage(messageId);
        return Result.success();
    }

    @GetMapping("/{userId}")
    public Result<CursorPageResult<MessageDTO>> getMessagesById(@PathVariable("userId") Long userId, CursorPage cursorPage) {
        CursorPageResult<MessageDTO> result = messageService.getMessagesById(userId,cursorPage);
        return Result.success(result);
    }
}
