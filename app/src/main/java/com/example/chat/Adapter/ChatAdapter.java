package com.example.chat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.Model.Chat;
import com.example.chat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter  extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {

    private static final int MESAJ_SOL=1;
    private static final int MESAJ_SAG=0;

    private ArrayList<Chat> mchatlist;
    private Context mcontext;
    private String mUID;
    private View v;
    private Chat mchat;
    private String hedefProfil;


    public ChatAdapter(ArrayList<Chat> mchatlist, Context mcontext,String mUID,String hedefProfil) {
        this.mchatlist = mchatlist;
        this.mcontext = mcontext;
        this.mUID=mUID;
        this.hedefProfil=hedefProfil;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==MESAJ_SOL)
            v= LayoutInflater.from(mcontext).inflate(R.layout.chat_item_left,parent,false);
        else if (viewType==MESAJ_SAG)
            v=LayoutInflater.from(mcontext).inflate(R.layout.chat_item_right,parent,false);
        return new ChatHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        mchat=mchatlist.get(position);
        holder.txtMesaj.setText(mchat.getMesajIcerigi());
        if(!mchat.getGonderen().equals(mUID)){
            if(hedefProfil.equals("default"))
                holder.imgProfil.setImageResource(R.mipmap.ic_launcher);
            else
                Picasso.get().load(hedefProfil).resize(56,56).into(holder.imgProfil);
        }
    }
    @Override
    public int getItemCount() {
        return mchatlist.size();
    }

    class ChatHolder extends RecyclerView.ViewHolder{
        CircleImageView imgProfil;
        TextView txtMesaj;
        public ChatHolder(@NonNull View itemView){
            super(itemView);

           imgProfil=itemView.findViewById(R.id.chat_item_imgProfil);
           txtMesaj=itemView.findViewById(R.id.chat_item_txtMesaj);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mchatlist.get(position).getGonderen().equals(mUID))
            return MESAJ_SAG;
        else
            return MESAJ_SOL;
    }
}
