package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.Model.Kullanici;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ThrowOnExtraProperties;
import com.google.firebase.storage.FirebaseStorage;

public class KayitOlActivity extends AppCompatActivity {

    private Kullanici mKullanici;
    //firebase stora erişim için yüklemeler yapıldı
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    //kullanıcının girdiği değer
    private EditText kayit_ol_inputMail,kayit_ol_inputSifre,kayit_ol_inputIsim;
    private Button btnKayitOl;
    private TextView tw_girisFormunaGecis;
    private String txtIsim,txtEmail,txtSifre;
    private Toolbar mToolbar;

    private void init(){
        //firebaselere ulaşıldı
        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();
        kayit_ol_inputIsim=(EditText) findViewById(R.id.kayit_ol_inputIsim);
        kayit_ol_inputSifre=(EditText) findViewById(R.id.kayit_ol_inputSifre);
        kayit_ol_inputMail=(EditText) findViewById(R.id.kayit_ol_inputMail);
        btnKayitOl=(Button) findViewById(R.id.btnKayitOl);
        tw_girisFormunaGecis=(TextView) findViewById(R.id.tw_girisFormunaGecis);
        mToolbar=(Toolbar) findViewById(R.id.giris_kayit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit_ol);
        init();

    }
    //hesabı varsa giriş ol sayfasına geçer
    public void giriseGit(View v){
        Intent intent = new Intent(KayitOlActivity.this, GirisYapActivity.class);
        startActivity(intent);
        finish();
    }

    public void btnKayitOl(View v){
        //kullanıcı text'lere grdiği verileri string türünde girişleri değişkenlerde tuttuk
        txtIsim = kayit_ol_inputIsim.getText().toString();
        txtEmail = kayit_ol_inputMail.getText().toString();
        txtSifre = kayit_ol_inputSifre.getText().toString();
        //herhangi bir verinin boş olup olmama kontrolü yapıldı
        if(!TextUtils.isEmpty(txtIsim)){
            if(!TextUtils.isEmpty(txtEmail)){
                if(!TextUtils.isEmpty(txtSifre)){
                    //create ile yeni user oluşuyo
                    mAuth.createUserWithEmailAndPassword(txtEmail,txtSifre).addOnCompleteListener(KayitOlActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {//kullanıcı girdileri başarılı bir şekilde girdikten sonra veritabanına kayıt olacak
                            if(task.isSuccessful()){
                                mUser=mAuth.getCurrentUser();
                                if(mUser!=null) {
                                    //eğer bütün şartlar sağlanıyor ise firestoredatabasesinde Kullanici adı altında kişinin bütün bilgileri tutulur ve alttaki bilgi sırasıyla kayıt edilir
                                    mKullanici = new Kullanici(txtIsim, txtEmail, mUser.getUid(),"default");
                                    //kullanıcı kayıt olduğunda profil fotoğrafı tanımlamadığım için kullaniciProfil yerine default yazdırıldı
                                    mFirestore.collection("Kullanicilar").document(mUser.getUid()).set(mKullanici).addOnCompleteListener(KayitOlActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(KayitOlActivity.this, "Kayıt Başarılı", Toast.LENGTH_SHORT).show();
                                                finish();
                                                startActivity(new Intent(KayitOlActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                            }else
                                                Toast.makeText(KayitOlActivity.this, "Kayıt Başarısız", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }else
                                Toast.makeText(KayitOlActivity.this, "Başarısız", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else
                    kayit_ol_inputSifre.setError("Şifre Boş Olamaz");
            }else
                kayit_ol_inputMail.setError("Mail Boş Olamaz");
        }else
            kayit_ol_inputIsim.setError("Kullanıcı Adi Boş Olamaz");
    }

}