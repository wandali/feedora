package com.sample.foo.simplerssreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.net.InternetDomainName;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.model.TreeNode.BaseNodeViewHolder;


class FeedTreeItemHolder extends BaseNodeViewHolder<FeedTreeItemHolder.FeedTreeItem> {

    FeedTreeItemHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, FeedTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.layout_feed_node, null, false);

        /* Date: 03/04/2017
        Incoming #3766
        Wanda: Strip the url to the topPrivateDomain. */
        String strippedUrl = value.url
                .replaceFirst("^(http://www\\.|http://|www\\.|https://www\\.|https://)", "");
        strippedUrl = strippedUrl.substring(0,strippedUrl.indexOf("/"));
        String faviconUrl = "https://www.google.com/s2/favicons?domain=" +
                InternetDomainName.from(strippedUrl).topPrivateDomain().toString();

        /* Date: 03/04/2017
        Incoming #3766
        Wanda: Set favicon. */
        ImageView imageView = (ImageView) view.findViewById(R.id.favicon);
        Glide
                .with(imageView.getContext())
                .load(faviconUrl)
                .centerCrop()
                .into(imageView);

        /* Date: 19/03/2017
        Incoming #3023
        Wanda: Set text. */
        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(value.text);


        return view;
    }

    static class FeedTreeItem {
        public String text;
        public String url;

        FeedTreeItem(String text, String url) {
            this.text = text;
            this.url = url;
        }
    }

    @Override
    public int getContainerStyle() {
        return R.style.FeedContainerStyle;
    }
}
