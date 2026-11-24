package com.fyf.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CursorPage {
    private Integer pageSize;

    private Integer cursorId;

    private LocalDateTime cursorTime;
}
