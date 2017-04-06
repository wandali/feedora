package com.sample.foo.simplerssreader;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

class RssFeedListAdapter
        extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

    private List<RssFeedModel> mRssFeedModels;

    static class FeedModelViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private View rssFeedView;
        private final Context context;
        private RssFeedModel model;

        FeedModelViewHolder(View v) {
            super(v);
            rssFeedView = v;
            context = v.getContext();
            v.setOnClickListener(this);
        }

        /* Date: 16/02/2017
         Wanda: Load the article when clicked */
        @Override
        public void onClick(View view) {
            final Intent intent = new Intent(context, ArticleViewActivity.class);
            intent.putExtra("link", model.link);
            context.startActivity(intent);
        }
    }

    RssFeedListAdapter(List<RssFeedModel> rssFeedModels) {
        mRssFeedModels = rssFeedModels;
    }

    @Override
    public FeedModelViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_rss_feed, parent, false);
        return new FeedModelViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FeedModelViewHolder holder, int position) {
        final RssFeedModel rssFeedModel = mRssFeedModels.get(position);
        holder.model = rssFeedModel;

        ((TextView) holder.rssFeedView.findViewById(R.id.titleText)).setText(rssFeedModel.title);
        ((TextView) holder.rssFeedView.findViewById(R.id.descriptionText)).setText(rssFeedModel.description);

        ImageView imageView = (ImageView) holder.rssFeedView.findViewById(R.id.thumbnail);
        if (rssFeedModel.thumbnailUri != null) {
            /* Date: 16/02/2017
            Wanda: If we have a thumbnail for this item load it in. */
            Glide
                    .with(imageView.getContext())
                    .load(rssFeedModel.thumbnailUri)
                    .centerCrop()
                    .into(imageView);
        } else {
            /* Date: 16/02/2017
               Wanda: If we don't have a thumbnail clear the imageView. */
            Glide
                    .clear(imageView);
            imageView.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return mRssFeedModels.size();
    }
}

