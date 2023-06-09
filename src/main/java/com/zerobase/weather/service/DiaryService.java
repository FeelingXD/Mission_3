package com.zerobase.weather.service;

import com.zerobase.weather.WeatherApplication;
import com.zerobase.weather.domain.DateWeather;
import com.zerobase.weather.domain.Diary;
import com.zerobase.weather.repository.DateWeatherRepository;
import com.zerobase.weather.repository.DiaryRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DiaryService {

    @Value("${openweathermap.key}")
    private String apiKey;

    @Value("${openweathermap.city}")
    private String city;

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    public DiaryService(DiaryRepository diaryRepository,DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository= diaryRepository ;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public  void saveWeatherDate(){
        //todo
        logger.info("날씨 데이터 삽입 완료");
        dateWeatherRepository.save(getWeatherFromApi());
    }


    @Transactional(isolation = Isolation.SERIALIZABLE )
    public void createDiary(LocalDate date, String text){
        //open weather map에서 날씨 데이터 가져오기
        DateWeather dateWeather=getDateWeather(date);

        //파싱된 데이터 + 일기 값 우리 db에 넣기
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text);

        diaryRepository.save(nowDiary);
        logger.info("end to create diary");
    }
    @Transactional
    @Scheduled(cron = "0/5 0 1 * * *")
    public void saveWeather(){
        dateWeatherRepository.save(getWeatherFromApi());
    }

    private DateWeather getDateWeather(LocalDate date){
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if(dateWeatherListFromDB.size()==0)            // 사이즈 0 일때 값없음 .
            return getWeatherFromApi();
        else
            return dateWeatherListFromDB.get(0);

    }
    private DateWeather getWeatherFromApi(){
        //open weather map 에서 가져오기
        String weatherData = getWeatherString();
        //날씨 json 파싱
        Map<String,Object> parsedWeather = parseWeather(weatherData);

        DateWeather dateWeather=new DateWeather();


        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parsedWeather.get("main").toString());
        dateWeather.setIcon(parsedWeather.get("icon").toString());
        dateWeather.setTemperature((Double)parsedWeather.get("temp"));



        return dateWeather;
    }


    public void updateDiary(LocalDate date,String text){
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
    }
    public void deleteDiary(LocalDate date){
        diaryRepository.deleteAllByDate(date);
    }

    private String getWeatherString(){
        String apiUrl ="http://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while((inputLine=br.readLine()) !=null){
                response.append(inputLine);
            }

            br.close();
            return response.toString();

        } catch (Exception e) {
            return "failed to get response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString){
        /***
         * return MapObj
         */

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try{
            jsonObject = (JSONObject) jsonParser.parse(jsonString);

        }catch (ParseException e){
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);

        resultMap.put("main",weatherData.get("main"));
        resultMap.put("icon" ,weatherData.get("icon"));

        return resultMap;

    }

    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date){
        logger.debug("read diary");
//        if(date.isAfter(LocalDate.ofYearDay(3050,1))){
//            throw new InvalidDate();
//        }
        return diaryRepository.findAllByDate(date);
    }
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate start,LocalDate end){
        return diaryRepository.findAllByDateBetween(start,end);
    }
}
