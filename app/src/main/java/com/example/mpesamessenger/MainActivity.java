package com.example.mpesamessenger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private List<MpesaMessage> messageList;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Starts scrolled to the newest message at bottom
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Listen for incoming live SMS
        SmsReceiver.setListener((body, timestamp) -> runOnUiThread(() -> {
            messageList.add(new MpesaMessage(body, formatTime(timestamp)));
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        }));

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                    SMS_PERMISSION_CODE);
        } else {
            loadExistingMpesaMessages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadExistingMpesaMessages();
        } else {
            Toast.makeText(this, "SMS permission is needed to display M-Pesa messages.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadExistingMpesaMessages() {
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uri, new String[]{"address", "body", "date"},
                "address = ?", new String[]{"MPESA"}, "date ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                messageList.add(new MpesaMessage(body, formatTime(dateMillis)));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            if (!messageList.isEmpty()) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        }
    }

    private String formatTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}