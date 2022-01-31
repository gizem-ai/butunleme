package com.example.chat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.Model.MesajIstegi;
import com.example.chat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class mesajIstekleriAdapter extends RecyclerView.Adapter<mesajIstekleriAdapter.mesajIstekleriHolder> {
    private ArrayList<MesajIstegi> mMesajIstegiList;
    private Context mcontext;
    private MesajIstegi mesajIstegi,yeniMesajistegi;
    private View v;
    private int mPos;
    private FirebaseFirestore mFirestore;
    private String mUID,mIsim,mProfilUrl;


    public mesajIstekleriAdapter(ArrayList<MesajIstegi> mMesajIstegiList, Context mcontext,String mUID,String mIsim,String mProfilUrl) {
        this.mMesajIstegiList = mMesajIstegiList;
        this.mcontext = mcontext;
        this.mUID=mUID;
        this.mIsim=mIsim;
        this.mProfilUrl=mProfilUrl;
        mFirestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public mesajIstekleriHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v= LayoutInflater.from(mcontext).inflate(R.layout.gelen_mesaj_istekleri_item,parent,false);
        return new mesajIstekleriHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull mesajIstekleriHolder holder, int position) {
        mesajIstegi= mMesajIstegiList.get(position);
        holder.txtMesaj.setText(mesajIstegi.getKullaniciIsim()+" kullanıcısı mesaj göndermek istiyor");
        if(mesajIstegi.getKullaniciID().equals("default"))
            holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
        else
            Picasso.get().load(mesajIstegi.getKullaniciProfil()).resize(77,77).into(holder.imgProfile);

        holder.imgOnay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPos=holder.getAdapterPosition();
                if (mPos!=RecyclerView.NO_POSITION){
                    //kullanıcıların kanal kısmı istek mesajların tutulduğu yer. burda ki verilerden kişinin adı çekilip kullanıcıya göseriliyo. kullanıcı
                    //onaylarsa eğer mesajlar fragmentinde kişiye ait bir mesaj kutusu oluşur ve ordan bire bir sohbete başlarlar
                    yeniMesajistegi=new MesajIstegi(mMesajIstegiList.get(mPos).getKanalID(),mMesajIstegiList.get(mPos).getKullaniciID(),mMesajIstegiList.get(mPos).getKullaniciIsim(),mMesajIstegiList.get(mPos).getKullaniciProfil());
                    mFirestore.collection("Kullanicilar").document(mUID).collection("Kanal").document(mMesajIstegiList.get(mPos).getKullaniciID()).set(yeniMesajistegi).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                yeniMesajistegi=new MesajIstegi(mMesajIstegiList.get(mPos).getKanalID(),mUID,mIsim,mProfilUrl);
                                mFirestore.collection("Kullanicilar").document(mMesajIstegiList.get(mPos).getKullaniciID()).collection("Kanal").document(mUID).set(yeniMesajistegi).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())//
                                            mesajisteginiSil(mMesajIstegiList.get(mPos).getKullaniciID(),"Mesaj isteği kabul edildi");
                                        else
                                            Toast.makeText(mcontext,task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else
                                Toast.makeText(mcontext,task.getException().getMessage() , Toast. LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        //kullanıcı gelen mesaj isteğini reddedebilir
        holder.imgIptal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPos=holder.getAdapterPosition();
                if (mPos!=RecyclerView.NO_POSITION)
                     mesajisteginiSil(mMesajIstegiList.get(mPos).getKullaniciID(),"Mesaj isteği reddedildi.");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMesajIstegiList.size();
    }

    class mesajIstekleriHolder extends RecyclerView.ViewHolder{
        CircleImageView imgProfile;
        TextView txtMesaj;
        ImageView imgIptal,imgOnay;
        public mesajIstekleriHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile=itemView.findViewById(R.id.gelen_mesajistekleri_item_imgProfil);
            txtMesaj=itemView.findViewById(R.id.gelenMesaj_istekleri_txtMesaj);
            imgIptal=itemView.findViewById(R.id.gelen_mesaj_istekleri_imgIptal);
            imgOnay=itemView.findViewById(R.id.gelen_mesaj_istekleri_imgOnayla);
        }
    }
private void mesajisteginiSil(String hesafUid,final String mesajicerigi){
        mFirestore.collection("Mesajİstekleri").document(mUID).collection("İstekler").document(hesafUid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    notifyDataSetChanged();//anlık veri değişikliğini kontrol eder
                    Toast.makeText(mcontext, mesajicerigi, Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(mcontext,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
