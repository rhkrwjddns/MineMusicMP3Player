package com.example.minemusicmp3player;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.CustomViewHolder> {
    private Context context;
    private ArrayList<MusicData> musicList;
    //인터페이스를 멤버변수로 선언
    private OnItemClickListener onItemClickListener = null;

    //생성자
    public MusicAdapter(Context context) {
        this.context = context;
    }

    public void setMusicList(ArrayList<MusicData> musicList) {
        this.musicList = musicList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    //리사이클러 뷰에 들어갈 항목 뷰를 inflater 한다. viewHolder 항목객체관리
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.CustomViewHolder customViewHolder, int position) {
        //앨범이미지를 비트맵으로
        Bitmap albumImg = getAlbumImg(context, Long.parseLong(musicList.get(position).getAlbumArt()), 200);
        if(albumImg != null){
            customViewHolder.albumArt.setImageBitmap(albumImg);
        }
        //recyclerViewer 보여줄 정보 세팅
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        customViewHolder.title.setText(musicList.get(position).getTitle());
        customViewHolder.artist.setText(musicList.get(position).getArtist());
        customViewHolder.duration.setText(simpleDateFormat.format(Integer.parseInt(musicList.get(position).getDuration())));
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
                }
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return (musicList != null) ? musicList.size() : 0;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        //inflater 데이터항목 찾아오기
        ImageView albumArt;
        TextView title;
        TextView artist;
        TextView duration;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.albumArt = itemView.findViewById(R.id.d_ivAlbum);
            this.title = itemView.findViewById(R.id.d_tvTitle);
            this.artist = itemView.findViewById(R.id.d_tvArtist);
            this.duration = itemView.findViewById(R.id.d_tvDuration);

            //추상화메소드구현
            itemView.setOnClickListener(view->{
                int position = getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                    onItemClickListener.onItemClick(view, position);
                }
            });
        }
    }

    //인터페이스 구현 setOnItemClickListener -> setters 함수
    public interface OnItemClickListener{
        //2.추상화 메소드 선언
        void onItemClick(View view, int position);
    }
}
