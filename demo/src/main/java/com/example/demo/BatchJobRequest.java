package com.example.demo;

import lombok.Data;

import java.util.List;

@Data
public class BatchJobRequest {
    private Long batchId;              // 배치 ID (업데이트할 배치의 ID)
    private String batchName;          // 배치명
    private String jobName;            // Job 이름
    private String batchFrequency;     // 배치 주기
    private String frequencyUnit;      // 주기 단위
    private String batchTime;          // 배치 시간 (HH:MM:SS 형식)
    private String daysOfWeek;         // 실행 요일
    private String daysOfMonth;        // 실행 날짜
    private boolean isAdw;             // ADW 여부
}
