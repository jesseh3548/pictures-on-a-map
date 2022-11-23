package ece.course.reference;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

// Modified: changed profilePhoto type from int to Bitmap
public class  Person implements ClusterItem {
    public final String name;
    public final Bitmap profilePhoto;
    private final LatLng mPosition;

    public Person(LatLng position, String name, Bitmap pictureResource) {
        this.name = name;
        profilePhoto = pictureResource;
        mPosition = position;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
