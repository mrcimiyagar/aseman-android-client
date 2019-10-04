package kasper.android.pulse.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.r0adkll.slidr.Slidr;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.MessagesAdapter;
import kasper.android.pulse.callbacks.network.ServerCallback2;
import kasper.android.pulse.callbacks.ui.OnFileSelectListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.LinearDecoration;
import kasper.android.pulse.helpers.CallbackHelper;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.FileMessageSending;
import kasper.android.pulse.models.extras.TextMessageSending;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.MessageHandler;
import kasper.android.pulse.rxbus.notifications.FileReceived;
import kasper.android.pulse.rxbus.notifications.MemberAccessUpdated;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;
import kasper.android.pulse.services.AsemanService;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ChatActivity extends BaseActivity {

    private long complexId;
    private long roomId;
    private long startFileId;
    private boolean afterRoom = false;

    private List<Entities.Message> messages = new ArrayList<>();
    private HashSet<Long> messageIdsStore = new HashSet<>();
    private ListIterator<Integer> searchIterator;
    private int currentSearchIndex = 0;
    private int searchCounter = 0, searchTotal = 0;
    private MessageSearchTask messageSearchTask;

    private boolean searchMode = false;

    CoordinatorLayout rootView;

    RelativeLayout mainToolbarContent;
    RelativeLayout searchToolbarContent;

    RecyclerView chatRV;
    FloatingActionButton toBottomFAB;
    EmojiEditText chatET;
    ImageButton filesBTN;
    ImageButton emojiBtn;
    ImageButton sendBTN;

    EditText searchET;
    TextView searchOcc;
    ImageButton searchUp;
    ImageButton searchDown;

    EmojiPopup emojiPopup;

    CardView chatETContainer;

    private Entities.MemberAccess memberAccess;

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

        Entities.User user = DatabaseHelper.getMe();
        if (user != null)
            memberAccess = DatabaseHelper.getMemberAccessByComplexAndUserId(complexId, user.getBaseUserId());

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        initViews();

                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) rootView.getLayoutParams();
                        if (GraphicHelper.pxToDp(GraphicHelper.getScreenWidth()) < 500) {
                            lp.width = MATCH_PARENT;
                        } else {
                            lp.width = GraphicHelper.dpToPx(500);
                            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        }
                        rootView.setLayoutParams(lp);

                        initDecorations();
                        initMessages();
                        initListeners();

                        handleMemberAccess();

                        fetchMessages();

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
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
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

    @Subscribe
    public void onMemberAccessUpdated(MemberAccessUpdated memberAccessUpdated) {
        Entities.Membership membership = DatabaseHelper.getMembershipById(memberAccessUpdated.getMemberAccess().getMembershipId());
        if (membership.getComplex().getComplexId() == complexId) {
            Entities.User me = DatabaseHelper.getMe();
            if (me != null) {
                if (membership.getUser().getBaseUserId() == me.getBaseUserId()) {
                    memberAccess = memberAccessUpdated.getMemberAccess();
                    handleMemberAccess();
                }
            }
        }
    }

    private void handleMemberAccess() {
        if (memberAccess.isCanCreateMessage()) {
            chatETContainer.setVisibility(View.VISIBLE);
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) chatRV.getLayoutParams();
            lp.bottomMargin = GraphicHelper.dpToPx(56);
            chatRV.setLayoutParams(lp);
        } else {
            chatETContainer.setVisibility(View.GONE);
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) chatRV.getLayoutParams();
            lp.bottomMargin = 0;
            chatRV.setLayoutParams(lp);
        }
    }

    private void scrollChatToPosition(int position) {
        if (chatRV.getLayoutManager() != null) {
            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(position);
            try {
                chatRV.getLayoutManager().startSmoothScroll(smoothScroller);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void initViews() {
        rootView = findViewById(R.id.rootView);
        mainToolbarContent = findViewById(R.id.mainContent);
        searchToolbarContent = findViewById(R.id.searchContent);
        chatRV = findViewById(R.id.fragment_messages_recycler_view);
        toBottomFAB = findViewById(R.id.toBottomFAB);
        chatET = findViewById(R.id.fragment_messages_edit_text);
        filesBTN = findViewById(R.id.fragment_messages_files_image_button);
        emojiBtn = findViewById(R.id.emojiBTN);
        sendBTN = findViewById(R.id.fragment_messages_send_image_button);
        searchET = findViewById(R.id.searchET);
        searchOcc = findViewById(R.id.searchOccurrences);
        searchUp = findViewById(R.id.searchUp);
        searchDown = findViewById(R.id.searchDown);
        chatETContainer = findViewById(R.id.chatEtContainer);
    }

    private void initDecorations() {
        chatRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        chatRV.addItemDecoration(new LinearDecoration(GraphicHelper.dpToPx(16), GraphicHelper.dpToPx(16)));
    }

    private void initListeners() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiPopupShownListener(() -> emojiBtn.setImageResource(R.drawable.ic_keyboard))
                .setOnEmojiPopupDismissListener(() -> emojiBtn.setImageResource(R.drawable.ic_emoji))
                .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                .setIconColor(Color.WHITE)
                .build(chatET);
        emojiBtn.setOnClickListener(v -> emojiPopup.toggle());
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

                    if (!recyclerView.canScrollVertically(-1)) {
                        fetchMessages();
                    }
                }
            }
        });
        sendBTN.setOnClickListener(v -> {
            if (chatET.getText() != null) {
                final String text = chatET.getText().toString();
                if (text.length() == 0) return;
                AsemanService.enqueueMessage(new TextMessageSending(complexId, roomId, text));
                chatET.setText("");
            }
        });
        filesBTN.setOnClickListener(v -> {
            OnFileSelectListener selectListener = (path, docType) ->
                    AsemanService.enqueueMessage(new FileMessageSending(complexId, roomId, docType, path));
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

    private void fetchMessages() {
        Packet packet = new Packet();
        Entities.Complex complex = new Entities.Complex();
        complex.setComplexId(complexId);
        packet.setComplex(complex);
        Entities.Room room = new Entities.Room();
        room.setRoomId(roomId);
        packet.setBaseRoom(room);
        Entities.Message msg = new Entities.Message();
        if (messages.size() > 0) {
            msg.setMessageId(messages.get(0).getMessageId());
        } else {
            msg.setMessageId(0);
        }
        packet.setMessage(msg);
        packet.setFetchNext(false);
        NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(MessageHandler.class).getMessages(packet)
                , new ServerCallback2() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        if (packet.getMessages().size() > 0) {
                            Collections.reverse(packet.getMessages());
                            int counter = 0;
                            for (Entities.Message message : packet.getMessages()) {
                                if (!messageIdsStore.contains(message.getMessageId())) {
                                    if (message instanceof Entities.TextMessage)
                                        DatabaseHelper.notifyTextMessageReceived((Entities.TextMessage) message);
                                    else if (message instanceof Entities.PhotoMessage)
                                        DatabaseHelper.notifyPhotoMessageReceived((Entities.PhotoMessage) message);
                                    else if (message instanceof Entities.AudioMessage)
                                        DatabaseHelper.notifyAudioMessageReceived((Entities.AudioMessage) message);
                                    else if (message instanceof Entities.VideoMessage)
                                        DatabaseHelper.notifyVideoMessageReceived((Entities.VideoMessage) message);
                                    else if (message instanceof Entities.ServiceMessage)
                                        DatabaseHelper.notifyServiceMessageReceived((Entities.ServiceMessage) message);
                                    Entities.MessageLocal messageLocal = new Entities.MessageLocal();
                                    messageLocal.setMessageId(message.getMessageId());
                                    messageLocal.setSent(true);
                                    if (message instanceof Entities.PhotoMessage) {
                                        Entities.FileLocal fileLocal = new Entities.FileLocal();
                                        fileLocal.setFileId(((Entities.PhotoMessage) message).getPhoto().getFileId());
                                        fileLocal.setPath("");
                                        fileLocal.setProgress(0);
                                        fileLocal.setTransferring(false);
                                        Core.getInstance().bus().post(new FileReceived(DocTypes.Video
                                                , ((Entities.PhotoMessage) message).getPhoto(), fileLocal));
                                    } else if (message instanceof Entities.AudioMessage) {
                                        Entities.FileLocal fileLocal = new Entities.FileLocal();
                                        fileLocal.setFileId(((Entities.AudioMessage) message).getAudio().getFileId());
                                        fileLocal.setPath("");
                                        fileLocal.setProgress(0);
                                        fileLocal.setTransferring(false);
                                        Core.getInstance().bus().post(new FileReceived(DocTypes.Video
                                                , ((Entities.AudioMessage) message).getAudio(), fileLocal));
                                    } else if (message instanceof Entities.VideoMessage) {
                                        Entities.FileLocal fileLocal = new Entities.FileLocal();
                                        fileLocal.setFileId(((Entities.VideoMessage) message).getVideo().getFileId());
                                        fileLocal.setPath("");
                                        fileLocal.setProgress(0);
                                        fileLocal.setTransferring(false);
                                        Core.getInstance().bus().post(new FileReceived(DocTypes.Video
                                                , ((Entities.VideoMessage) message).getVideo(), fileLocal));
                                    }
                                    Core.getInstance().bus().post(new MessageReceived(false, message, messageLocal));
                                    scrollChatToPosition(counter);
                                    counter++;
                                }
                            }
                        }
                    }
                    @Override
                    public void onLogicalError(String errorCode) {

                    }
                    @Override
                    public void onServerFailure() {

                    }
                    @Override
                    public void onConnectionFailure() {

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
        for (Entities.Message message : messages) {
            messageIdsStore.add(message.getMessageId());
        }
        Hashtable<Long, Entities.MessageLocal> messageLocals = DatabaseHelper.getLocalMessages(messages);
        Hashtable<Long, Entities.FileLocal> fileLocals = DatabaseHelper.getLocalFiles(messages);
        chatRV.setAdapter(new MessagesAdapter(this, roomId, messages, messageIdsStore, messageLocals, fileLocals));
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
        showOptionsMenu(afterRoom ? R.menu.chat_options_menu1 : R.menu.chat_options_menu2, view, menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.members_list:
                    startActivity(new Intent(ChatActivity.this, ComplexMembersActivity.class)
                            .putExtra("complex_id", complexId));
                    return true;
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
    }
}
