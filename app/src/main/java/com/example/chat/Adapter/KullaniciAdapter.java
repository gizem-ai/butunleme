package com.example.chat.Adapter;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.ChatActivity;
import com.example.chat.Model.Kullanici;
import com.example.chat.Model.MesajIstegi;
import com.example.chat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class KullaniciAdapter extends RecyclerView.Adapter<KullaniciAdapter.KullaniciHolder> {

    private ArrayList<Kullanici> mKullaniciList;
    private Context mContext;
    private View v;
    private Kullanici mKullanici;
    private int kpos;
    private Dialog mesajDialog;
    private ImageView imgIptal;
    private LinearLayout linearGonder;
    private CircleImageView imgProfil;
    private EditText editMesaj;
    private String txtmesaj;
    private TextView txtIsim;
    private Window mesajWindow;
    private FirebaseFirestore mFireStore;
    private DocumentReference mRef;
    private String mUID,mIsim,mProfilUrl,kanalID,mesajDocID;
    private MesajIstegi mesajIstegi;
    private HashMap<String,Object> mData;
    private Intent chatIntent;

    public KullaniciAdapter(ArrayList<Kullanici> mKullaniciList, Context mContext,String mUID,String mIsim,String mProfilUrl) {
        this.mKullaniciList = mKullaniciList;
        this.mContext = mContext;
        mFireStore=FirebaseFirestore.getInstance();
        this.mUID=mUID;
        this.mIsim=mIsim;
        this.mProfilUrl=mProfilUrl;
    }

    @NonNull
    @Override
    public KullaniciHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v= LayoutInflater.from(mContext).inflate(R.layout.kullanici_item,parent,false);
        return new KullaniciHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull KullaniciHolder holder, int position) {
        mKullanici = mKullaniciList.get(position);
        holder.kullaniciIsmi.setText(mKullanici.getKullaniciIsmi());
        if(mKullanici.getKullaniciProfil().equals("default"))//kullanıcının pp si yok ise mipmap deki foto atıldı
            holder.kullaniciProfili.setImageResource(R.mipmap.ic_launcher);
        else//boş değilse de 66,66 ya resize edip kullanıcının profilene atama yapıldı
            Picasso.get().load(mKullanici.getKullaniciProfil()).resize(66,66).into(holder.kullaniciProfili);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kpos=holder.getAdapterPosition();
                if(kpos!=RecyclerView.NO_POSITION){
                    mRef=mFireStore.collection("Kullanicilar").document(mUID).collection("Kanal").document(mKullaniciList.get(kpos).getKullaniciId());
                    mRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){//mesajlaşılacağı zaman bilgiler kanal dan çekilir
                                chatIntent=new Intent(mContext, ChatActivity.class);
                                chatIntent.putExtra("kanalID",documentSnapshot.getData().get("kanalID").toString());
                                chatIntent.putExtra("hedefId",mKullaniciList.get(kpos).getKullaniciId());
                                chatIntent.putExtra("hedefProfil",mKullaniciList.get(kpos).getKullaniciProfil());
                                chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mContext.startActivity(chatIntent);
                                //mesajlaşmaAktivite
                            }else   mesajGonderDialog(mKullaniciList.get(kpos));
                        }
                    });
                }
            }
            //ekranda tıklanılan kişiye ait bir mesaj kutusu çıkarmaya yarıyor
            private void mesajGonderDialog(final Kullanici kullanici) {
                mesajDialog=new Dialog(mContext);
                mesajDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                mesajWindow=mesajDialog.getWindow();
                mesajWindow.setGravity(Gravity.CENTER);
                //mesaj kutusunda gözükmesini istediğimiz özelliklerin kime ait olduğunu çektik
                mesajDialog.setContentView(R.layout.cutsom_dialog_mesaj_gonder);
                imgIptal=mesajDialog.findViewById(R.id.custom_dialog_mesaj_gonder_imgIptal);
                linearGonder=mesajDialog.findViewById(R.id.custom_dialog_mesaj_gonder_imgGonder);
                imgProfil=mesajDialog.findViewById(R.id.custom_dialog_mesaj_gonder_imgKullanici);
                editMesaj=mesajDialog.findViewById(R.id.custom_dialog_mesaj_gonder_editMesaj);
                txtIsim=mesajDialog.findViewById(R.id.custom_diyalog_mesaj_gonder_txtKullaniciIsim);
                txtIsim.setText(kullanici.getKullaniciIsmi());
                //açılacak olan mesaj kutusunun resim kısmı için eğer kullanici içinde kullaniciProfili default ise mipmapden otomatik bir foto konuyor
                if(kullanici.getKullaniciProfil().equals("default"))
                    imgProfil.setImageResource(R.mipmap.ic_launcher);
                else//değişse de Firedatabase den gelen fotoğrafın boyutunu 126,126 olarak ayarlayıp mesaj kutusunda gösteriyor
                    Picasso.get().load(kullanici.getKullaniciProfil()).resize(126,126).into(imgProfil);
                //açılan mesaj kutusunda x ya basıldığında diyalog kutusu kapanır
                imgIptal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mesajDialog.dismiss();
                    }
                });
                linearGonder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        txtmesaj=editMesaj.getText().toString();
                        if(!TextUtils.isEmpty(txtmesaj)){//mesaj gönderme işlemi
                            kanalID= UUID.randomUUID().toString();
                            //karşı tarafa mesaj isteği gönderdik
                            mesajIstegi=new MesajIstegi(kanalID,mUID,mIsim,mProfilUrl);
                            mFireStore.collection("Mesajİstekleri").document(kullanici.getKullaniciId()).collection("İstekler").document(mUID).set(mesajIstegi).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //chatbölümü
                                        mesajDocID=UUID.randomUUID().toString();
                                        mData=new HashMap<>();
                                        mData.put("mesajIcerigi",txtmesaj);
                                        mData.put("gonderen",mUID);
                                        mData.put("alici",kullanici.getKullaniciId());
                                        mData.put("mesajTipi","text");
                                        mData.put("mesajTarihi", FieldValue.serverTimestamp());
                                        mData.put("docID",mesajDocID);
                                        mFireStore.collection("ChatKanallari").document(kanalID).collection("Mesajalar").document(mesajDocID).set(mData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(mContext, "Mesaj isteğiniz iletildi", Toast.LENGTH_SHORT).show();
                                                    if(mesajDialog.isShowing())
                                                        mesajDialog.dismiss();
                                                    else
                                                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else
                                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else//kutu boş iken mesaj ikonuna basınca uyarı çıkar
                            Toast.makeText(mContext, "Boş mesaj gönderemezsiniz", Toast.LENGTH_SHORT).show();
                    }
                });
                mesajWindow.setLayout(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
                mesajDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mKullaniciList.size();
    }

    class KullaniciHolder extends RecyclerView.ViewHolder{
        TextView kullaniciIsmi;
        CircleImageView kullaniciProfili;

        public KullaniciHolder(@NonNull View itemView) {
            super(itemView);
            kullaniciIsmi=itemView.findViewById(R.id.kullanici_item_txtKullainiciIsmi);
            kullaniciProfili=itemView.findViewById(R.id.kullanici_item_imgKullainiciProfili);
        }
    }
}
