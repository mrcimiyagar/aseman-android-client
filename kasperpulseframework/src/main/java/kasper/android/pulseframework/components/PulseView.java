package kasper.android.pulseframework.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kasper.android.pulseframework.engines.EREngine;
import kasper.android.pulseframework.engines.UiAnimatorEngine;
import kasper.android.pulseframework.engines.UiInitiatorEngine;
import kasper.android.pulseframework.engines.UiUpdaterEngine;
import kasper.android.pulseframework.interfaces.IClickNotifier;
import kasper.android.pulseframework.locks.Locks;
import kasper.android.pulseframework.models.Anims;
import kasper.android.pulseframework.models.Bindings;
import kasper.android.pulseframework.models.Codes;
import kasper.android.pulseframework.models.Controls;
import kasper.android.pulseframework.models.Tuple;
import kasper.android.pulseframework.models.Updates;
import kasper.android.pulseframework.utils.GraphicsHelper;

public class PulseView extends RelativeLayout {

    private Hashtable<String, Pair<Controls.Control, View>> idTable;
    private UiInitiatorEngine uiInitiatorEngine;
    private UiUpdaterEngine uiUpdaterEngine;
    private UiAnimatorEngine uiAnimatorEngine;
    private EREngine erEngine;

    public PulseView(Context context) {
        super(context);
        init();
    }

    public PulseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PulseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PulseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.idTable = new Hashtable<>();
        GraphicsHelper.setup(getContext());
        this.setFocusableInTouchMode(true);
    }

    public void setup(AppCompatActivity activity, IClickNotifier controlClickNotifier) {
        Locks.setup(activity::runOnUiThread);
        this.uiInitiatorEngine = new UiInitiatorEngine(
                getContext(),
                activity::runOnUiThread,
                controlClickNotifier);
        this.uiUpdaterEngine = new UiUpdaterEngine(
                getContext(),
                activity::runOnUiThread,
                controlClickNotifier);
        this.uiAnimatorEngine = new UiAnimatorEngine(
                update -> uiUpdaterEngine.updateUi(idTable, update),
                activity::runOnUiThread);
        this.erEngine = new EREngine(
                (mirror, value) -> uiUpdaterEngine.handleMirrorEffect(idTable, mirror, value),
                (anim) -> uiAnimatorEngine.animateUi(idTable, anim));
    }

    public void buildUi(Controls.Control control) {
        this.removeAllViews();
        Tuple<View, List<Pair<Controls.Control, View>>
                , Hashtable<String, Pair<Controls.Control, View>>> result =
                uiInitiatorEngine.buildViewTree(Controls.PanelCtrl.LayoutType.RELATIVE, control);
        View view = result.getItem1();
        idTable = result.getItem3();
        this.addView(view);
    }

    public void buildUi(String json) {
        try {
            Controls.Control root = initMapper().readValue(json, Controls.Control.class);
            buildUi(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateUi(String json) {
        try {
            Updates.Update update = initMapper().readValue(json, Updates.Update.class);
            updateUi(update);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateUi(Updates.Update update) {
        uiUpdaterEngine.updateUi(idTable, update);
    }

    public void updateBatchUi(String json) {
        try {
            List<Updates.Update> updates = initMapper().readValue(json, new TypeReference<List<Updates.Update>>(){});
            updateBatchUi(updates);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateBatchUi(List<Updates.Update> updates) {
        uiUpdaterEngine.updateBatchUi(idTable, updates);
    }

    public void animateUi(Anims.Anim anim) {
        uiAnimatorEngine.animateUi(idTable, anim);
    }

    public void animateUi(String json) {
        try {
            Anims.Anim anim = initMapper().readValue(json, Anims.Anim.class);
            animateUi(anim);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void animateBatchUi(List<Anims.Anim> anims) {
        uiAnimatorEngine.animateBatchUi(idTable, anims);
    }

    public void animateBatchUi(String json) {
        try {
            List<Anims.Anim> anims = initMapper().readValue(json, new TypeReference<List<Anims.Anim>>(){});
            animateBatchUi(anims);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runCommand(Codes.Code code) {
        this.runCommands(Collections.singletonList(code));
    }

    public void runCommands(List<Codes.Code> codes) {
        this.erEngine.run(codes);
    }

    public void runCommand(String json) {
        try {
            Codes.Code code = initMapper().readValue(json, Codes.Code.class);
            runCommand(code);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runCommands(String json) {
        try {
            List<Codes.Code> codes = initMapper().readValue(json, new TypeReference<List<Codes.Code>>(){});
            this.runCommands(codes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void modifyMirror(Bindings.Mirror mirror) {
        this.erEngine.modifyMirror(mirror);
    }

    public void modifyMirror(String json) {
        try {
            Bindings.Mirror mirror = initMapper().readValue(json, Bindings.Mirror.class);
            modifyMirror(mirror);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ObjectMapper initMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
