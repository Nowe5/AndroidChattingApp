package com.example.a611_windows;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private String checker, myUrl;
    private String saveCurrentTime,saveCurrentDate;

    private GroupMessageAdapter groupMessageAdapter;
    private final List<GroupMessages> groupMessagesList = new ArrayList<>();
    private RecyclerView userMessagesList;
    private LinearLayoutManager linearLayoutManager;
    private ProgressDialog loadingBar;
    private TextView lastSeen;
    private CircleImageView profileImg, senderImg;

    private StorageTask uploadTask;
    private Uri fileUri;

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

        GroupMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();



        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        InitializeFields();

        groupName.setText(currentGroupName);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });


        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"

                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select the file");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(i==0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                        }
                        if(i==1){
                            checker = "pdf";
                        }
                        if(i==2){
                            checker = "docx";
                        }

                    }
                });

                builder.show();

            }
        });

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {

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
        lastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        profileImg = (CircleImageView) findViewById(R.id.custom_profile_IMAGE);

        profileImg.setVisibility(View.GONE);
        lastSeen.setVisibility(View.GONE);


        groupName = (TextView) findViewById(R.id.custom_profile_name);
        groupName.setVisibility(View.GONE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData() != null ){

            Log.d("FOTOĞRAF","1");
            loadingBar.setTitle("Set File");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            Log.d("FOTOĞRAF","2");
            fileUri = data.getData();

            if(!checker.equals("image")){

            }
            else if(checker.equals("image")){
                Log.d("FOTOĞRAF","3");
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

               // DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = GroupMessageKeyRef.push().getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                Log.d("FOTOĞRAF",fileUri.toString());
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();
                            Log.d("FOTOĞRAF",myUrl);

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);
                            messageTextBody.put("id", mAuth.getCurrentUser().getUid() );

                            Log.d("FOTOĞRAF","4");


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put("Groups/"+currentGroupName+ "/" +messagePushID,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Toast.makeText(GroupChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }

                                    userMessageInput.setText("");
                                }
                            });
                            Log.d("FOTOĞRAF","5");


                        }
                    }
                });

            }
            else{
                loadingBar.dismiss();
                Toast.makeText(this, "Error: Nothing is selected.", Toast.LENGTH_SHORT).show();
            }
        }
        Log.d("FOTOĞRAF","6");
    }

    private void SendMessage() {

        String messageText = userMessageInput.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "First write your message...", Toast.LENGTH_SHORT).show();

        } else {

            String messagePushID = GroupMessageKeyRef.push().getKey();

            String userName= mAuth.getCurrentUser().getDisplayName();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("id", mAuth.getCurrentUser().getUid() );
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


}
