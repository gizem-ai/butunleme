package com.example.chat.Model;

public class Chat {
    private String mesajIcerigi,gonderen,alici,mesajTip,docIDi;

    public Chat(String mesajIcerigi, String gonderen, String alici, String mesajTip, String docIDi) {
        this.mesajIcerigi = mesajIcerigi;
        this.gonderen = gonderen;
        this.alici = alici;
        this.mesajTip = mesajTip;
        this.docIDi = docIDi;
    }

    public Chat() {
    }

    public String getMesajIcerigi() {
        return mesajIcerigi;
    }

    public void setMesajIcerigi(String mesajIcerigi) {
        this.mesajIcerigi = mesajIcerigi;
    }

    public String getGonderen() {
        return gonderen;
    }

    public void setGonderen(String gonderen) {
        this.gonderen = gonderen;
    }

    public String getAlici() {
        return alici;
    }

    public void setAlici(String alici) {
        this.alici = alici;
    }

    public String getMesajTip() {
        return mesajTip;
    }

    public void setMesajTip(String mesajTip) {
        this.mesajTip = mesajTip;
    }

    public String getDocIDi() {
        return docIDi;
    }

    public void setDocIDi(String docIDi) {
        this.docIDi = docIDi;
    }
}
