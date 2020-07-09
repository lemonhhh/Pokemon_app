package com.example.pokedex;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

//Note数据库的中间媒介，具有储存，选择，更新的功能
@Dao
public interface NoteDao {
    @Query("INSERT INTO notes (contents) VALUES ('New note')")
    void create();

    @Query("SELECT * FROM notes")
    List<Note> getAllNotes();

    @Query("UPDATE notes SET contents = :contents WHERE id = :id")
    void save(String contents, int id);
}
