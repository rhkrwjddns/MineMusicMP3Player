package com.example.minemusicmp3player;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Player extends Fragment implements View.OnClickListener{
    private ImageView ivAlbum;
    private TextView tvClick, tvArtist, tvTitle, tvCurrentTime, tvDuration;
    private SeekBar seekBar;
    private ImageButton ibPlay, ibPrevious, ibNext;

    //프래그먼트에서 장착된 액티비티 가져오기 가ㅑ능 (getActivity())
    //노래를 등록하기 위한 선언 객체변수
    private MainActivity mainActivity;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    //노래 위치지정
    private int index;
    private MusicData musicData = new MusicData();

    //Context (화면+클래스)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mainActivity = (MainActivity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.player, container, false);
        findViewByIdFunc(view);

        return view;
    }

    private void findViewByIdFunc(View view) {
        ivAlbum = view.findViewById(R.id.ivAlbum);
        tvClick = view.findViewById(R.id.tvClick);
        tvArtist = view.findViewById(R.id.tvArtist);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvCurrentTime = view.findViewById(R.id.tvPlaytime);
        tvDuration = view.findViewById(R.id.tvDuration);
        seekBar = view.findViewById(R.id.seekBar);
        ibPlay = view.findViewById(R.id.ibPlay);
        ibPrevious = view.findViewById(R.id.ibPrevious);
        ibNext = view.findViewById(R.id.ibNext);

        ibPlay.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibNext.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) mediaPlayer.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.ibPlay:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    ibPlay.setImageResource(R.drawable.ic_play);
                }else{
                    mediaPlayer.start();
                    ibPlay.setImageResource(R.drawable.ic_pause);
                    setSeekBarThread();
                }
                break;
            case R.id.ibPrevious:
                mediaPlayer.stop();
                mediaPlayer.reset();
                index = (index == 0) ? mainActivity.getMusicDataArrayList().size()-1 : index-1;
                setPlayerData(index);
                ibPlay.setImageResource(R.drawable.ic_pause);
                break;
            case R.id.ibNext:
                mediaPlayer.stop();
                mediaPlayer.reset();
                index = (index == mainActivity.getMusicDataArrayList().size()-1) ? 0 : index+1;
                setPlayerData(index);
                ibPlay.setImageResource(R.drawable.ic_pause);
            break;
            default: break;
        }
    }

    private void setSeekBarThread() {

        Thread thread = new Thread(new Runnable() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

            @Override
            public void run() {
                while (mediaPlayer.isPlaying()) {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            tvCurrentTime.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                        }
                    });
                    SystemClock.sleep(16);
                }//end of while

            }
        });

        thread.start();

    }

    //recyclerView 에서 아이템 선택하면 해당 위치의 선택내용 나옴.
    public void setPlayerData(int position) {
        index= position;
        mediaPlayer.stop();
        mediaPlayer.reset();

        musicData = mainActivity.getMusicDataArrayList().get(index);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        tvTitle.setText(musicData.getTitle());
        tvArtist.setText(musicData.getArtist());
        tvClick.setText(String.valueOf(musicData.getClick()));
        tvDuration.setText(simpleDateFormat.format(Integer.parseInt(musicData.getDuration())));

        //앨범 이미지 세팅
        Bitmap albumImg =getAlbumImg(mainActivity, Long.parseLong(musicData.getAlbumArt()), 200);
        if(albumImg !=null){
            ivAlbum.setImageBitmap(albumImg);
        }else{
            ivAlbum.setImageResource(R.drawable.img_cd);
        }

        //음악재생
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,musicData.getId());
        try {
            mediaPlayer.setDataSource(mainActivity, musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(musicData.getDuration()));
            ibPlay.setImageResource(R.drawable.ic_pause);

            setSeekBarThread();

            //한 곡 완료 후 발생하는 이벤트리스너
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    musicData.setClick(musicData.getClick() + 1);
                    ibNext.callOnClick();
                }
            });
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    //앨범사진 아이디와 앨범사이즈
    private Bitmap getAlbumImg(Context context, long albumArt, int imgMaxSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        ContentResolver contentResolver = context.getContentResolver();

        // 앨범아트는 uri를 제공하지 않으므로, 별도로 생성.
        Uri uri = Uri.parse("content://media/external/audio/albumart/"+albumArt);
        if (uri != null){
            ParcelFileDescriptor fd = null;
            try{
                fd = contentResolver.openFileDescriptor(uri, "r");

                //true면 비트맵객체에 메모리를 할당하지 않아서 비트맵을 반환하지 않음.
                //다만 options fields는 값이 채워지기 때문에 Load 하려는 이미지의 크기를 포함한 정보들을 얻어올 수 있다.
                //체크안해도 되는 문장, options.inJustDecodeBounds = false; 앞문장까지


                options.inJustDecodeBounds = false; // false 비트맵을 만들고 해당이미지의 가로, 세로, 중심으로 가져옴
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);

                if(bitmap != null){
                    // 정확하게 사이즈를 맞춤
                    if(options.outWidth != imgMaxSize || options.outHeight != imgMaxSize){
                        Bitmap tmp = Bitmap.createScaledBitmap(bitmap, imgMaxSize, imgMaxSize, true);
                        bitmap.recycle();
                        bitmap = tmp;
                    }
                }
                return bitmap;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
