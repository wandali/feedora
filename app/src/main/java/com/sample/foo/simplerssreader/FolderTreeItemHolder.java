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
    private IconicsImageView arrowIcon;
    private View.OnClickListener clickListener;

    FolderTreeItemHolder(Context context, View.OnClickListener clickListener) {
        super(context);
        this.clickListener = clickListener;
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
        arrowIcon = (IconicsImageView) view.findViewById(R.id.icon);
        arrowIcon.setIcon("gmd-keyboard_arrow_down");

        IconicsImageView editIcon = (IconicsImageView) view.findViewById(R.id.editIcon);
        editIcon.setOnClickListener(clickListener);

        return view;
    }

    @Override
    public void toggle(boolean active) {
        if (active) {
            arrowIcon.setIcon("gmd-keyboard_arrow_up");
        } else {
            arrowIcon.setIcon("gmd-keyboard_arrow_down");
        }
    }

    static class IconTreeItem {
        public String text;

        IconTreeItem(String text) {
            this.text = text;
        }
    }

    @Override
    public int getContainerStyle() {
        return R.style.FeedContainerStyle;
    }
}
