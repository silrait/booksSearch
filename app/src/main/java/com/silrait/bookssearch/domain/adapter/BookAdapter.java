package com.silrait.bookssearch.domain.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.silrait.bookssearch.R;
import com.silrait.bookssearch.domain.Book;

import java.io.InputStream;
import java.util.List;

class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
    private static final String LOG_TAG = DownloadImageFromInternet.class.getName();
    ImageView imageView;

    public DownloadImageFromInternet(ImageView imageView) {
        this.imageView = imageView;
    }

    protected Bitmap doInBackground(String... urls) {
        Log.d(LOG_TAG, "DownloadImageFromInternet.doInBackground");
        String imageURL = "https://" + urls[0].split("://")[1];
        Bitmap bimage = null;
        try {
            InputStream in = new java.net.URL(imageURL).openStream();
            bimage = BitmapFactory.decodeStream(in);

        } catch (Exception e) {
            Log.e("Error Message", e.getMessage());
            e.printStackTrace();
        }
        return bimage;
    }

    protected void onPostExecute(Bitmap result) {
        imageView.setImageBitmap(result);
    }
}

public class BookAdapter extends ArrayAdapter<Book> {
    public static final String LOG_TAG = BookAdapter.class.getName();

    public BookAdapter(Context context, List<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(LOG_TAG, "BookAdapter.getView");

        View listItemView = convertView;

        listItemView = LayoutInflater.from(getContext())
                .inflate(R.layout.book_item, parent, false);

        Book currentBook = getItem(position);

        TextView nameView = (TextView) listItemView.findViewById(R.id.title);
        nameView.setText(currentBook.getTitle());

        TextView authorView = (TextView) listItemView.findViewById(R.id.author);
        authorView.setText(currentBook.getAuthor());

        ImageView imageView = (ImageView) listItemView.findViewById(R.id.thumbnailView);
        new DownloadImageFromInternet(imageView)
                .execute(currentBook.getThumbnail());

        RatingBar rating = (RatingBar) listItemView.findViewById(R.id.ratingBar);
        rating.setRating(currentBook.getAverageRating().floatValue());

        return listItemView;
    }
}
