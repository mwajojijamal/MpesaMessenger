package com.example.mpesamessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    public interface OnMpesaReceivedListener {
        void onMpesaReceived(String body, long timestamp);
    }

    private static OnMpesaReceivedListener listener;

    public static void setListener(OnMpesaReceivedListener l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");
                if (pdus != null) {
                    StringBuilder fullMessage = new StringBuilder();
                    String sender = "";
                    long timestamp = System.currentTimeMillis();

                    for (Object pdu : pdus) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                        sender = sms.getDisplayOriginatingAddress();
                        fullMessage.append(sms.getMessageBody());
                        timestamp = sms.getTimestampMillis();
                    }

                    if (sender != null && sender.equalsIgnoreCase("MPESA")) {
                        if (listener != null) {
                            listener.onMpesaReceived(fullMessage.toString(), timestamp);
                        }
                    }
                }
            }
        }
    }
}