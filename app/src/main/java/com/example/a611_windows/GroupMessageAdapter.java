package com.example.a611_windows;


import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder> {

    private List<GroupMessages> userMessagesList;
    private FirebaseAuth mAuth;
    private String currentUserID, nickName;

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    private String groupName;
    private DatabaseReference GroupRef, UsersRef;

    public GroupMessageAdapter (List<GroupMessages> userMessagesList){

        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText,receiverMessageText;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            messageReceiverPicture =(ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture =(ImageView) itemView.findViewById(R.id.message_sender_image_view);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        Log.d("bilgi","currentUserID -----"+currentUserID);


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {

        GroupMessages messages = userMessagesList.get(i);

        String fromMessageType = messages.getType();
        String messageSenderID = messages.getId();

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(messageSenderID);


        UsersRef.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                nickName = snapshot.getValue().toString();  //prints "Do you have data? You'll love Firebase."
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        messageViewHolder.receiverMessageText.setVisibility(View.GONE);

        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);

        if(fromMessageType.equals("text")){


            if(currentUserID.equals(messageSenderID)){
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                //messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage()+ "\n \n" + messages.getTime()+ "-" + messages.getDate());
            }
            else{

                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);


                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                //messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(nickName+"\n"+messages.getMessage()+ "\n \n" + messages.getTime()+ "-" + messages.getDate());
            }
        }

        else if(fromMessageType.equals("image")){

            if(currentUserID.equals(messageSenderID)){
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
            }
            else{
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


}