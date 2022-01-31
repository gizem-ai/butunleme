package com.example.chat.Fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chat.Model.Kullanici;
import com.example.chat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfilFragment extends Fragment {

    private EditText editisim,editmail;
    private CircleImageView imgProfil;
    private View v;
    private FirebaseFirestore mFirestore;
    private DocumentReference ref;
    private FirebaseUser muser;
    private ImageView imgYeniresim;
    private Kullanici user;
    private static final int IZIN_KODU=0;
    private static final int IZINALINDI=1;
    private Intent galeriIntent;
    private Uri muri;
    private Bitmap gelenResim;
    private ImageDecoder.Source imgSource;
    private ByteArrayOutputStream outputStream;
    private byte[] imgByte;
    private StorageReference storageReference,yeniref,sref;
    private String kayityeri,indirmelinki;
    private HashMap<String,Object> mdata;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v= inflater.inflate(R.layout.fragment_profil, container, false);
        editisim=v.findViewById(R.id.profil_fragment_editIsim);
        editmail=v.findViewById(R.id.profil_fragment_editmail);
        imgProfil=v.findViewById(R.id.profil_fragment_imgUserProfil);
        imgYeniresim=v.findViewById(R.id.profil_fragment_YeniResim);
        muser= FirebaseAuth.getInstance().getCurrentUser();
        mFirestore=FirebaseFirestore.getInstance();
        storageReference=FirebaseStorage.getInstance().getReference();
        ref=mFirestore.collection("Kullanicilar").document(muser.getUid());
        ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Toast.makeText(v.getContext(),error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(value!=null&&value.exists()){
                    user=value.toObject(Kullanici.class);
                    if(user!=null){
                        editisim.setText(user.getKullaniciIsmi());
                        editmail.setText(user.getKullaniciEmail());
                        if(user.getKullaniciProfil().equals("default"))
                            imgProfil.setImageResource(R.mipmap.ic_launcher);
                        else
                            Picasso.get().load(user.getKullaniciProfil()).resize(156,156).into(imgProfil);
                    }
                }
            }
        });

        imgYeniresim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions((Activity)v.getContext(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},IZIN_KODU);
                else
                    galeriAc();
            }
        });

        return v;
    }
    private void galeriAc(){
        galeriIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galeriIntent,IZINALINDI);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == IZIN_KODU){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                galeriAc();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==IZINALINDI){
            if(data != null && data.getData() != null){
                muri=data.getData();
                try {
                    if(Build.VERSION.SDK_INT>=28){
                        imgSource=ImageDecoder.createSource(v.getContext().getContentResolver(),muri);
                        gelenResim=ImageDecoder.decodeBitmap(imgSource);
                    }else {
                        gelenResim=MediaStore.Images.Media.getBitmap(v.getContext().getContentResolver(),muri);
                    }

                    outputStream=new ByteArrayOutputStream();
                    gelenResim.compress(Bitmap.CompressFormat.PNG,75,outputStream);
                    imgByte=outputStream.toByteArray();
                    kayityeri="Kullanicilar/"+user.getKullaniciEmail()+"/profil.png";
                    sref=storageReference.child(kayityeri);
                    sref.putBytes(imgByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            yeniref=FirebaseStorage.getInstance().getReference(kayityeri);
                            yeniref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    indirmelinki=uri.toString();
                                    mdata=new HashMap<>();
                                    mdata.put("kullaniciProfil",indirmelinki);
                                    mFirestore.collection("Kullanicilar").document(muser.getUid()).update(mdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(v.getContext(), "Profil Fotosu Başarıyla güncellendi", Toast.LENGTH_SHORT).show();
                                            }else
                                                Toast.makeText(v.getContext(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(v.getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(v.getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}