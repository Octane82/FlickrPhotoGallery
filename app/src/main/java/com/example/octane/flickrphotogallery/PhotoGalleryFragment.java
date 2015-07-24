package com.example.octane.flickrphotogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.octane.flickrphotogallery.net.FlickrFetchr;
import com.example.octane.flickrphotogallery.net.ThumbnailDownloader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Octane on 22.07.2015.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    GridView mGridView;
    ArrayList<GalleryItem> mItems;
    ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Запуск асинхронного потока
        new FetchItemTask().execute();

        // Запускаем отдельный поток загрузки изображений
       // mThumbnailThread = new ThumbnailDownloader<ImageView>();
        mThumbnailThread = new ThumbnailDownloader(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        setupAdapter();
        // Прослушивание нажатия на изображение в GridView
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> gridView, View view, int pos,
                                    long id) {
                GalleryItem item = mItems.get(pos);
                Uri photoPageUri = Uri.parse(item.getPhotoPageUrl());
                // Отправляем неявный интент для открытия браузера
                //Intent i = new Intent(Intent.ACTION_VIEW, photoPageUri);
                Intent i = new Intent(getActivity(), PhotoPageActivity.class);
                i.setData(photoPageUri);
                startActivity(i);
            }
        });

        return v;
    }

    // Заполняем адаптер данными
    void setupAdapter() {
        if (getActivity() == null || mGridView == null) return;
        if (mItems != null) {
            /*mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
                    android.R.layout.simple_gallery_item, mItems));*/
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }

    // При закрытиии фрагмента уничтожаем поток загрузки изображений
    // !!! Необходимо обязательно завершать потоки
    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    // Очищаем загрузчик в PhotoGalleryFragment при уничтожении представления.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }


    // Внутренний асинхронный класс для запуска объекта FlickrFetch (чтение из Flickr)
    private class FetchItemTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>>{

        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            /*try {
                String result = new FlickrFetchr().getUrl("http://www.google.com");
                Log.i(TAG, "Fetched contents of URL: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch URL: ", ioe);
            }*/

            // Загрузить XML с последними фотками с Flickr
            return  new FlickrFetchr().fetchItems();
        }

        // Метод выполняется после завершения doInBackground()
        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    // Класс адаптера для одной фотки в GridView
    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{

        public GalleryItemAdapter(ArrayList<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.gallery_item, parent, false);
            }
            ImageView imageView = (ImageView)convertView
                    .findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.nmg);

            // Получаем правильный элемент GalleryItem по его position
            GalleryItem item = getItem(position);
            mThumbnailThread.queueThumbnail(imageView, item.getUrl());

            return convertView;
        }

    }


}
