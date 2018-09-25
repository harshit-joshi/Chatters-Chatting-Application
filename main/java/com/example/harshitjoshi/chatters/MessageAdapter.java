package com.example.harshitjoshi.chatters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.harshitjoshi.chatters.Models.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

//    @Override
//    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
//
//        mAuth = FirebaseAuth.getInstance();
//        String current_user_id=mAuth.getCurrentUser().getUid();
//        Messages messages = mMessageList.get(position);
//        String from_user=messages.getFrom();
//        if(from_user.equals(current_user_id))
//        {
//            holder.messageText.setBackgroundColor(Color.BLACK);
//            holder.messageText.setTextColor(Color.YELLOW);
//
//        }
//        else
//        {
//            holder.messageText.setBackgroundResource(R.drawable.messege_text_background);
//            holder.messageText.setTextColor(Color.BLACK);
//        }
//        holder.messageText.setText(messages.getMessage());
//    }
//
//    @Override
//    public int getItemCount() {
//        return mMessageList.size();
//    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            displayName=itemView.findViewById(R.id.name_text_layout);
            messageText = itemView.findViewById(R.id.message_text);
            profileImage = itemView.findViewById(R.id.message_profile);
            messageImage=itemView.findViewById(R.id.message_image_layout);

        }
    }
    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String thumb_nail = dataSnapshot.child("thumb_nail").getValue().toString();

                viewHolder.displayName.setText(name);
                Picasso.get().load(thumb_nail).placeholder(R.drawable.profile).error(R.drawable.profile).into(viewHolder.profileImage);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {

            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);


        } else
            {

            viewHolder.messageText.setVisibility(View.INVISIBLE);
                Picasso.get().load(c.getMessage()).fit().networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(viewHolder.messageImage);

            }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}