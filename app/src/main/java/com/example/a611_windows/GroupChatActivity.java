package com.example.a611_windows;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private ImageButton SendMessageButton, SendFilesButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages, groupName;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef, RootRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private String checker ="";
    private String saveCurrentTime,saveCurrentDate;

    private GroupMessageAdapter groupMessageAdapter;
    private final List<GroupMessages> groupMessagesList = new ArrayList<>();
    private RecyclerView userMessagesList;
    private LinearLayoutManager linearLayoutManager;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);



        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        GroupMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName).push();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();



        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        InitializeFields();

        groupName.setText(currentGroupName);

        //GetUserInfo();

        /*SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });*/
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    //DisplayMessages(dataSnapshot);

                    GroupMessages messages = dataSnapshot.getValue(GroupMessages.class);
                    groupMessagesList.add(messages);
                    groupMessageAdapter.notifyDataSetChanged();


                    userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void InitializeFields()
    {
        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar2);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        getSupportActionBar().setTitle(currentGroupName);


        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn2);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn2);
        userMessageInput = (EditText) findViewById(R.id.input_message2);

        groupName = (TextView) findViewById(R.id.custom_profile_name);
        groupMessageAdapter = new GroupMessageAdapter(groupMessagesList);
        groupMessageAdapter.setGroupName(currentGroupName);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users2);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager); //bunları kapatınca mesajlar görünmüyor
        userMessagesList.setAdapter(groupMessageAdapter);       //

        //loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());




    }

    private void SendMessage() {

        String messageText = userMessageInput.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "First write your message...", Toast.LENGTH_SHORT).show();

        } else {




            String messagePushID = GroupMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("name", "" );
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put("Groups/"+currentGroupName+ "/" +messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(GroupChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    userMessageInput.setText("");
                }
            });

        }
    }
    /*private void SaveMessageInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Cant send empty Message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy"); //date format değiştirebiliriz!
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a"); //time format değiştirebiliriz! büyük H belki
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("message", message);
                messageInfoMap.put("type",checker);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

        private void GetUserInfo ()
        {
            UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        currentUserName = dataSnapshot.child("name").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
*/

}
