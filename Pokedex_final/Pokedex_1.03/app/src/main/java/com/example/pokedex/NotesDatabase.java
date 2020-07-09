package com.example.pokedex;

import androidx.room.Database;
import androidx.room.RoomDatabase;
//存储Note的数据库
@Database(entities = {Note.class}, version = 1)
public abstract class NotesDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();
}
