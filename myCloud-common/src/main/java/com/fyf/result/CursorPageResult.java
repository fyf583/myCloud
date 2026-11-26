package com.fyf.result;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CursorPageResult <T>{
    private Long nextCursorId;

    private LocalDateTime nextCursorTime;

    private Boolean hasMore;

    private List<T>  list;
}
