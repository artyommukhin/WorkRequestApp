package com.example.tam.shadowtoast;

import android.arch.persistence.room.*;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
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

    @Query("DELETE FROM workrequest WHERE id = :id")
    void deleteById(int id);

    @Update
    void update(WorkRequest workRequest);

    @Query("SELECT * FROM workrequest")
    List<WorkRequest> getAllWorkRequests();

    @Query("SELECT * FROM workrequest WHERE complete = 0")
    List<WorkRequest> getAllIncompleteWorkRequests();

    @Query("SELECT * FROM workrequest WHERE complete = 1")
    List<WorkRequest> getAllCompleteWorkRequests();
}
