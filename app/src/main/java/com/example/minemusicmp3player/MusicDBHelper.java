package com.example.minemusicmp3player;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MusicDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "musicDB";
    private static final int VERSION = 1;
    private Context context;

    //2.싱글톤 자신의 객체멤버 갖기
    private static MusicDBHelper musicDBHelper;

    //DB 생성
    private MusicDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context;
    }

    //1. 싱글톤 생성 1단계
    public static MusicDBHelper getInstance(Context context) {
        if (musicDBHelper == null) {
            musicDBHelper = new MusicDBHelper(context);
        }
        return musicDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE musicTBL(" +
                        "id VARCHAR(15) PRIMARY KEY," +
                        "artist VARCHAR(15)," +
                        "title VARCHAR(15)," +
                        "albumArt VARCHAR(15)," +
                        "duration VARCHAR(15)," +
                        "click INTEGER," +
                        "liked INTEGER );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists musicTBL");
        onCreate(sqLiteDatabase);
    }

    // DB 선택(select)
    public ArrayList<MusicData> selectMusicTBL() {

        ArrayList<MusicData> musicDBArrayList = new ArrayList<>();


        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        // 쿼리문 입력하고 커서 리턴 받음
        Cursor cursor = sqLiteDatabase.rawQuery("select * from musicTBL;", null);

        while (cursor.moveToNext()) {
            MusicData musicData = new MusicData(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5),
                    cursor.getInt(6));

            musicDBArrayList.add(musicData);
        }

        cursor.close();
        //  sqLiteDatabase.close();

        return musicDBArrayList;
    }

    // DB 선택(select)
//    public MusicData selectMusicTBLMusicData(MusicData data) {
//
//        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
//
//        // 쿼리문 입력하고 커서 리턴 받음
//        Cursor cursor = sqLiteDatabase.rawQuery("select * from musicTBL WHERE id ='" + data.getId() + "';", null);
//
//        MusicData musicData = null;
//        if (cursor.moveToNext()) {
//            musicData = new MusicData(
//                    cursor.getString(0),
//                    cursor.getString(1),
//                    cursor.getString(2),
//                    cursor.getString(3),
//                    cursor.getString(4),
//                    cursor.getInt(5),
//                    cursor.getInt(6));
//
//            cursor.close();
//            sqLiteDatabase.close();
//            return musicData;
//        }
//
//        cursor.close();
//        // sqLiteDatabase.close();
//
//        return musicData;
//    }

    // DB 삽입(insert)
    public boolean insertMusicDataToDB(ArrayList<MusicData> arrayList) {

        boolean returnValue = false;
        //insert 하기위한 SQLiteDatabase
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        try {

            // DB 에서 리스트 가져오기
            ArrayList<MusicData> dbList = selectMusicTBL();
            if (dbList.size() == 0) {
                for (MusicData data : arrayList) {
                    String title1 = data.getTitle().replace("'", "_");
                    data.setTitle(title1);
                    String query = "insert into musicTBL values("
                            + "'" + data.getId() + "',"
                            + "'" + data.getArtist() + "',"
                            + "'" + data.getTitle() + "',"
                            + "'" + data.getAlbumArt() + "',"
                            + "'" + data.getDuration() + "',"
                            + data.getClick() + "," + data.getLiked() + ");";
                    // 쿼리문 작성해서 넘김
                    // 예외발생시 SQLException
                    sqLiteDatabase.execSQL(query);
                }
            } else {
                for (MusicData data : arrayList) {
                    if (!dbList.contains(data)) {
                        String title1 = data.getTitle().replace("'", "_");
                        data.setTitle(title1);

                        String query = "insert into musicTBL values("
                                + "'" + data.getId() + "',"
                                + "'" + data.getArtist() + "',"
                                + "'" + data.getTitle() + "',"
                                + "'" + data.getAlbumArt() + "',"
                                + "'" + data.getDuration() + "',"
                                + data.getClick() + "," + data.getLiked() + ");";

                        // 쿼리문 작성해서 넘김
                        // 예외발생시 SQLException
                        sqLiteDatabase.execSQL(query);
                    }
                }
            }

            returnValue = true;
        } catch (Exception e) {
            returnValue = false;
        }
        return returnValue;
    }

    //DB 수정(update)
    public boolean updateMusicDataToDB(MusicData data) {
        boolean returnValue = false;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        try {
            if (data != null) {
                String query = "UPDATE musicTBL SET click = " + data.getClick() + ", liked = " + data.getLiked() +
                        " WHERE id = '" + data.getId() + "';";
                sqLiteDatabase.execSQL(query);
            }
            returnValue = true;
        } catch (Exception e) {
            return false;
        }
        return returnValue;
    }
}
