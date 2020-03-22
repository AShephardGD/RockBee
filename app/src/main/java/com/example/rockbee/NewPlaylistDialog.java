package com.example.rockbee;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class NewPlaylistDialog extends DialogFragment {
    EditText ed;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.playlists_alert_dialog, null);
        ed = view.findViewById(R.id.newPlaylistName);
        builder.setNegativeButton(getResources().getText(R.string.addNowPlays), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((MainActivity)getActivity()).playlistFromNowPlays(ed.getText().toString());
            }
        });
        builder.setPositiveButton(getResources().getText(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((MainActivity)getActivity()).setNewPlaylistName(ed.getText().toString());
            }
        });
        builder.setNeutralButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setTitle(getResources().getText(R.string.newPlaylist));
        builder.setView(view);
        return builder.create();
    }
}
