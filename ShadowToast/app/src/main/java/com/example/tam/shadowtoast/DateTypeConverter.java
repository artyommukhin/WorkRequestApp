package com.example.tam.shadowtoast;

import android.arch.persistence.room.*;

import java.util.Date;

public class DateTypeConverter {
    @TypeConverter
    public Date toDate(Long value){
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long toLong(Date value){
        return value == null ? null : value.getTime();
    }
}
