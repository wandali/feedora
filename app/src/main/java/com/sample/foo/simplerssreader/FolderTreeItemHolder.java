package com.sample.foo.simplerssreader;

import android.annotation.SuppressLint;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.model.TreeNode.BaseNodeViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

class FolderTreeItemHolder extends BaseNodeViewHolder<FolderTreeItemHolder.IconTreeItem> {
    private IconicsImageView arrowView;

    FolderTreeItemHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, FolderTreeItemHolder.IconTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.layout_folder_node, null, false);

        /* Date: 19/04/2017
        Incoming #3010
        Wanda: Set text. */
        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(value.text);

        /* Date: 19/04/2017
        Incoming #3010
        Wanda: Get icon view. */
        arrowView = (IconicsImageView) view.findViewById(R.id.icon);
        arrowView.setIcon("gmd-keyboard_arrow_down");

        return view;
    }

    @Override
    public void toggle(boolean active) {
        if (active) {
            arrowView.setIcon("gmd-keyboard_arrow_up");
        } else {
            arrowView.setIcon("gmd-keyboard_arrow_down");
        }
    }

    static class IconTreeItem {
        public String text;

        IconTreeItem(String text) {
            this.text = text;
        }
    }
}
