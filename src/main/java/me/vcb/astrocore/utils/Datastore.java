package me.vcb.astrocore.utils;

import me.vcb.astrocore.AstroCore;

public enum Datastore {

    JSON, MONGO;

    public static Datastore DATASTORE;

    static {
        String value = AstroCore.getInstance().getConfig().getString("DATASTORE");

        try {
            DATASTORE = Datastore.valueOf(value);
        } catch(IllegalArgumentException e) {
            DATASTORE = JSON;
        }
    }
}
