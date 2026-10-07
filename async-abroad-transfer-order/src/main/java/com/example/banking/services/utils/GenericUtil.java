package com.example.banking.services.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.banking.services.enums.FormatDateEnum;
import lombok.extern.log4j.Log4j2;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Log4j2
public class GenericUtil {

    public static Date cleanHours(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        return calendar.getTime();
    }

    public static Date convertStringToDate(String dateString,FormatDateEnum formatDateEnum) {
        try {
            return new SimpleDateFormat(formatDateEnum.getName()).parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Error convertStringToDate");
        }
    }

    public static String convertDateToString(Date date,FormatDateEnum formatDateEnum) {

        DateFormat dateFormat = new SimpleDateFormat(formatDateEnum.getName());
        return dateFormat.format(date);

    }

    public static String jsonObjectToString(Object object) {
        String result;
        ObjectMapper objectMapper=new ObjectMapper();
        try {
            result = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting the Java object to json");
        }
        return result;
    }

    public static OffsetDateTime dateToOffsetDateTime(Date date, FormatDateEnum formatDateEnum) {


        // Convierte el objeto Date a Instant y luego a OffsetDateTime utilizando la zona horaria UTC
        OffsetDateTime offsetDateTime = date.toInstant().atOffset(ZoneOffset.UTC);

        // Formatea el OffsetDateTime al formato deseado
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatDateEnum.getName());

        return OffsetDateTime.parse(dateFormat.format(Date.from(offsetDateTime.toInstant())))
                .plusSeconds(1l);
    }

    public static String userKey(String trayOrderId){
        // Obtén la fecha actual
        LocalDate fechaActual = LocalDate.now();
        // Define un patrón de formateo para obtener la fecha en formato numérico (yyyyMMdd)
        DateTimeFormatter formatoNumerico = DateTimeFormatter.ofPattern(FormatDateEnum.yyyyMMdd.getName());
        // Formatea la fecha actual utilizando el patrón
        String fechaNumerica = fechaActual.format(formatoNumerico);
        return ("BBE"+fechaActual.format(formatoNumerico)+trayOrderId+ UUID.randomUUID()
                .toString().replace("-",""))
                .toUpperCase()
                .substring(0, 23);
    }

    public static String removeSpecialCharacters(String value){
        return value.replaceAll("[^a-zA-Z0-9]", "");
    }
}
