package com.fyf.service.impl;

import com.fyf.dto.CursorPage;
import com.fyf.dto.MessageDTO;
import com.fyf.entity.Message;
import com.fyf.enums.MessageStatusEnum;
import com.fyf.exception.LoginException;
import com.fyf.exception.MessageException;
import com.fyf.mapper.MessageMapper;
import com.fyf.result.CursorPageResult;
import com.fyf.service.MessageService;
import com.fyf.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private MessageMapper messageMapper;

    /**
     * 滑动分页显示留言
     * @param cursorPage 游标分页参数
     * @return 分页结果（CursorPageResult）
     */
    public CursorPageResult<MessageDTO> messageList(CursorPage cursorPage) {
        Integer pageSize = cursorPage.getPageSize();
        if(pageSize == null || pageSize <=0 || pageSize > 20 ){
            pageSize = 20;
        }
        Long cursorId = cursorPage.getCursorId();
        LocalDateTime cursorTime = cursorPage.getCursorTime();

        boolean hasMore = false;
        List<MessageDTO> list = messageMapper.getMessages(pageSize+1,cursorId,cursorTime,null, MessageStatusEnum.NORMAL.getCode());
        if(list.size()>pageSize){
            hasMore = true;
            list.remove(list.size()-1);
        }

        CursorPageResult<MessageDTO> cursorPageResult = new CursorPageResult<>();
        cursorPageResult.setHasMore(hasMore);
        cursorPageResult.setList(list);
        if(!list.isEmpty()){
            cursorPageResult.setNextCursorId(list.get(list.size()-1).getId());
            cursorPageResult.setNextCursorTime(list.get(list.size()-1).getCreateTime());
        }
        return cursorPageResult;
    }

    /**
     * 发布留言
     * @param messageDTO message具体信息
     */
    public void sendMessage(MessageDTO messageDTO) {
        Long userId = UserContext.getCurrentUserId();
        if(userId == null){
            throw new LoginException("请先登录");
        }

        Message message = new Message();
        BeanUtils.copyProperties(messageDTO, message);
        message.setStatus(MessageStatusEnum.NORMAL.getCode());
        message.setUserId(userId);

        Integer affectedRows = messageMapper.insertMessage(message);
        if(affectedRows != 1){
            throw new MessageException("发布失败");
        }
    }

    /**
     * 删除留言
     * @param messageId 要删除的留言id
     */
    public void deleteMessage(Long messageId) {
        Message message = new Message();
        message.setId(messageId);
        message.setStatus(MessageStatusEnum.DELETE.getCode());
        Integer affectedRows = messageMapper.updateMessage(message);
        if(affectedRows != 1){
            throw new MessageException("删除失败");
        }
    }

    /**
     * 查询某个用户的所有留言
     * @param userId 目标用户id
     * @param cursorPage 分页条件
     * @return 滑动分页结果
     */
    public CursorPageResult<MessageDTO> getMessagesById(Long userId,CursorPage cursorPage) {
        Integer pageSize = cursorPage.getPageSize();
        if(pageSize == null || pageSize <=0 || pageSize > 20 ){
            pageSize = 20;
        }
        Long cursorId = cursorPage.getCursorId();
        LocalDateTime cursorTime = cursorPage.getCursorTime();

        boolean hasMore = false;
        List<MessageDTO> list = messageMapper.getMessages(pageSize+1,cursorId,cursorTime,userId, MessageStatusEnum.NORMAL.getCode());
        if(list.size()>pageSize){
            hasMore = true;
            list.remove(list.size()-1);
        }

        CursorPageResult<MessageDTO> cursorPageResult = new CursorPageResult<>();
        cursorPageResult.setHasMore(hasMore);
        cursorPageResult.setList(list);
        if(!list.isEmpty()){
            cursorPageResult.setNextCursorId(list.get(list.size()-1).getId());
            cursorPageResult.setNextCursorTime(list.get(list.size()-1).getCreateTime());
        }
        return cursorPageResult;
    }


}
