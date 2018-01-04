package com.example.tlabuser.musicapplication.Model;

/**
 * inner class of ExTrack
 */

public class ExTrackSituation {
    public String situation;    // situation name
    public int    weight;       // situation weight (weight_d + weight_u)
    public int    weight_d;     // situation weight (default by server)
    public int    weight_u;     // situation weight (feedback by user)
}
