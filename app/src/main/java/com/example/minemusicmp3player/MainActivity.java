package com.example.minemusicmp3player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FrameLayout frameLayout;
    private RecyclerView recyclerView;
    private RecyclerView recyclerLike;

    //musicDataArrayList
    private ArrayList<MusicData> musicDataArrayList = new ArrayList<>();
    private ArrayList<MusicData> likeDataArrayList = new ArrayList<>();

    private MusicAdapter musicAdapter, musicAdapterLike;
    private Player player;

    //DB 객체참조변수
    private MusicDBHelper musicDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewByIdFunc();

        //외부접근권한 설정
        requestPermissionsFunc();

        //어뎁터 생성
        musicAdapter = new MusicAdapter(getApplicationContext());
        musicAdapterLike = new MusicAdapter(getApplicationContext());

        //recyclerView 에서 리니어레이아웃메니저를 적용시켜야 된다.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        LinearLayoutManager likeLayoutManager = new LinearLayoutManager(getApplicationContext());


        //recyclerView 에 리니어레이아웃메니저를 적용
        recyclerView.setAdapter(musicAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerLike.setAdapter(musicAdapter);
        recyclerLike.setLayoutManager(likeLayoutManager);

        //DB생성
        musicDBHelper = MusicDBHelper.getInstance(getApplicationContext());

        //ArrayList<MusicData>를 가져와서 musicAdapter 적용시키기
        musicDataArrayList = findMusic();
        musicDBHelper.insertMusicDataToDB(musicDataArrayList);
        likeDataArrayList = musicDBHelper.selectLikeTBL();

        musicAdapter.setMusicList(musicDataArrayList);
        musicAdapter.notifyDataSetChanged();
        musicAdapterLike.setMusicList(likeDataArrayList);
        musicAdapterLike.notifyDataSetChanged();


        //DB 에 저장
        musicDBHelper = MusicDBHelper.getInstance(getApplicationContext());

        //recyclerView 에 나오는 노래를 저장
        musicDBHelper.insertMusicDataToDB(musicDataArrayList);


        //인터페이스 구현 못한다면 여기까지가 한계
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                player.setPlayerData(position,true);
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        musicAdapterLike.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                player.setPlayerData(position, true);
                drawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });


        //현재 액티비티있는 프레임 레이아웃에 프레그먼트 지정
        rePlaceFrag();

    }



    //sdCard 안의 음악 검색
    public ArrayList<MusicData> findMusic() {
        ArrayList<MusicData> sdCardList = new ArrayList<>();

        String[] data = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        //특정 폴더에서 음악 가져오기
        //String selection = mediaStore.Audio.Media.DATA + "like ? ";
        //String selectionArgs = new String[]{%MusicList%}

        //전체영역에서 음악 가져오기
        Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                data, null, null, data[2] + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                //음악데이터 가져오기
                String id = cursor.getString(cursor.getColumnIndex(data[0]));
                String artist = cursor.getString(cursor.getColumnIndex(data[1]));
                String title = cursor.getString(cursor.getColumnIndex(data[2]));
                String albumArt = cursor.getString(cursor.getColumnIndex(data[3]));
                String duration = cursor.getString(cursor.getColumnIndex(data[4]));

                MusicData mData = new MusicData(id, artist, title, albumArt, duration, 0, 0);
                sdCardList.add(mData);
            }
        }
        return sdCardList;
    }

    //외부파일에 접근하기위한 허용요청
    private void requestPermissionsFunc() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
    }

    private void rePlaceFrag() {
        //프래그먼트 생성
        player = new Player();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, player);
        ft.commit();
    }

    private void findViewByIdFunc() {
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerLike = (RecyclerView) findViewById(R.id.recyclerLike);
    }

    public ArrayList<MusicData> getMusicDataArrayList() {
        return musicDataArrayList;
    }

    public MusicDBHelper getMusicDBHelper() {
        return musicDBHelper;
    }
}