package kasper.android.pulse.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.anadeainc.rxbus.Subscribe;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import kasper.android.pulse.R;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.rxbus.notifications.ConnectionStateChanged;

public class BaseActivity extends AppCompatActivity {

    private Snackbar statusSnackbar;
    public Snackbar getStatusSnackbar() {
        return statusSnackbar;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Core.getInstance().bus().register(this);
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    public void showSnack(String message) {
        if (statusSnackbar == null) {
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            statusSnackbar = Snackbar.make(rootView, message,
                    Snackbar.LENGTH_INDEFINITE);
        } else {
            statusSnackbar.setText(message);
        }
        if (!statusSnackbar.isShown()) {
            statusSnackbar.show();
        }
    }

    public void setupSnackAction(String action, View.OnClickListener clickListener) {
        statusSnackbar.setAction("Retry Login", clickListener);
        statusSnackbar.setActionTextColor(getResources().getColor(R.color.colorBlue));
    }

    public void hideSnack() {
        statusSnackbar.dismiss();
    }

    @Subscribe
    public void onConnectionStateChanged(ConnectionStateChanged connectionStateChanged) {
        switch (connectionStateChanged.getState()) {
            case Connected:
                hideSnack();
                break;
            case Reconnecting:
                showSnack("Reconnecting to server");
                break;
        }
    }

    public static void insertMenuItemIcons(Context context, PopupMenu popupMenu) {
        Menu menu = popupMenu.getMenu();
        if (hasIcon(menu)) {
            for (int i = 0; i < menu.size(); i++) {
                insertMenuItemIcon(context, menu.getItem(i));
            }
        }
    }

    private static boolean hasIcon(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getIcon() != null) return true;
        }
        return false;
    }

    private static void insertMenuItemIcon(Context context, MenuItem menuItem) {
        Drawable icon = menuItem.getIcon();
        if (icon == null) icon = new ColorDrawable(Color.TRANSPARENT);
        else icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.menu_item_icon_size);
        icon.setBounds(0, 0, iconSize, iconSize);
        ImageSpan imageSpan = new ImageSpan(icon);
        SpannableStringBuilder ssb = new SpannableStringBuilder("       " + menuItem.getTitle());
        ssb.setSpan(imageSpan, 1, 2, 0);
        menuItem.setTitle(ssb);
        menuItem.setIcon(null);
    }

    public void showOptionsMenu(@MenuRes int menu, View anchor, PopupMenu.OnMenuItemClickListener listener) {
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, anchor);
        popupMenu.inflate(menu);
        insertMenuItemIcons(this, popupMenu);
        popupMenu.setOnMenuItemClickListener(listener);
        popupMenu.show();
    }
}
