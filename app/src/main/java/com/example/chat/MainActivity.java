package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.DecorToolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.Adapter.mesajIstekleriAdapter;
import com.example.chat.Fragment.KullanicilarFragment;
import com.example.chat.Fragment.MesajlarFragment;
import com.example.chat.Fragment.ProfilFragment;
import com.example.chat.Model.Kullanici;
import com.example.chat.Model.MesajIstegi;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBottomView;
    private KullanicilarFragment kullanicilarFragment;
    private MesajlarFragment mesajlarFragment;
    private ProfilFragment profilFragment;
    private FragmentTransaction transaction;
    private Toolbar mTToolbar;
    private ImageView appdenCik;
    private RelativeLayout mRelaNotiv;
    private TextView txtBildirimSayisi;
    private FirebaseFirestore mFireStore;
    private Query mQuery;
    private FirebaseUser mUser;
    private MesajIstegi mMesajIstegi;
    private ArrayList<MesajIstegi> mesajIstegiList;
    private Dialog mesajIstekleriDialog;
    private ImageView mesajIstekleriKapat;
    private RecyclerView mesajIstekleriRecyclerView;
    private mesajIstekleriAdapter mAdapter;
    private DocumentReference mRef;
    private Kullanici mKullanici;


    private void init(){
        mBottomView=(BottomNavigationView) findViewById(R.id.main_activity_bottomView);
        mesajIstegiList=new ArrayList<>();
        mFireStore=FirebaseFirestore.getInstance();
        mUser= FirebaseAuth.getInstance().getCurrentUser();
        mRef=mFireStore.collection("Kullanicilar").document(mUser.getUid());
        mRef.get().addOnSuccessListener(this    ,new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                    mKullanici=documentSnapshot.toObject(Kullanici.class);
            }
        });
        mesajlarFragment=new MesajlarFragment();
        profilFragment=new ProfilFragment();
        kullanicilarFragment=new KullanicilarFragment();
        fragmentiAyarla(kullanicilarFragment);
        mTToolbar=(Toolbar) findViewById(R.id.toolbar);
        mRelaNotiv=(RelativeLayout) findViewById(R.id.bar_layout_relaNotiv);
        txtBildirimSayisi=(TextView) findViewById(R.id.bar_layout_txtBildirimSayisi);
        appdenCik = (ImageView) findViewById(R.id.appdenCik);
    }
    //hesaptan çıkış yapar
    public void hesaptancik(View view){
        Intent cikis=new Intent(MainActivity.this,GirisYapActivity.class);
        startActivity(cikis);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        //mesajistekleri içerisinde ki istekler kısmında ki collection şekline döndürüp snapshotlistener ile ekledik
        mQuery=mFireStore.collection("Mesajİstekleri").document(mUser.getUid()).collection("İstekler");
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //hata varsa mainactivitye hatasını göstersin
                if(error!=null){
                    Toast.makeText(MainActivity.this,error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                //ya da değerler eksik değilse verileri mesaj istekleri listesine ekler
                if(value!=null){
                    txtBildirimSayisi.setText(String.valueOf(value.getDocuments().size()));
                    mesajIstegiList.clear();//liste silinir çünkü var olan veriler tekrar tekrar gösterilmesin
                    for(DocumentSnapshot snapshot:value.getDocuments()){
                        mMesajIstegi=snapshot.toObject(MesajIstegi.class);
                        mesajIstegiList.add(mMesajIstegi);
                    }
                }
            }
        });

        mRelaNotiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mesajIstekleriDialog();
            }
        });
        //fragmentler arası geçiş
        mBottomView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottom_nav_ic_people:
                        fragmentiAyarla(kullanicilarFragment);
                        mRelaNotiv.setVisibility(View.VISIBLE);
                        return true;
                    case R.id.bottom_nav_ic_profile:
                        mRelaNotiv.setVisibility(View.INVISIBLE);
                        fragmentiAyarla(profilFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void fragmentiAyarla(Fragment fragment){
        transaction= getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_activity_frameLayout,fragment);
        transaction.commit();
    }
    private void mesajIstekleriDialog(){
        //mesajlaşmak istediğimiz kişiye tıkladığımızda mesaj atabileceğimiz mini bir ekran çıkar
        mesajIstekleriDialog=new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        mesajIstekleriDialog.setContentView(R.layout.custom_diyalog_gelenmesaj_istekleri);
        mesajIstekleriKapat=mesajIstekleriDialog.findViewById(R.id.custom_diyalog_gelenMesaj_istekleri_imgKapat);
        mesajIstekleriRecyclerView=mesajIstekleriDialog.findViewById(R.id.custom_diyalog_gelenMesajistekleri_recylerView);
        mesajIstekleriKapat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mesajIstekleriDialog.dismiss();
            }
        });
        mesajIstekleriRecyclerView.setHasFixedSize(true);
        mesajIstekleriRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        if(mesajIstegiList.size()>0){
            mAdapter=new mesajIstekleriAdapter(mesajIstegiList,this,mKullanici.getKullaniciId(),mKullanici.getKullaniciIsmi(),mKullanici.getKullaniciProfil());
            mesajIstekleriRecyclerView.setAdapter(mAdapter);
        }
        mesajIstekleriDialog.show();
    }
}