package ece.course.reference;

import static android.os.Environment.getExternalStoragePublicDirectory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;

/**
 * Demonstrates heavy customisation of the look of rendered clusters.
 */
public class
CustomMarkerClusteringDemoActivity extends BaseDemoActivity implements ClusterManager.OnClusterClickListener<Person>, ClusterManager.OnClusterInfoWindowClickListener<Person>, ClusterManager.OnClusterItemClickListener<Person>, ClusterManager.OnClusterItemInfoWindowClickListener<Person> {
    private ClusterManager<Person> mClusterManager;
    private Random mRandom = new Random(1984);

    /**
     * Draws profile photos inside
     * s (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull Person person, MarkerOptions markerOptions) {
            // Draw a single person - show their profile photo and set the info window to show their name
            markerOptions
                    .icon(getItemIcon(person))
                    .title(person.name);
        }

        @Override
        protected void onClusterItemUpdated(@NonNull Person person, Marker marker) {
            // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
            marker.setIcon(getItemIcon(person));
            marker.setTitle(person.name);
        }

        /**
         * Get a descriptor for a single person (i.e., a marker outside a cluster) from their
         * profile photo to be used for a marker icon
         *
         * @param person person to return an BitmapDescriptor for
         * @return the person's profile photo as a BitmapDescriptor
         */
        private BitmapDescriptor getItemIcon(Person person) {

            //Modified: draw image from bitmap instead of drawable
            mImageView.setImageBitmap(person.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

        @Override
        protected void onBeforeClusterRendered(@NonNull Cluster<Person> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            markerOptions.icon(getClusterIcon(cluster));
        }

        @Override
        protected void onClusterUpdated(@NonNull Cluster<Person> cluster, Marker marker) {
            // Same implementation as onBeforeClusterRendered() (to update cached markers)
            marker.setIcon(getClusterIcon(cluster));
        }

        /**
         * Get a descriptor for multiple people (a cluster) to be used for a marker icon. Note: this
         * method runs on the UI thread. Don't spend too much time in here (like in this example).
         *
         * @param cluster cluster to draw a BitmapDescriptor for
         * @return a BitmapDescriptor representing a cluster
         */
        private BitmapDescriptor getClusterIcon(Cluster<Person> cluster) {
            List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;

                // Modified: draw image from bitmap instead of drawable
                // Drawable drawable = getResources().getDrawable(p.profilePhoto);
                Drawable drawable = new BitmapDrawable(getResources(), p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

    @Override
    protected void startDemo(boolean isRestore) {
        if (!isRestore) {

            // Modified: zoom to HKUST when opening the map
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3360979, 114.2665707), 9.5f));
        }

        mClusterManager = new ClusterManager<>(this, getMap());
        mClusterManager.setRenderer(new PersonRenderer());
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        try {
            addItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mClusterManager.cluster();
    }

    private void addItems() throws IOException {

        // Modified: Generate Bitmaps for file A to F in DCIM/ELEC
        File dcimElec = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + File.separator + "ELEC" + File.separator);
        // FilenameFilter reference: https://stackoverflow.com/questions/19932962
        String[] photoList = dcimElec.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            }
        });
        Log.d("DEBUG_TAG", "Reading files: " + photoList[0]);

        if (photoList != null) {
            Bitmap[] bitmaps = new Bitmap[photoList.length];
            for (int i = 0; i < photoList.length; i++) {
                String fileName = dcimElec.getPath() + File.separator + photoList[i];
                Log.d("DEBUG_TAG", "Decoding files at: " + fileName + "(" + i+ " of " +photoList.length+ ")");
                bitmaps[i] = BitmapFactory.decodeFile(fileName);
                ExifInterface exif = new ExifInterface(fileName);
                mClusterManager.addItem(new Person(position(exif), Integer.toString(i), bitmaps[i]));
            }
        }
    }

    // Modified:
    private LatLng position(ExifInterface exif) {
        float[] latLong = new float[2];
        exif.getLatLong(latLong);
        return new LatLng((double) latLong[0],(double) latLong[1]);
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }
}


