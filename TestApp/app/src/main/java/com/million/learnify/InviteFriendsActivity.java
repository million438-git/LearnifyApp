package com.million.learnify;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class InviteFriendsActivity extends AppCompatActivity {

    private String referralLink;
    private static final String BASE_URL = "learnify.com/invite/";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openContactsPicker();
                } else {
                    Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    handleContactSelection(contactUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        generateReferralLink();
        setupToolbar();
        initViews();
    }

    private void generateReferralLink() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        referralLink = BASE_URL + code.toString();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Invite Friends");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        TextView tvLink = findViewById(R.id.tvReferralLink);
        tvLink.setText(referralLink);

        MaterialButton btnCopy = findViewById(R.id.btnCopyLink);
        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Referral Link", referralLink);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Link copied: " + referralLink, Toast.LENGTH_SHORT).show();
        });

        MaterialButton btnShare = findViewById(R.id.btnShareInvite);
        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Join me on Learnify! Use my link to get +50 XP: " + referralLink);
            startActivity(Intent.createChooser(intent, "Invite via"));
        });

        findViewById(R.id.btnSelectContacts).setOnClickListener(v -> checkContactsPermission());
    }

    private void checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            openContactsPicker();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void openContactsPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }

    private void handleContactSelection(Uri contactUri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String number = cursor.getString(numberIndex);
                String name = cursor.getString(nameIndex);

                sendSmsInvite(name, number);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to get contact info", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSmsInvite(String name, String number) {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + number));
        smsIntent.putExtra("sms_body", "Hi " + name + "! Join me on Learnify to study together. Use my link: " + referralLink);
        startActivity(smsIntent);
    }
}
