package com.example.chat.Model;

public class MesajIstegi {
    private String kanalID;
    private String kullaniciID;
    private String kullaniciProfil;
    private String kullaniciIsim;

    public MesajIstegi(String kanalID, String kullaniciID,String kullaniciIsim,String kullaniciProfil) {
        this.kanalID = kanalID;
        this.kullaniciID = kullaniciID;
        this.kullaniciProfil=kullaniciProfil;
        this.kullaniciIsim=kullaniciIsim;
    }

    public MesajIstegi() {

    }

    public String getKullaniciProfil() {
        return kullaniciProfil;
    }

    public void setKullaniciProfil(String kullaniciProfil) {
        this.kullaniciProfil = kullaniciProfil;
    }

    public String getKullaniciIsim() {
        return kullaniciIsim;
    }

    public void setKullaniciIsim(String kullaniciIsim) {
        this.kullaniciIsim = kullaniciIsim;
    }

    public String getKanalID() {
        return kanalID;
    }

    public void setKanalID(String kanalID) {
        this.kanalID = kanalID;
    }

    public String getKullaniciID() {
        return kullaniciID;
    }

    public void setKullaniciID(String kullaniciID) {
        this.kullaniciID = kullaniciID;
    }
}
