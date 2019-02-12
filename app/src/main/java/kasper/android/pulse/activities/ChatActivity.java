package kasper.android.pulse.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.MessagesAdapter;
import kasper.android.pulse.callbacks.network.OnFileUploadListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.callbacks.ui.OnFileSelectListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.LinearDecoration;
import kasper.android.pulse.helpers.CallbackHelper;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.retrofit.MessageHandler;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.ProgressListener;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.FileUploading;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import retrofit2.Call;

public class ChatActivity extends AppCompatActivity {

    private long complexId;
    private long roomId;
    private long startFileId;
    private boolean afterRoom = false;

    private List<Entities.Message> messages = new ArrayList<>();
    private ListIterator<Integer> searchIterator;
    private int currentSearchIndex = 0;
    private int searchCounter = 0, searchTotal = 0;
    private MessageSearchTask messageSearchTask;

    private boolean searchMode = false;

    RelativeLayout mainToolbarContent;
    RelativeLayout searchToolbarContent;

    RecyclerView chatRV;
    FloatingActionButton toBottomFAB;
    EditText chatET;
    ImageButton filesBTN;
    ImageButton sendBTN;

    EditText searchET;
    TextView searchOcc;
    ImageButton searchUp;
    ImageButton searchDown;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Core.getInstance().bus().register(this);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("complex_id"))
                complexId = getIntent().getExtras().getLong("complex_id");
            if (getIntent().getExtras().containsKey("room_id"))
                roomId = getIntent().getExtras().getLong("room_id");
            if (getIntent().getExtras().containsKey("start_file_id"))
                startFileId = getIntent().getExtras().getLong("start_file_id");
            if (getIntent().getExtras().containsKey("after_room"))
                afterRoom = getIntent().getExtras().getBoolean("after_room");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initViews();
        initDecorations();
        initListeners();
        initMessages();

        if (startFileId > 0) {
            new Handler().postDelayed(() -> {
                if (chatRV.getAdapter() != null) {
                    int pos = ((MessagesAdapter) chatRV.getAdapter()).findFilePosition(startFileId);
                    scrollChatToPosition(pos);
                }
            }, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        if (chatRV.getAdapter() != null)
            ((MessagesAdapter) chatRV.getAdapter()).dispose();
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Subscribe
    public void onMessageReceived(MessageReceived messageReceived) {
        if (chatRV.getLayoutManager() != null) {
            if (((LinearLayoutManager) chatRV.getLayoutManager()).findLastVisibleItemPosition() > messages.size() - 3) {
                scrollChatToPosition(messages.size() - 1);
            }
        }
    }

    @Subscribe
    public void onMessageSending(MessageSending messageSending) {
        if (chatRV.getLayoutManager() != null) {
            if (((LinearLayoutManager) chatRV.getLayoutManager()).findLastVisibleItemPosition() > messages.size() - 3) {
                scrollChatToPosition(messages.size() - 1);
            }
        }
    }

    @Subscribe
    public void onMessageSent(MessageSent messageSent) {
        for (Entities.Message message : messages) {
            if (message.getMessageId() == messageSent.getLocalMessageId()) {
                message.setMessageId(messageSent.getOnlineMessageId());
                break;
            }
        }
    }

    private void scrollChatToPosition(int position) {
        if (chatRV.getLayoutManager() != null) {
            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
                @Override protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(position);
            chatRV.getLayoutManager().startSmoothScroll(smoothScroller);
        }
    }

    private void initViews() {
        mainToolbarContent = findViewById(R.id.mainContent);
        searchToolbarContent = findViewById(R.id.searchContent);
        chatRV = findViewById(R.id.fragment_messages_recycler_view);
        toBottomFAB = findViewById(R.id.toBottomFAB);
        chatET = findViewById(R.id.fragment_messages_edit_text);
        filesBTN = findViewById(R.id.fragment_messages_files_image_button);
        sendBTN = findViewById(R.id.fragment_messages_send_image_button);
        searchET = findViewById(R.id.searchET);
        searchOcc = findViewById(R.id.searchOccurrences);
        searchUp = findViewById(R.id.searchUp);
        searchDown = findViewById(R.id.searchDown);
    }

    private void initDecorations() {
        chatRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        chatRV.addItemDecoration(new LinearDecoration(GraphicHelper.dpToPx(16), GraphicHelper.dpToPx(16)));
    }

    private void initListeners() {
        chatRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (chatRV.getAdapter() != null && chatRV.getLayoutManager() != null) {
                    if (((LinearLayoutManager) chatRV.getLayoutManager())
                            .findLastVisibleItemPosition() < chatRV.getAdapter().getItemCount() - 5) {
                        toBottomFAB.animate().cancel();
                        toBottomFAB.animate().y(GraphicHelper.getScreenHeight()
                                - GraphicHelper.dpToPx(24) - GraphicHelper.dpToPx(72 + 56))
                                .setDuration(350).start();
                    } else {
                        toBottomFAB.animate().cancel();
                        toBottomFAB.animate().y(GraphicHelper.getScreenHeight())
                                .setDuration(350).start();
                    }
                }
            }
        });
        sendBTN.setOnClickListener(v -> {
            final String text = chatET.getText().toString();
            if (text.length() == 0) {
                return;
            }
            final Pair<Entities.Message, Entities.MessageLocal> pair = DatabaseHelper.notifyTextMessageSending(roomId, text);
            final Entities.Message message = pair.first;
            final Entities.MessageLocal messageLocal = pair.second;
            final long messageLocalId = message.getMessageId();
            Core.getInstance().bus().post(new MessageSending(message, messageLocal));
            final Packet packet = new Packet();
            Entities.Complex complex = new Entities.Complex();
            complex.setComplexId(complexId);
            packet.setComplex(complex);
            Entities.Room room = new Entities.Room();
            room.setRoomId(roomId);
            packet.setRoom(room);
            packet.setTextMessage((Entities.TextMessage) message);
            MessageHandler messageHandler = NetworkHelper.getRetrofit().create(MessageHandler.class);
            Call<Packet> call = messageHandler.createTextMessage(packet);
            NetworkHelper.requestServer(call, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    final Entities.TextMessage msg = packet.getTextMessage();
                    DatabaseHelper.notifyTextMessageSent(messageLocalId, msg.getMessageId(), msg.getTime());
                    Core.getInstance().bus().post(new MessageSent(messageLocalId, msg.getMessageId()));
                }

                @Override
                public void onServerFailure() {
                    Toast.makeText(ChatActivity.this, "Message delivery failure", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectionFailure() {
                    Toast.makeText(ChatActivity.this, "Message delivery failure", Toast.LENGTH_SHORT).show();
                }
            });
            chatET.setText("");
        });
        filesBTN.setOnClickListener(v -> {
            OnFileSelectListener selectListener = (path, docType) -> {
                String fileName = new File(path).getName();
                Entities.File file;
                Entities.FileLocal fileLocal;
                Entities.Message message;
                Entities.MessageLocal messageLocal;
                if (docType == DocTypes.Photo) {
                    try {
                        FileInputStream inputStream = new FileInputStream(new File(path));
                        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                        bitmapOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
                        int imageWidth = bitmapOptions.outWidth;
                        int imageHeight = bitmapOptions.outHeight;
                        inputStream.close();
                        Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper.notifyPhotoUploading(false, path, imageWidth, imageHeight);
                        file = filePair.first;
                        fileLocal = filePair.second;
                        Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper.notifyPhotoMessageSending(roomId, file.getFileId());
                        message = msgPair.first;
                        messageLocal = msgPair.second;
                    } catch (Exception ignored) {
                        message = null;
                        messageLocal = null;
                        file = null;
                        fileLocal = null;
                    }
                } else if (docType == DocTypes.Audio) {
                    Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper.notifyAudioUploading(false, path, fileName, 60000);
                    file = filePair.first;
                    fileLocal = filePair.second;
                    Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper.notifyAudioMessageSending(roomId, file.getFileId());
                    message = msgPair.first;
                    messageLocal = msgPair.second;
                } else if (docType == DocTypes.Video) {
                    Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper.notifyVideoUploading(false, path, fileName, 60000);
                    file = filePair.first;
                    fileLocal = filePair.second;
                    Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper.notifyVideoMessageSending(roomId, file.getFileId());
                    message = msgPair.first;
                    messageLocal = msgPair.second;
                } else {
                    file = null;
                    fileLocal = null;
                    message = null;
                    messageLocal = null;
                }

                final Entities.File finalFile = file;
                final Entities.Message finalMessage = message;
                final Entities.MessageLocal finalMessageLocal = messageLocal;

                Core.getInstance().bus().post(new FileUploading(docType, file, fileLocal));
                Core.getInstance().bus().post(new MessageSending(message, messageLocal));

                ProgressListener progressListener = progress -> {
                    if (finalFile != null) {
                        DatabaseHelper.notifyFileTransferProgressed(finalFile.getFileId(), progress);
                        Core.getInstance().bus().post(new UiThreadRequested(() ->
                            Core.getInstance().bus().post(new FileTransferProgressed(docType
                                        , finalFile.getFileId(), progress))));
                    }
                };

                OnFileUploadListener uploadListener = (OnFileUploadListener) (fileId, fileUsageId) -> {
                    if (finalFile != null) {
                        final long localFileId = finalFile.getFileId();
                        if (docType == DocTypes.Photo) {
                            DatabaseHelper.notifyPhotoUploaded(localFileId, fileId);
                        } else if (docType == DocTypes.Audio) {
                            DatabaseHelper.notifyAudioUploaded(localFileId, fileId);
                        } else if (docType == DocTypes.Video) {
                            DatabaseHelper.notifyVideoUploaded(localFileId, fileId);
                        }
                        DatabaseHelper.notifyUpdateMessageAfterFileUpload(finalMessage.getMessageId(), fileId, fileUsageId);
                        Core.getInstance().bus().post(new UiThreadRequested(() -> {
                            Core.getInstance().bus().post(new FileUploaded(docType, localFileId, fileId));
                            Packet packet = new Packet();
                            Entities.Complex complex = new Entities.Complex();
                            complex.setComplexId(complexId);
                            packet.setComplex(complex);
                            Entities.Room room = new Entities.Room();
                            room.setRoomId(roomId);
                            packet.setRoom(room);
                            finalFile.setFileId(fileId);
                            packet.setFile(finalFile);
                            MessageHandler messageHandler = NetworkHelper.getRetrofit().create(MessageHandler.class);
                            Call<Packet> call = messageHandler.createFileMessage(packet);
                            NetworkHelper.requestServer(call, new ServerCallback() {
                                @Override
                                public void onRequestSuccess(Packet packet) {
                                    long messageId = -1;
                                    long time;
                                    if (docType == DocTypes.Photo) {
                                        messageId = packet.getPhotoMessage().getMessageId();
                                        time = packet.getPhotoMessage().getTime();
                                        DatabaseHelper.notifyPhotoMessageSent(finalMessageLocal.getMessageId(), messageId, time);
                                    } else if (docType == DocTypes.Audio) {
                                        messageId = packet.getAudioMessage().getMessageId();
                                        time = packet.getAudioMessage().getTime();
                                        DatabaseHelper.notifyAudioMessageSent(finalMessageLocal.getMessageId(), messageId, time);
                                    } else if (docType == DocTypes.Video) {
                                        messageId = packet.getVideoMessage().getMessageId();
                                        time = packet.getVideoMessage().getTime();
                                        DatabaseHelper.notifyVideoMessageSent(finalMessageLocal.getMessageId(), messageId, time);
                                    }
                                    Core.getInstance().bus().post(new MessageSent(finalMessageLocal.getMessageId(), messageId));
                                }

                                @Override
                                public void onServerFailure() {
                                    Toast.makeText(ChatActivity.this, "Message delivery failure", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onConnectionFailure() {
                                    Toast.makeText(ChatActivity.this, "Message delivery failure", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }));
                    }
                };

                NetworkHelper.uploadFile(file, complexId, roomId, path, progressListener, uploadListener);
            };
            long selectCallbackId = CallbackHelper.register(selectListener);
            startActivity(new Intent(ChatActivity.this, FilesActivity.class)
                    .putExtra("select-callback", selectCallbackId));
        });
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                if (query.length() > 0) {
                    if (messageSearchTask != null) {
                        try {
                            messageSearchTask.cancel(true);
                        } catch (Exception ignored) { }
                    }
                    messageSearchTask = new MessageSearchTask();
                    messageSearchTask.execute(query);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private class MessageSearchTask extends AsyncTask<String, Void, List<Integer>> {
        @Override
        protected List<Integer> doInBackground(String... inputs) {
            String query = inputs[0];
            List<Integer> result = new ArrayList<>();
            int counter = messages.size() - 1;
            ListIterator<Entities.Message> iterator = messages.listIterator(messages.size());
            while (iterator.hasPrevious()) {
                Entities.Message message = iterator.previous();
                if (message instanceof Entities.TextMessage) {
                    if (((Entities.TextMessage) message).getText().contains(query)) {
                        result.add(0, counter);
                    }
                }
                counter--;
            }
            return result;
        }
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(List<Integer> integers) {
            super.onPostExecute(integers);
            searchOcc.setText(integers.size() + "");
            searchUp.setColorFilter(Color.WHITE);
            searchDown.setColorFilter(Color.GRAY);
            searchTotal = integers.size();
            searchCounter = searchTotal;
            searchIterator = integers.listIterator(integers.size());
            if (!integers.isEmpty() && chatRV.getAdapter() != null) {
                int last = searchIterator.previous();
                currentSearchIndex = last;
                scrollChatToPosition(last);
            }
            updateButtonEnabled();
        }
    }

    private void initMessages() {
        messages = DatabaseHelper.getMessages(roomId);
        Hashtable<Long, Entities.MessageLocal> messageLocals = DatabaseHelper.getLocalMessages(messages);
        Hashtable<Long, Entities.FileLocal> fileLocals = DatabaseHelper.getLocalFiles(messages);
        chatRV.setAdapter(new MessagesAdapter(this, messages, messageLocals, fileLocals));
        if (chatRV.getAdapter() != null)
            chatRV.scrollToPosition(chatRV.getAdapter().getItemCount() - 1);
    }

    public void onSearchUpClicked(View view) {
        if (searchIterator != null && searchIterator.hasPrevious()) {
            int current = searchIterator.previous();
            if (current == currentSearchIndex) {
                onSearchUpClicked(view);
                return;
            }
            currentSearchIndex = current;
            searchCounter--;
            if (chatRV.getAdapter() != null)
                scrollChatToPosition(current);
            updateButtonEnabled();
        }
    }

    public void onSearchDownClicked(View view) {
        if (searchIterator != null && searchIterator.hasNext()) {
            int current = searchIterator.next();
            if (current == currentSearchIndex) {
                onSearchDownClicked(view);
                return;
            }
            currentSearchIndex = current;
            searchCounter++;
            if (chatRV.getAdapter() != null)
                scrollChatToPosition(current);
            updateButtonEnabled();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateButtonEnabled() {
        if (!searchIterator.hasPrevious())
            searchUp.setColorFilter(Color.GRAY);
        else
            searchUp.setColorFilter(Color.WHITE);
        if (!searchIterator.hasNext())
            searchDown.setColorFilter(Color.GRAY);
        else
            searchDown.setColorFilter(Color.WHITE);
        searchOcc.setText(searchCounter + "/" + searchTotal);
    }

    public void onToBottomFABClicked(View view) {
        if (chatRV.getAdapter() != null)
            scrollChatToPosition(chatRV.getAdapter().getItemCount() - 1);
    }

    public void onBackBtnClicked(View view) {
        if (searchMode) {
            mainToolbarContent.setVisibility(View.VISIBLE);
            searchToolbarContent.setVisibility(View.GONE);
            searchMode = false;
        } else {
            this.onBackPressed();
        }
    }

    public void onOptionsBtnClicked(View view) {
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        MenuInflater inflater = getMenuInflater();
        if (afterRoom)
            inflater.inflate(R.menu.chat_options_menu1, popupMenu.getMenu());
        else
            inflater.inflate(R.menu.chat_options_menu2, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.goto_room:
                    startActivity(new Intent(ChatActivity.this, RoomActivity.class)
                            .putExtra("complex_id", complexId)
                            .putExtra("room_id", roomId)
                            .putExtra("after_chat", true));
                    return true;
                case R.id.show_files:
                    startActivity(new Intent(ChatActivity.this, DocsActivity.class)
                            .putExtra("complex_id", complexId)
                            .putExtra("room_id", roomId));
                    return true;
                case R.id.search_messages:
                    searchTotal = 0;
                    searchCounter = 0;
                    searchET.setText("");
                    searchOcc.setText("0/0");
                    currentSearchIndex = 0;
                    searchIterator = null;
                    searchUp.setColorFilter(Color.GRAY);
                    searchDown.setColorFilter(Color.GRAY);
                    searchToolbarContent.setVisibility(View.VISIBLE);
                    mainToolbarContent.setVisibility(View.GONE);
                    searchMode = true;
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }
}
