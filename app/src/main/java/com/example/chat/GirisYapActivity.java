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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GirisYapActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btn_girisYap;
    private TextView tw_uyeOl;
    private FirebaseUser mUser;
    private EditText giris_yap_inputEmail,giris_yap_inputSifre;
    private String txtEmail,txtSifre;
    //private TextInputLayout giris_yap_inputSifre;
    private Toolbar mToolbar;

    private void init(){
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        tw_uyeOl=(TextView) findViewById(R.id.tw_uyeOl);
        giris_yap_inputSifre=(EditText) findViewById(R.id.giris_yap_inputSifre);
        giris_yap_inputEmail=(EditText) findViewById(R.id.giris_yap_inputEmail);
        btn_girisYap=(Button) findViewById(R.id.btn_girisYap);
        mToolbar=(Toolbar) findViewById(R.id.giris_kayit);
    }


    //kullanıcı mailini ve şifresin girdikten sonra kişi profiline girer
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris_yap);
        init();
    }
    //kullanıcının hesabı yok ise üye olmak için diğer activitye geçer
    public void uyeOlaGec(View v){
        Intent intent = new Intent(GirisYapActivity.this, KayitOlActivity.class);
        startActivity(intent);
        finish();
    }

    public void btnGirisYap(View v){
        //kullanıcı textlere mailini ve şifresini giriyor,
        txtEmail=giris_yap_inputEmail.getText().toString();
        txtSifre=giris_yap_inputSifre.getText().toString();
        //txtSifre=giris_yap_inputSifre.toString();
        if(!TextUtils.isEmpty(txtEmail)){
            if(!TextUtils.isEmpty(txtSifre)){
                //singin ile kullanıcı var mı varsa yani task.issuccessful ise giriş yapaibliyo
                mAuth.signInWithEmailAndPassword(txtEmail,txtSifre).addOnCompleteListener(GirisYapActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(GirisYapActivity.this, "Başarılı Giriş", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(GirisYapActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        }else
                            Toast.makeText(GirisYapActivity.this, "Başarısız", Toast.LENGTH_SHORT).show();
                    }
                });
            }else
                giris_yap_inputSifre.setError("Lütfen geçerli bir şifre adresi girin.");
        }else
            giris_yap_inputEmail.setError("Lütfen geçerli bir mail adresi girin.");
    }
}