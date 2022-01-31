package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.Adapter.ChatAdapter;
import com.example.chat.Model.Chat;
import com.example.chat.Model.Kullanici;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private RecyclerView mRecyclerView;
    private EditText editmesaj;
    private String txtMesaj,docId;
    private CircleImageView hedefProfil;
    private TextView hedefIsim;
    private Intent gelenIntent;
    private String hedefId,kanalId,hedefProfilS;
    private DocumentReference hedefRef;
    private FirebaseFirestore mFS;
    private Kullanici hedefKullanici;
    private Query chatQuery;
    private ArrayList<Chat> mChatList;
    private Chat mChat;
    private ChatAdapter chatAdapter;
    private HashMap<String ,Object> mData;

    private void init(){
        mRecyclerView=(RecyclerView) findViewById(R.id.chat_activity_recyclerView);
        editmesaj=(EditText) findViewById(R.id.chat_activity_editMesaj);
        hedefIsim=(TextView) findViewById(R.id.chat_activity_txtHedefIsim);
        hedefProfil=(CircleImageView) findViewById(R.id.chat_activity_imgHedefProfil);
        mFS=FirebaseFirestore.getInstance();
        gelenIntent=getIntent();
        hedefId=gelenIntent.getStringExtra("hedefId");
        kanalId=gelenIntent.getStringExtra("kanalID");
        hedefProfilS=gelenIntent.getStringExtra("hedefProfil");
        mChatList=new ArrayList<>();
        mUser= FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();

        hedefRef=mFS.collection("Kullanicilar").document(hedefId);
        hedefRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(value!=null&&value.exists()){
                    hedefKullanici=value.toObject(Kullanici.class);//ilgili bilgi kullanıcıya gitti
                    if (hedefKullanici!=null){
                        hedefIsim.setText(hedefKullanici.getKullaniciIsmi());
                        if(hedefKullanici.getKullaniciProfil().equals("default"))//fotoğrafı defaultsa ic'i koy
                            hedefProfil.setImageResource(R.mipmap.ic_launcher);
                        else
                            Picasso.get().load(hedefKullanici.getKullaniciProfil()).resize(66,66).into(hedefProfil);//değilse firebaseden al koy

                    }
                }
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        chatQuery=mFS.collection("ChatKanallari").document(kanalId).collection("Mesajalar").orderBy("mesajTarihi", Query.Direction.ASCENDING);
        chatQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(value!=null){//mesaj verileri boş değilse alıp atıcaz
                    mChatList.clear();
                    for(DocumentSnapshot snapshot:value.getDocuments()){
                        mChat=snapshot.toObject(Chat.class);
                        if(mChat!=null)
                            mChatList.add(mChat);
                    }

                    chatAdapter=new ChatAdapter(mChatList,ChatActivity.this,mUser.getUid(),hedefProfilS);
                    mRecyclerView.setAdapter(chatAdapter);
                }
            }
        });
    }
    public void btnMesajGonder(View v){
        txtMesaj=editmesaj.getText().toString();
        if(!TextUtils.isEmpty(txtMesaj)){
            mData=new HashMap<>();
            docId= UUID.randomUUID().toString();
            mData.put("mesajIcerigi",txtMesaj);
            mData.put("gonderen",mUser.getUid());
            mData.put("alici",hedefId);
            mData.put("mesajTipi","text");
            mData.put("mesajTarihi", FieldValue.serverTimestamp());
            mData.put("docID",docId);

            mFS.collection("ChatKanallari").document(kanalId).collection("Mesajalar").document(docId).set(mData).addOnCompleteListener(ChatActivity.this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                        editmesaj.setText(" ");
                    else
                        Toast.makeText(ChatActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        }
        else
            Toast.makeText(ChatActivity.this, "Boş mesaj gönderilemez", Toast.LENGTH_SHORT).show();

    }
    public void btnChatKapat(View v){
        finish();
    }
}