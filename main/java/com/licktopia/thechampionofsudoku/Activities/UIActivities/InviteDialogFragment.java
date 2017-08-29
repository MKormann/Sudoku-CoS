package com.licktopia.thechampionofsudoku.Activities.UIActivities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.R;

import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.gameHelper;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;

/**
 * Created by Matt on 8/5/2017.
 */

public class InviteDialogFragment extends DialogFragment {

    public static InviteDialogFragment newInstance() {
        return new InviteDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        super.onCreateDialog(bundle);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View inviteDialogFragment;
        if(phoneSize==1) {
            inviteDialogFragment = getActivity().getLayoutInflater()
                    .inflate(R.layout.activity_invite_small, null);
        }
        else if(phoneSize == 2){
            inviteDialogFragment = getActivity().getLayoutInflater()
                    .inflate(R.layout.activity_invite_large, null);
        }
        else if(phoneSize == 3){
            inviteDialogFragment = getActivity().getLayoutInflater()
                    .inflate(R.layout.activity_invite_phablet, null);
        }
        else{
            inviteDialogFragment = getActivity().getLayoutInflater()
                    .inflate(R.layout.activity_invite_large, null);
        }

        builder.setView(inviteDialogFragment);
        builder.setTitle(getString(R.string.invite_dialog_title));

        Button acceptButton = (Button)inviteDialogFragment.findViewById(R.id.button_accept_popup_invitation);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gameHelper.getApiClient().isConnected()) {
                    getDialog().hide();
                    ((BaseActivity)getActivity()).acceptInviteToRoom(null);
                }
                else{
                    Toast.makeText(getContext(), R.string.must_be_logged_in,Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button declineButton = (Button)inviteDialogFragment.findViewById(R.id.decline_button);
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().hide();
            }
        });
        return builder.create();
    }
}
