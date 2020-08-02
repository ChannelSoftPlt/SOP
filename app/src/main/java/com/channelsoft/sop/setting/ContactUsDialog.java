package com.channelsoft.sop.setting;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.channelsoft.sop.R;
import com.channelsoft.sop.others.SwipeDismissTouchListener;

import java.util.Objects;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.channelsoft.sop.shareObject.CustomToast.CustomToast;

public class ContactUsDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private Button contactUsDialogSendButton;
    private TextView contactUsDialogEmail, contactUsDialogPhone, contactUsDialogAddress;
    public static final int MY_PERMISSIONS_REQUEST_PHONE_CALL = 10;

    public ContactUsDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.contact_us_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        contactUsDialogSendButton = rootView.findViewById(R.id.contact_us_dialog_send_button);
        contactUsDialogEmail = rootView.findViewById(R.id.contact_us_dialog_email);
        contactUsDialogPhone = rootView.findViewById(R.id.contact_us_dialog_phone);
        contactUsDialogAddress = rootView.findViewById(R.id.contact_us_dialog_address);

    }

    private void objectSetting() {
        contactUsDialogSendButton.setOnClickListener(this);
        contactUsDialogEmail.setOnClickListener(this);
        contactUsDialogPhone.setOnClickListener(this);
        contactUsDialogAddress.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog d = getDialog();
        Objects.requireNonNull(d.getWindow()).getDecorView().setOnTouchListener(new SwipeDismissTouchListener(d.getWindow().getDecorView(), null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        dismiss();
                    }
                }));
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.contact_us_dialog_send_button:
                sendMessage();
                break;
            case R.id.contact_us_dialog_email:
                composeEmail();
                break;
            case R.id.contact_us_dialog_phone:
                phoneCallPermission();
                break;
            case R.id.contact_us_dialog_address:
                openMap();
                break;
        }
    }

    /*
     * send message
     * */
    private void sendMessage() {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(
                        "https://api.whatsapp.com/send?phone=60143157329,"
                )));
    }

    /*
     * send email
     * */
    public void composeEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle getActivity()
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"channelsoftmy@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Help");
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /*
     * open map
     * */
    public void openMap() {
        String uri = "";
        Intent intent;
        try {
            uri = "http://maps.google.co.in/maps?q=" + contactUsDialogAddress.getText().toString();
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                CustomToast(getActivity(), "This device is not support this action!");
            }
        }
    }

    public boolean checkPhoneCallPermission() {
        if (checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // getActivity() thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_PHONE_CALL);

            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_PHONE_CALL);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PHONE_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        onCall();
                    }
                } else {
                    Toast.makeText(getActivity(), "Unable to make a phone call with permission!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void phoneCallPermission() {
        if (checkPhoneCallPermission()) {
            if (checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                onCall();
            }
        }
    }

    public void onCall() {
        String phoneNo = contactUsDialogPhone.getText().toString().trim();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNo));    //getActivity() is the phone number calling

        if (checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //request permission from user if the app hasn't got the required permission
            requestPermissions(
                    new String[]{Manifest.permission.CALL_PHONE},   //request specific permission from user
                    MY_PERMISSIONS_REQUEST_PHONE_CALL);
        } else {     //have got permission
            try {
                startActivity(callIntent);  //call activity and make phone call
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "Invalid Number", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
