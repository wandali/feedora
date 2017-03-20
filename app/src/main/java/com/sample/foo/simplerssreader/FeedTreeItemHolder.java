package com.sample.foo.simplerssreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.model.TreeNode.BaseNodeViewHolder;

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
        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(value.text);

        return view;
    }

    static class IconTreeItem {
        public String text;

        IconTreeItem(String text) {
            this.text = text;
        }
    }
}
