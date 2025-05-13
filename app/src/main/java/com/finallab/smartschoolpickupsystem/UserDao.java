package com.finallab.smartschoolpickupsystem;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    UserEntity getUserById(String uid);
}
