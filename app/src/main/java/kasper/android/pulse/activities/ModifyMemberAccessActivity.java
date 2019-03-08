package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.rxbus.notifications.MemberAccessUpdated;
import kasper.android.pulse.rxbus.notifications.ShowToast;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

public class ModifyMemberAccessActivity extends AppCompatActivity {

    private Switch canCreateMessageSwitch;
    private Switch canSendInviteSwitch;
    private Switch canModifyWorkersSwitch;
    private Switch canUpdateProfilesSwitch;
    private Switch canModifyAccessSwitch;

    private long complexId;
    private long userId;

    private Entities.MemberAccess memberAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_member_access);

        if (getIntent().getExtras() != null) {
            complexId = getIntent().getExtras().getLong("complex_id");
            userId = getIntent().getExtras().getLong("user_id");
        }

        memberAccess = DatabaseHelper.getMemberAccessByComplexAndUserId(complexId, userId);

        initViews();

        canCreateMessageSwitch.setChecked(memberAccess.isCanCreateMessage());
        canSendInviteSwitch.setChecked(memberAccess.isCanSendInvite());
        canModifyWorkersSwitch.setChecked(memberAccess.isCanModifyWorkers());
        canUpdateProfilesSwitch.setChecked(memberAccess.isCanUpdateProfiles());

        Entities.Complex complex = DatabaseHelper.getComplexById(complexId);
        Entities.User me = DatabaseHelper.getMe();
        if (me != null && complex.getComplexSecret() != null &&
                complex.getComplexSecret().getAdminId() == me.getBaseUserId())
            canModifyAccessSwitch.setChecked(memberAccess.isCanModifyAccess());
        else
            canModifyAccessSwitch.setVisibility(View.GONE);

        initListeners();
    }

    private void initViews() {
        canCreateMessageSwitch = findViewById(R.id.canCreateMessageSwitch);
        canSendInviteSwitch = findViewById(R.id.canSendInviteSwitch);
        canModifyWorkersSwitch = findViewById(R.id.canModifyWorkersSwitch);
        canUpdateProfilesSwitch = findViewById(R.id.canUpdateProfilesSwitch);
        canModifyAccessSwitch = findViewById(R.id.canModifyAccessSwitch);
    }

    private void initListeners() {
        canCreateMessageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateServer());
        canSendInviteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateServer());
        canModifyWorkersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateServer());
        canUpdateProfilesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateServer());
        canModifyAccessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateServer());
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }

    private void updateServer() {
        LogHelper.log("Aseman", "hello 1");
        memberAccess.setCanCreateMessage(canCreateMessageSwitch.isChecked());
        memberAccess.setCanSendInvite(canSendInviteSwitch.isChecked());
        memberAccess.setCanModifyWorkers(canModifyWorkersSwitch.isChecked());
        memberAccess.setCanUpdateProfiles(canUpdateProfilesSwitch.isChecked());
        memberAccess.setCanModifyAccess(canModifyAccessSwitch.isChecked());
        Packet packet = new Packet();
        Entities.Complex complex = new Entities.Complex();
        complex.setComplexId(complexId);
        packet.setComplex(complex);
        Entities.User user = new Entities.User();
        user.setBaseUserId(userId);
        packet.setUser(user);
        Entities.MemberAccess ma = new Entities.MemberAccess();
        ma.setCanCreateMessage(memberAccess.isCanCreateMessage());
        ma.setCanSendInvite(memberAccess.isCanSendInvite());
        ma.setCanModifyWorkers(memberAccess.isCanModifyWorkers());
        ma.setCanUpdateProfiles(memberAccess.isCanUpdateProfiles());
        ma.setCanModifyAccess(memberAccess.isCanModifyAccess());
        packet.setMemberAccess(ma);
        NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(ComplexHandler.class).updateMemberAccess(packet)
                , new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {

                        LogHelper.log("Aseman", "hello 2");

                        DatabaseHelper.notifyMemberAccessCreated(memberAccess);
                        Core.getInstance().bus().post(new MemberAccessUpdated(memberAccess));
                    }
                    @Override
                    public void onServerFailure() {
                        Core.getInstance().bus().post(new ShowToast("modifying member access failure"));
                    }
                    @Override
                    public void onConnectionFailure() {
                        Core.getInstance().bus().post(new ShowToast("modifying member access failure"));
                    }
                });
    }
}
