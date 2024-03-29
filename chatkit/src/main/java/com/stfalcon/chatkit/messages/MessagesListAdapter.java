/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.stfalcon.chatkit.messages;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stfalcon.chatkit.R;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.ViewHolder;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Adapter for {@link MessagesList}.
 */
@SuppressWarnings("WeakerAccess")
public class MessagesListAdapter<MESSAGE extends IMessage>
        extends RecyclerView.Adapter<ViewHolder>
        implements RecyclerScrollMoreListener.OnLoadMoreListener {

    private MessageHolders holders;
    private String senderId;
    private List<Wrapper> items;

    private int selectedItemsCount;
    private SelectionListener selectionListener;

    protected static boolean isSelectionModeEnabled;

    private OnLoadMoreListener loadMoreListener;
    private OnMessageClickListener<MESSAGE> onMessageClickListener;
    private OnMessageViewClickListener<MESSAGE> onMessageViewClickListener;
    private OnMessageLongClickListener<MESSAGE> onMessageLongClickListener;
    private OnMessageViewLongClickListener<MESSAGE> onMessageViewLongClickListener;
    private ImageLoader imageLoader;
    private RecyclerView.LayoutManager layoutManager;
    private MessagesListStyle messagesListStyle;
    private DateFormatter.Formatter dateHeadersFormatter;
    private SparseArray<OnMessageViewClickListener> viewClickListenersArray = new SparseArray<>();

    /**
     * For default list item layout and view holder.
     *
     * @param senderId    identifier of sender.
     * @param imageLoader image loading method.
     */
    public MessagesListAdapter(String senderId, ImageLoader imageLoader) {
        this(senderId, new MessageHolders(), imageLoader);
    }

    /**
     * For default list item layout and view holder.
     *
     * @param senderId    identifier of sender.
     * @param holders     custom layouts and view holders. See {@link MessageHolders} documentation for details
     * @param imageLoader image loading method.
     */
    public MessagesListAdapter(String senderId, MessageHolders holders,
                               ImageLoader imageLoader) {
        this.senderId = senderId;
        this.holders = holders;
        this.imageLoader = imageLoader;
        this.items = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return holders.getHolder(parent, viewType, messagesListStyle);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Wrapper wrapper = items.get(position);
        holders.bind(holder, wrapper.item, wrapper.isSelected, imageLoader,
                getMessageClickListener(wrapper),
                getMessageLongClickListener(wrapper),
                viewClickListenersArray);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return holders.getViewType(items.get(position).item, senderId);
    }

    @Override
    public void onLoadMore(int page, int total) {
        if (loadMoreListener != null) {
            loadMoreListener.onLoadMore(page, total);
        }
    }

    /*
    * PUBLIC METHODS
    * */

    /**
     * Adds message to bottom of list and scroll if needed.
     *
     * @param message message to add.
     * @param scroll  {@code true} if need to scroll list to bottom when message added.
     */
    public void addToStart(MESSAGE message, boolean scroll) {

        items.add(0, new Wrapper<>(message.getCreatedAt()));
        Wrapper<MESSAGE> element = new Wrapper<>(message);
        items.add(0, element);
        notifyItemRangeInserted(0, 2);
        if (layoutManager != null && scroll) {
            layoutManager.scrollToPosition(0);
        }
    }

    /**
     * Adds messages list in chronological order. Use this method to add history.
     *
     * @param messages messages from history.
     * @param reverse  {@code true} if need to reverse messages before adding.
     */
    public void addToEnd(List<MESSAGE> messages, boolean reverse) {
        if (reverse) Collections.reverse(messages);

        if (!items.isEmpty()) {
            int lastItemPosition = items.size() - 1;
            String lastItem = (String) items.get(lastItemPosition).item;
            items.remove(lastItemPosition);
            notifyItemRemoved(lastItemPosition);
        }

        int oldSize = items.size();
        generateDateHeaders(messages);
        notifyItemRangeInserted(oldSize, items.size() - oldSize);
    }

    /**
     * Updates message by its id.
     *
     * @param message updated message object.
     */
    public void update(MESSAGE message) {
        update(message.getId(), message);
    }

    /**
     * Updates message by old identifier (use this method if id has changed). Otherwise use {@link #update(IMessage)}
     *
     * @param oldId      an identifier of message to update.
     * @param newMessage new message object.
     */
    public void update(String oldId, MESSAGE newMessage) {
        int position = getMessagePositionById(oldId);
        if (position >= 0) {
            Wrapper<MESSAGE> element = new Wrapper<>(newMessage);
            items.set(position, element);
            notifyItemChanged(position);
        }
    }

    /**
     * Deletes message.
     *
     * @param message message to delete.
     */
    public void delete(MESSAGE message) {
        deleteById(message.getId());
    }

    /**
     * Deletes messages list.
     *
     * @param messages messages list to delete.
     */
    public void delete(List<MESSAGE> messages) {
        for (MESSAGE message : messages) {
            int index = getMessagePositionById(message.getId());
            items.remove(index);
            notifyItemRemoved(index);
        }
        recountDateHeaders();
    }

    /**
     * Deletes message by its identifier.
     *
     * @param id identifier of message to delete.
     */
    public void deleteById(String id) {
        int index = getMessagePositionById(id);
        if (index >= 0) {
            items.remove(index);
            notifyItemRemoved(index);
            recountDateHeaders();
        }
    }



    /**
     * Returns {@code true} if, and only if, messages count in adapter is non-zero.
     *
     * @return {@code true} if size is 0, otherwise {@code false}
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Clears the messages list.
     */
    public void clear() {
        items.clear();
    }


    /**
     * Returns the list of selected messages.
     *
     * @return list of selected messages. Empty list if nothing was selected or selection mode is disabled.
     */
    @SuppressWarnings("unchecked")
    public ArrayList<MESSAGE> getSelectedMessages() {
        ArrayList<MESSAGE> selectedMessages = new ArrayList<>();
        for (Wrapper wrapper : items) {
            if (wrapper.item instanceof IMessage && wrapper.isSelected) {
                selectedMessages.add((MESSAGE) wrapper.item);
            }
        }
        return selectedMessages;
    }


    /**
     * Copies text to device clipboard and returns selected messages text. Also it does {@link #unselectAllItems()} for you.
     *
     * @param context   The context.
     * @param formatter The formatter that allows you to format your message model when copying.
     * @param reverse   Change ordering when copying messages.
     * @return formatted text by {@link Formatter}. If it's {@code null} - {@code MESSAGE#toString()} will be used.
     */
    public String copySelectedMessagesText(Context context, Formatter<MESSAGE> formatter, boolean reverse) {
        String copiedText = getSelectedText(formatter, reverse);
        copyToClipboard(context, copiedText);
        unselectAllItems();
        return copiedText;
    }

    /**
     * Unselect all of the selected messages. Notifies {@link SelectionListener} with zero count.
     */
    public void unselectAllItems() {
        for (int i = 0; i < items.size(); i++) {
            Wrapper wrapper = items.get(i);
            if (wrapper.isSelected) {
                wrapper.isSelected = false;
                notifyItemChanged(i);
            }
        }
        isSelectionModeEnabled = false;
        selectedItemsCount = 0;
        notifySelectionChanged();
    }

    /**
     * Deletes all of the selected messages and disables selection mode.
     * Call {@link #getSelectedMessages()} before calling this method to delete messages from your data source.
     */
    public void deleteSelectedMessages() {
        List<MESSAGE> selectedMessages = getSelectedMessages();
        delete(selectedMessages);
        unselectAllItems();
    }

    /**
     * Sets long click listener for item. Fires only if selection mode is disabled.
     *
     * @param onMessageLongClickListener long click listener.
     */
    public void setOnMessageLongClickListener(OnMessageLongClickListener<MESSAGE> onMessageLongClickListener) {
        this.onMessageLongClickListener = onMessageLongClickListener;
    }



    /**
     * Set callback to be invoked when list scrolled to top.
     *
     * @param loadMoreListener listener.
     */
    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }



    /*
    * PRIVATE METHODS
    * */
    private void recountDateHeaders() {
        List<Integer> indicesToDelete = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Wrapper wrapper = items.get(i);
            if (wrapper.item instanceof Date) {
                if (i == 0) {
                    indicesToDelete.add(i);
                } else {
                    if (items.get(i - 1).item instanceof Date) {
                        indicesToDelete.add(i);
                    }
                }
            }
        }

        Collections.reverse(indicesToDelete);
        for (int i : indicesToDelete) {
            items.remove(i);
            notifyItemRemoved(i);
        }
    }

    private void generateDateHeaders(List<MESSAGE> messages) {
        for (int i = 0; i < messages.size(); i++) {
            MESSAGE message = messages.get(i);
            this.items.add(new Wrapper<>(message));
            if (messages.size() > i + 1) {
                MESSAGE nextMessage = messages.get(i + 1);
                this.items.add(new Wrapper<>(message.getCreatedAt()));
            } else {
                this.items.add(new Wrapper<>(message.getCreatedAt()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private int getMessagePositionById(String id) {
        for (int i = 0; i < items.size(); i++) {
            Wrapper wrapper = items.get(i);
            if (wrapper.item instanceof IMessage) {
                MESSAGE message = (MESSAGE) wrapper.item;
                if (message.getId().contentEquals(id)) {
                    return i;
                }
            }
        }
        return -1;
    }


    private void incrementSelectedItemsCount() {
        selectedItemsCount++;
        notifySelectionChanged();
    }

    private void decrementSelectedItemsCount() {
        selectedItemsCount--;
        isSelectionModeEnabled = selectedItemsCount > 0;

        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedItemsCount);
        }
    }

    private void notifyMessageClicked(MESSAGE message) {
        if (onMessageClickListener != null) {
            onMessageClickListener.onMessageClick(message);
        }
    }

    private void notifyMessageViewClicked(View view, MESSAGE message) {
        if (onMessageViewClickListener != null) {
            onMessageViewClickListener.onMessageViewClick(view, message);
        }
    }

    private void notifyMessageLongClicked(MESSAGE message) {
        if (onMessageLongClickListener != null) {
            onMessageLongClickListener.onMessageLongClick(message);
        }
    }

    private void notifyMessageViewLongClicked(View view, MESSAGE message) {
        if (onMessageViewLongClickListener != null) {
            onMessageViewLongClickListener.onMessageViewLongClick(view, message);
        }
    }

    private View.OnClickListener getMessageClickListener(final Wrapper<MESSAGE> wrapper) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectionListener != null && isSelectionModeEnabled) {
                    wrapper.isSelected = !wrapper.isSelected;

                    if (wrapper.isSelected) incrementSelectedItemsCount();
                    else decrementSelectedItemsCount();

                    MESSAGE message = (wrapper.item);
                    notifyItemChanged(getMessagePositionById(message.getId()));
                } else {
                    notifyMessageClicked(wrapper.item);
                    notifyMessageViewClicked(view, wrapper.item);
                }
            }
        };
    }

    private View.OnLongClickListener getMessageLongClickListener(final Wrapper<MESSAGE> wrapper) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (selectionListener == null) {
                    notifyMessageLongClicked(wrapper.item);
                    notifyMessageViewLongClicked(view, wrapper.item);
                    return true;
                } else {
                    isSelectionModeEnabled = true;
                    view.performClick();
                    return true;
                }
            }
        };
    }

    private String getSelectedText(Formatter<MESSAGE> formatter, boolean reverse) {
        StringBuilder builder = new StringBuilder();

        ArrayList<MESSAGE> selectedMessages = getSelectedMessages();
        if (reverse) Collections.reverse(selectedMessages);

        for (MESSAGE message : selectedMessages) {
            builder.append(formatter == null
                    ? message.toString()
                    : formatter.format(message));
            builder.append("\n\n");
        }
        builder.replace(builder.length() - 2, builder.length(), "");

        return builder.toString();
    }

    private void copyToClipboard(Context context, String copiedText) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(copiedText, copiedText);
        clipboard.setPrimaryClip(clip);
    }

    void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    void setStyle(MessagesListStyle style) {
        this.messagesListStyle = style;
    }

    /*
    * WRAPPER
    * */
    private class Wrapper<DATA> {
        protected DATA item;
        protected boolean isSelected;

        Wrapper(DATA item) {
            this.item = item;
        }
    }

    /*
    * LISTENERS
    * */

    /**
     * Interface definition for a callback to be invoked when next part of messages need to be loaded.
     */
    public interface OnLoadMoreListener {

        /**
         * Fires when user scrolled to the end of list.
         *
         * @param page            next page to download.
         * @param totalItemsCount current items count.
         */
        void onLoadMore(int page, int totalItemsCount);
    }

    /**
     * Interface definition for a callback to be invoked when selected messages count is changed.
     */
    public interface SelectionListener {

        /**
         * Fires when selected items count is changed.
         *
         * @param count count of selected items.
         */
        void onSelectionChanged(int count);
    }

    /**
     * Interface definition for a callback to be invoked when message item is clicked.
     */
    public interface OnMessageClickListener<MESSAGE extends IMessage> {

        /**
         * Fires when message is clicked.
         *
         * @param message clicked message.
         */
        void onMessageClick(MESSAGE message);
    }

    /**
     * Interface definition for a callback to be invoked when message view is clicked.
     */
    public interface OnMessageViewClickListener<MESSAGE extends IMessage> {

        /**
         * Fires when message view is clicked.
         *
         * @param message clicked message.
         */
        void onMessageViewClick(View view, MESSAGE message);
    }

    /**
     * Interface definition for a callback to be invoked when message item is long clicked.
     */
    public interface OnMessageLongClickListener<MESSAGE extends IMessage> {

        /**
         * Fires when message is long clicked.
         *
         * @param message clicked message.
         */
        void onMessageLongClick(MESSAGE message);
    }

    /**
     * Interface definition for a callback to be invoked when message view is long clicked.
     */
    public interface OnMessageViewLongClickListener<MESSAGE extends IMessage> {

        /**
         * Fires when message view is long clicked.
         *
         * @param message clicked message.
         */
        void onMessageViewLongClick(View view, MESSAGE message);
    }

    /**
     * Interface used to format your message model when copying.
     */
    public interface Formatter<MESSAGE> {

        /**
         * Formats an string representation of the message object.
         *
         * @param message The object that should be formatted.
         * @return Formatted text.
         */
        String format(MESSAGE message);
    }



}
