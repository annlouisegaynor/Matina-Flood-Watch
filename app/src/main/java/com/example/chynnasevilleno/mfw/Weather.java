package com.example.chynnasevilleno.mfw;

public class Weather {
    String id;
    String rainfall;
    String rh;
    String meant;
    String maxt;
    String mint;
    String spress;
    String windspeed;
    String floodlvl;

    public Weather(){

    }

    public Weather(String id, String rainfall, String rh, String meant, String maxt,
                   String mint,String spress, String windspeed, String floodlvl) {
        this.id = id;
        this.rainfall = rainfall;
        this.rh = rh;
        this.meant = meant;
        this.maxt = maxt;
        this.mint = mint;
        this.spress = spress;
        this.windspeed = windspeed;
        this.floodlvl = floodlvl;
    }

    public String getId() {
        return id;
    }

    public String getRainfall() {
        return rainfall;
    }


    public String getRh() {
        return rh;
    }


    public String getMeant() {
        return meant;
    }


    public String getMaxt() {
        return maxt;
    }


    public String getMint() {
        return mint;
    }

    public String getSpress() {
        return spress;
    }


    public String getWindspeed() {
        return windspeed;
    }


    public String getFloodlvl() {
        return floodlvl;
    }
}
