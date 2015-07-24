package com.example.octane.flickrphotogallery;

import android.support.v4.app.Fragment;

/**
 * Created by ITS on 24.07.2015.
 */
public class PhotoPageActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PhotoPageFragment();
    }
}
