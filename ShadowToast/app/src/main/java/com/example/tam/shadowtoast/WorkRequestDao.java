package com.example.tam.shadowtoast;

import android.arch.persistence.room.*;
import android.support.annotation.WorkerThread;
import java.util.List;

@Dao
public interface WorkRequestDao {
    @WorkerThread
    @Insert
    void insertAll(WorkRequest... workRequests);

    @Delete
    void delete(WorkRequest workRequest);

    @Query("DELETE FROM workrequest" )
    void deleteAll();

    @Update
    void update(WorkRequest workRequest);

    @Query("UPDATE workrequest SET complete = 0")
    void dropAllRequests();

    @Query("SELECT * FROM workrequest")
    List<WorkRequest> getAllWorkRequests();
}
