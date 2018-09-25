package com.example.harshitjoshi.chatters.Ui.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.harshitjoshi.chatters.Ui.Activities.ProfileActivity;
import com.example.harshitjoshi.chatters.Models.Request;
import com.example.harshitjoshi.chatters.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment  {

    private RecyclerView mRequestRecycler;
    private FirebaseAuth mAuth;
    private View mRequestView;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mUserDatabase;
    private String mCurrent_user_id;
   // private CircleImageView profileImage;
   // private TextView status;


    private DatabaseReference mRequestDatabase;

    public RequestsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRequestView= inflater.inflate(R.layout.fragment_requests, container, false);
        mRequestRecycler=mRequestView.findViewById(R.id.requestRecycler);

//        profileImage=mRequestView.findViewById(R.id.single_user_image);
//        status=mRequestView.findViewById(R.id.single_status);

        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();

       // mRequestDatabase = mUserDatabase.child(mCurrent_user_id).child("Friend_req");
        mFriendRequestDatabase= FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mFriendRequestDatabase.keepSynced(true);
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users");
        mUserDatabase.keepSynced(true);
        mFriendRequestDatabase.keepSynced(true);
        mRequestRecycler.setHasFixedSize(true);
        mRequestRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return mRequestView;

    }

    @Override
    public void onStart() {
        super.onStart();
        Query conversationQuery = mFriendRequestDatabase.orderByChild("request_type").equalTo("received");
        final FirebaseRecyclerAdapter<Request,RequestsFragment.RequestViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                Request.class, R.layout.single_user_layout_file, RequestsFragment.RequestViewHolder.class, conversationQuery)
        {

            @Override
            protected void populateViewHolder(final RequestsFragment.RequestViewHolder viewHolder, final Request model, int position)
            {
                viewHolder.setRequestType(model.getRequest_type());
                final String list_user_id=getRef(position).getKey();
                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        String userStatus=dataSnapshot.child("status").getValue().toString();

                        viewHolder.setName(userName);
                        viewHolder.setUserImage(userImage);
                        viewHolder.setRequestType(userStatus);
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                profileIntent.putExtra("user_id",list_user_id);
                                startActivity(profileIntent);

                            }

                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRequestRecycler.setAdapter(firebaseRecyclerAdapter);
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public RequestViewHolder(View itemView)
        {
            super(itemView);
            mView=itemView;
        }
        public void setRequestType(String requestType)
        {
            TextView requestTypeView = mView.findViewById(R.id.single_status);
            requestTypeView.setText(requestType);
        }
        public void setName(String name)
        {
            TextView userNameView = mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);

        }
        public void setUserImage(String image) {

            CircleImageView userImageView = mView.findViewById(R.id.single_user_image);

            Picasso.get().load(image).placeholder(R.drawable.profile).into(userImageView);





        }


    }
}
