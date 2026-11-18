package com.example.mpesamessenger;

import android.graphics.BlurMaskFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.MaskFilterSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<MpesaMessage> messageList;

    // Matches "New M-PESA balance is Ksh12,345.00" (case-insensitive)
    private static final Pattern BALANCE_PATTERN = Pattern.compile(
            "(New M-PESA balance is\\s*)(Ksh[\\d,]+\\.\\d{2})",
            Pattern.CASE_INSENSITIVE
    );

    public MessageAdapter(List<MpesaMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MpesaMessage msg = messageList.get(position);
        holder.textTime.setText(msg.getTime());

        // Hardware acceleration must be disabled on the TextView for BlurMaskFilter to render
        holder.textBody.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        if (msg.isBlurred()) {
            holder.textBody.setText(getBlurredText(msg.getBody()));
        } else {
            holder.textBody.setText(msg.getBody());
        }

        // Allow user to tap a bubble to temporarily reveal/re-blur the balance
        holder.itemView.setOnClickListener(v -> {
            msg.toggleBlur();
            notifyItemChanged(position);
        });
    }

    private SpannableString getBlurredText(String rawText) {
        SpannableString spannable = new SpannableString(rawText);
        Matcher matcher = BALANCE_PATTERN.matcher(rawText);

        while (matcher.find()) {
            int start = matcher.start(2); // Start of "Ksh..."
            int end = matcher.end(2);     // End of ".00"
            BlurMaskFilter blurFilter = new BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL);
            spannable.setSpan(new MaskFilterSpan(blurFilter), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textBody, textTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textBody = itemView.findViewById(R.id.textMessageBody);
            textTime = itemView.findViewById(R.id.textMessageTime);
        }
    }
}