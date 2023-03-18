package com.zerobase.weather.controller;

import com.zerobase.weather.domain.Diary;
import com.zerobase.weather.service.DiaryService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService){
        this.diaryService=diaryService;
    }

    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB 일기 저장", notes = "이것은 노트")
    @PostMapping("/create/diary")
    void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestBody String text){
        diaryService.createDiary(date,text);
    }

    @ApiOperation("선택한 날짜의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diary")
    public List<Diary> readDiary(@RequestParam @DateTimeFormat(iso =DateTimeFormat.ISO.DATE) LocalDate date){
//        if(date.isAfter(LocalDate.ofYearDay(3050,1))){
//            throw new InvalidDate();
//        }
        return diaryService.readDiary(date);
    }

    @GetMapping("/read/diaries")
    List<Diary> readDiaries(@RequestParam @DateTimeFormat(iso= DateTimeFormat.ISO.DATE) @ApiParam(value = "조회할 기간의 첫번째날입니다.",example = "2020-01-02") LocalDate startDate,
                            @RequestParam @DateTimeFormat(iso= DateTimeFormat.ISO.DATE) @ApiParam(value = "조회할 기간의 마지막날입니다.",example = "2020-02-02") LocalDate endDate){
        return diaryService.readDiaries(startDate,endDate);
    }

    @PutMapping("/update/diary")
    void updateDiary(@RequestParam @DateTimeFormat(iso= DateTimeFormat.ISO.DATE) LocalDate date,@RequestBody String text){
        diaryService.updateDiary(date,text);
    }

    @DeleteMapping("/delete/diary")
    void deleteDiary(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date){
        diaryService.deleteDiary(date);
    }


}
