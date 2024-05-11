package com.example.decoratemycakebackend.global.util;

import java.time.LocalDate;

public class BirthdayUtil {

    public static LocalDate getNextBirthday(LocalDate today, LocalDate birthday) {
        LocalDate thisYearBirthday = LocalDate.of(today.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());
        return today.isBefore(thisYearBirthday) ? thisYearBirthday : thisYearBirthday.plusYears(1);
    }

}
