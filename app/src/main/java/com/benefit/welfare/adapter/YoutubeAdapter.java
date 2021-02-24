package com.benefit.welfare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.benefit.welfare.Data.YoutubeItem;
import com.benefit.welfare.R;

import java.util.List;

public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeAdapter.YoutubeViewHolder>
{
    private Context context;
    private List<YoutubeItem> list;

    String video_id;

    public YoutubeAdapter(Context context, List<YoutubeItem> list)
    {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public YoutubeAdapter.YoutubeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.youtube_item, parent, false);
        return new YoutubeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YoutubeAdapter.YoutubeViewHolder holder, int position)
    {
        YoutubeItem dto = list.get(position);
        holder.youtube_player_title.setText(dto.getTitle());
        holder.youtube_url_id.setText(dto.getUrl_id());
        Log.e("onBindViewHolder()", "youtube_url_id = " + dto.getUrl_id());
        for (int i = 0; i < list.size(); i++)
        {
            holder.youtube_player.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
            {
                @Override
                public void onVideoId(YouTubePlayer youTubePlayer, String videoId)
                {
                    videoId = dto.getUrl_id();
                    Log.e("bbb", "video_id : " + videoId);
                    youTubePlayer.cueVideo(videoId, 0);
                }
            });
        }
//        holder.youtube_player.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
//        {
//            @Override
//            public void onReady(YouTubePlayer youTubePlayer)
//            {
//                switch (dto.getUrl_id())
//                {
//                    case "0" :
//                        video_id = String.valueOf(list.get(0));
//                        youTubePlayer.cueVideo(video_id, 0);
//                        youTubePlayer.pause();
//                        break;
//                }
//            }
//        });
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class YoutubeViewHolder extends RecyclerView.ViewHolder
    {
        YouTubePlayerView youtube_player;
        TextView youtube_player_title, youtube_url_id;

        public YoutubeViewHolder(@NonNull View view)
        {
            super(view);

            youtube_player = view.findViewById(R.id.youtube_player);
            youtube_player_title = view.findViewById(R.id.youtube_player_title);
            youtube_url_id = view.findViewById(R.id.youtube_url_id);
        }
    }
}
