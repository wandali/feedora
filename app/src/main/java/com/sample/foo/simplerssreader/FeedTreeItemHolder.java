package com.sample.foo.simplerssreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.model.TreeNode.BaseNodeViewHolder;

import java.net.URL;

class FeedTreeItemHolder extends BaseNodeViewHolder<FeedTreeItemHolder.IconTreeItem> {
    FeedTreeItemHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, FeedTreeItemHolder.IconTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.layout_feed_node, null, false);

        /* Date: 19/04/2017
        Incoming #3023
        Wanda: Set text. */


        String urlToLoad;

        try {
            URL url = new URL(value.url);

            Log.d("YOUR STRING HERE", "YOUR PRINT STATEMENT BRUHHH " + url.getAuthority()+ "/favicon.ico");
            ImageView imageView = (ImageView) view.findViewById(R.id.favicon);

            urlToLoad = url.getAuthority()+ "/favicon.ico";

            Glide
                    .with(imageView.getContext())
                    .load(Uri.parse(urlToLoad))
                    .centerCrop()
                    .into(imageView);
            Log.d("YOU MADE IT", "made it");
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(value.text);


        return view;

    }

    static class IconTreeItem {
        public String text;
        public String url;

        IconTreeItem(String text, String url) {
            this.text = text;
            this.url = url;
        }
    }

    @Override
    public int getContainerStyle() {
        return R.style.FeedContainerStyle;
    }
}
