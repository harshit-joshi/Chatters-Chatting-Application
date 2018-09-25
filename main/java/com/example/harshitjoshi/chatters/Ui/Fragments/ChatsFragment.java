package com.example.harshitjoshi.chatters.Ui.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harshitjoshi.chatters.Models.Conversation;
import com.example.harshitjoshi.chatters.R;
import com.example.harshitjoshi.chatters.Ui.Activities.ChatActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;
     FirebaseRecyclerAdapter<Conversation, ConvViewHolder> firebaseConvAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);




        // Inflate the layout
        return mMainView;
    }



    @Override
    public void onStart() {
        super.onStart();

        final Query conversationQuery = mConvDatabase.orderByChild("timestamp");

       firebaseConvAdapter = new FirebaseRecyclerAdapter<Conversation, ConvViewHolder>(
                Conversation.class,
                R.layout.single_user_layout_file,
                ConvViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conversation conv, int i)
            {
                final String list_user_id = getRef(i).getKey();
                final Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(2);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        convViewHolder.setMessage(data, conv.isSeen());

//                        convViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
//                            @Override
//                            public boolean onLongClick(View v) {
//                                CharSequence options[]=new CharSequence[]{"Delete","Open Message"};
//                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
//                                //builder.setTitle("Select option");
//                                builder.setItems(options, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which)
//                                    {
//                                        if(which==0)
//                                        {
//                                            mMessageDatabase.child(list_user_id).removeValue();
//                                            mConvDatabase.child(list_user_id).removeValue();
//                                           updatelist();
//
//
//
//
//                                        }
//                                        if(which==1)
//                                        {
//                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
//                                            chatIntent.putExtra("user_id",list_user_id);
//                                           // chatIntent.putExtra("user_name",userName);
//                                            startActivity(chatIntent);
//                                        }
//
//                                    }
//                                });
//                                builder.show();
//                                return true;
//                            }
//                        });

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                convViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final CharSequence options[]=new CharSequence[]{"Clear Chat","Open Message"};
                        final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                        //builder.setTitle("Select option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which==0)
                                {

                                            mMessageDatabase.child(list_user_id).removeValue();
                                            mConvDatabase.child(list_user_id).removeValue();
                                            firebaseConvAdapter.notifyItemRemoved(getId());
                                            convViewHolder.getAdapterPosition();
                                }
                                if(which==1)
                                {
                                    Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("user_id",list_user_id);
                                    // chatIntent.putExtra("user_name",userName);
                                    startActivity(chatIntent);
                                }

                            }
                        });
                        builder.show();
                        return true;
                    }
                });




                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_nail").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            convViewHolder.setUserOnline(userOnline);

                        }

                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb);

                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mConvList.setAdapter(firebaseConvAdapter);

    }

//    private void updatelist(){
//        if(firebaseConvAdapter!=null){
//            firebaseConvAdapter.notifyItemRemoved();
//        }
//    }
    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = mView.findViewById(R.id.single_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                userStatusView.setTextColor(Color.BLUE);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image){

            CircleImageView userImageView = mView.findViewById(R.id.single_user_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profile).into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView =mView.findViewById(R.id.onlineStatusImage);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }



        }

        public void deleteSingleChat()
        {

        }


    }


