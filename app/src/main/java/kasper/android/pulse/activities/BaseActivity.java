package kasper.android.pulse.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.anadeainc.rxbus.Subscribe;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import kasper.android.pulse.R;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.rxbus.notifications.ConnectionStateChanged;

public class BaseActivity extends AppCompatActivity {

    private boolean isHome = false;
    public void thisIsHome() {
        this.isHome = true;
    }

    private Snackbar statusSnackbar;
    private SlidrInterface sliderInterface;

    public SlidrInterface getSliderInterface() {
        return sliderInterface;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!this.isHome)
            this.sliderInterface = Slidr.attach(this);
    }

    @Override
    public void onBackPressed() {
        hideSnack();
        super.onBackPressed();
    }

    public void showSnack(String message) {
        if (statusSnackbar == null) {
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            statusSnackbar = Snackbar.make(rootView, message,
                    Snackbar.LENGTH_INDEFINITE);
        } else {
            statusSnackbar.setText(message);
        }
        statusSnackbar.show();
    }

    public void showSnack(String message, String action, View.OnClickListener actionCallback) {
        if (statusSnackbar == null) {
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            statusSnackbar = Snackbar.make(rootView, message,
                    Snackbar.LENGTH_INDEFINITE);
        } else {
            statusSnackbar.setText(message);
        }
        statusSnackbar.setActionTextColor(getResources().getColor(R.color.colorBlue));
        statusSnackbar.setAction(action, actionCallback);
        statusSnackbar.show();
    }

    public void hideSnack() {
        if (statusSnackbar != null)
            statusSnackbar.dismiss();
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
