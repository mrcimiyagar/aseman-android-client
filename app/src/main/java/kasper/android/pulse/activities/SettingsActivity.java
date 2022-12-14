package kasper.android.pulse.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.github.javiersantos.bottomdialogs.BottomDialog;

import org.michaelbel.bottomsheet.BottomSheet;

import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.middleware.OnMeSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.AsemanDB;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.UserProfileUpdating;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.AuthHandler;
import kasper.android.pulse.retrofit.UserHandler;
import kasper.android.pulse.rxbus.notifications.ConnectionStateChanged;
import kasper.android.pulse.rxbus.notifications.UserProfileUpdated;
import kasper.android.pulse.services.AsemanService;
import retrofit2.Call;

public class SettingsActivity extends BaseActivity {

    private CircleImageView avatarIV;
    private TextView titleTV;
    private TextView subtitleTV;

    Entities.User me;

    public void onInvitesBtnClicked(View view) {
        startActivity(new Intent(this, InvitesActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Core.getInstance().bus().register(this);

        this.avatarIV = findViewById(R.id.avatar);
        this.titleTV = findViewById(R.id.title);
        this.subtitleTV = findViewById(R.id.subtitle);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        me = DatabaseHelper.getMe();
        if (me != null) {
            titleTV.setText(me.getTitle());
            NetworkHelper.loadUserAvatar(me.getAvatar(), avatarIV);
        }

        subtitleTV.setText(AsemanService.getConnectionState());

        DataSyncer.syncMeWithServer(new OnMeSyncListener() {
            @Override
            public void meSynced(Entities.User me, long homeId) {
                titleTV.setText(me.getTitle());
                NetworkHelper.loadUserAvatar(me.getAvatar(), avatarIV);
            }
            @Override
            public void syncFailed() { }
        });
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    public void onOptionsBtnClicked(View view) {
        showOptionsMenu(R.menu.settings_options_menu, view, menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.logout:
                    new BottomDialog.Builder(SettingsActivity.this)
                            .setTitle("Logout")
                            .setContent("Do you really want to logout of this session ?")
                            .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                            .setTitleColor(Color.WHITE)
                            .setTextColor(Color.WHITE)
                            .setPositiveText("Logout")
                            .setPositiveBackgroundColorResource(R.color.colorPrimary)
                            .setPositiveBackgroundColor(getResources().getColor(R.color.colorBlue))
                            .setPositiveTextColor(Color.WHITE)
                            .onPositive(dialog -> NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(AuthHandler.class).logout()
                                    , new ServerCallback() {
                                        @Override
                                        public void onRequestSuccess(Packet packet) {
                                            AsemanDB.deleteAllData(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startActivity(new Intent(SettingsActivity.this, RegisterActivity.class)
                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                                                }
                                            });
                                        }
                                        @Override
                                        public void onServerFailure() {
                                            AsemanDB.deleteAllData(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startActivity(new Intent(SettingsActivity.this, RegisterActivity.class)
                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                                                }
                                            });
                                        }
                                        @Override
                                        public void onConnectionFailure() {
                                            AsemanDB.deleteAllData(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startActivity(new Intent(SettingsActivity.this, RegisterActivity.class)
                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                                                }
                                            });
                                        }
                                    }))
                            .setNegativeText("Cancel")
                            .setNegativeTextColor(Color.WHITE)
                            .onNegative(BottomDialog::dismiss)
                            .show();
                    return true;
                case R.id.delete_account:
                    new BottomDialog.Builder(SettingsActivity.this)
                            .setTitle("Delete Account")
                            .setContent("Do you really want to delete your account ?")
                            .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                            .setTitleColor(Color.WHITE)
                            .setTextColor(Color.WHITE)
                            .setPositiveText("Delete account")
                            .setPositiveBackgroundColorResource(R.color.colorPrimary)
                            .setPositiveBackgroundColor(getResources().getColor(R.color.colorBlue))
                            .setPositiveTextColor(Color.WHITE)
                            .onPositive(dialog ->
                                    NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(AuthHandler.class).deleteAccount(),
                                            new ServerCallback() {
                                                @Override
                                                public void onRequestSuccess(Packet packet) {
                                                    AsemanDB.deleteAllData(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startActivity(new Intent(SettingsActivity.this, RegisterActivity.class)
                                                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onServerFailure() {
                                                    AsemanDB.deleteAllData(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startActivity(new Intent(SettingsActivity.this, RegisterActivity.class)
                                                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onConnectionFailure() {
                                                    AsemanDB.deleteAllData(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startActivity(new Intent(SettingsActivity.this, RegisterActivity.class)
                                                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                                                        }
                                                    });
                                                }
                                            }))
                            .setNegativeText("Cancel")
                            .setNegativeTextColor(Color.WHITE)
                            .onNegative(BottomDialog::dismiss)
                            .show();
                    return true;
                default:
                    return false;
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    public void onConnectionStateChanged(ConnectionStateChanged connectionStateChanged) {
        switch (connectionStateChanged.getState()) {
            case Connecting:
                subtitleTV.setText("Connecting");
                showSnack("Connecting to server...", "Dismiss", view -> hideSnack());
                break;
            case Connected:
                subtitleTV.setText("Online");
                hideSnack();
                break;
        }
    }

    @Subscribe
    public void onProfileUpdated(UserProfileUpdated updated) {
        if (updated.getUser().getBaseUserId() == me.getBaseUserId())
            NetworkHelper.loadUserAvatar(updated.getUser().getAvatar(), avatarIV);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras() != null) {
                    String path = data.getExtras().getString("path");
                    AsemanService.updateUserProfileAvatar(new UserProfileUpdating(path, me));
                }
            }
        } else if (requestCode == 456) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras() != null) {
                    String title = data.getExtras().getString("title");
                    if (title != null && me != null) {
                        me.setTitle(title);
                        updateProfile();
                    }
                }
            }
        }
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }

    public void onEditTitleBtnClicked(View view) {
        startActivityForResult(new Intent(this, TitleEditorActivity.class)
                .putExtra("title", me.getTitle()), 456);
    }

    public void onAvatarImageClicked(View view) {
        Drawable editDrawable = getResources().getDrawable(R.drawable.ic_edit);
        editDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable viewDrawable = getResources().getDrawable(R.drawable.ic_view);
        viewDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable deleteDrawable = getResources().getDrawable(R.drawable.ic_delete);
        deleteDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        Drawable[] itemIcons;
        String[] itemTitles;

        if (me != null) {
            if (me.getAvatar() > 0) {
                itemIcons = new Drawable[]{
                        editDrawable,
                        deleteDrawable,
                        viewDrawable
                };
                itemTitles = new String[]{
                        "Set new photo",
                        "Delete photo",
                        "View photo"
                };
            } else {
                itemIcons = new Drawable[]{
                        editDrawable,
                };
                itemTitles = new String[]{
                        "Set new photo"
                };
            }
            BottomSheet.Builder builder = new BottomSheet.Builder(this);
            builder.setItems(itemTitles, itemIcons,
                    (dialogInterface, i) -> {
                        if (i == 0) {
                            startActivityForResult(new Intent(this, PickImageActivity.class), 123);
                        } else if (i == 1) {
                            new BottomDialog.Builder(this)
                                    .setTitle("Delete complex")
                                    .setContent("Do you really want to delete profile photo ?")
                                    .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                                    .setTitleColor(Color.WHITE)
                                    .setTextColor(Color.WHITE)
                                    .setPositiveText("Delete")
                                    .setPositiveBackgroundColorResource(R.color.colorPrimary)
                                    .setPositiveBackgroundColor(getResources().getColor(R.color.colorBlue))
                                    .setPositiveTextColor(Color.WHITE)
                                    .onPositive(dialog -> {
                                        me.setAvatar(-1L);
                                        updateProfile();
                                    })
                                    .setNegativeText("Cancel")
                                    .setNegativeTextColor(Color.WHITE)
                                    .onNegative(BottomDialog::dismiss)
                                    .show();
                        } else if (i == 2) {
                            Pair<View, String> picture = Pair.create(avatarIV, "photo");
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SettingsActivity.this, picture);
                            Intent intent = new Intent(SettingsActivity.this, PhotoViewerActivity.class);
                            if (me.getAvatar() > 0) {
                                intent.putExtra("fileId", me.getAvatar());
                                SettingsActivity.this.startActivity(intent, options.toBundle());
                            }
                        }
                    })
                    .setTitle("Profile photo")
                    .setDarkTheme(true)
                    .setTitleTextColor(Color.WHITE)
                    .setContentType(BottomSheet.LIST)
                    .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                    .setItemTextColor(Color.WHITE)
                    .show();
        }
    }

    private void updateProfile() {
        Packet packet = new Packet();
        if (me != null) {
            packet.setUser(me);
            UserHandler profileHandler = NetworkHelper.getRetrofit().create(UserHandler.class);
            Call<Packet> call = profileHandler.updateUserProfile(packet);
            NetworkHelper.requestServer(call, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    DatabaseHelper.updateMe(me);
                    titleTV.setText(me.getTitle());
                    NetworkHelper.loadUserAvatar(me.getAvatar(), avatarIV);
                    Core.getInstance().bus().post(new UserProfileUpdated(me));
                }

                @Override
                public void onServerFailure() {
                    Toast.makeText(SettingsActivity.this, "Profile update failure", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectionFailure() {
                    Toast.makeText(SettingsActivity.this, "Profile update failure", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
