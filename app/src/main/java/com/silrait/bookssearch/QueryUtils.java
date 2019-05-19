package com.silrait.bookssearch;

import android.util.Log;

import com.silrait.bookssearch.domain.Book;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {
    public static final String LOG_TAG = QueryUtils.class.getName();

    private QueryUtils() {
    }


    public static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    public static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static List<Book> extractBooks(String jsonResponse) {
        ArrayList<Book> books = new ArrayList<>();

        try {

            JSONObject root = new JSONObject(jsonResponse);

            JSONArray items = root.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject JSONVolumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");

                books.add(new Book(
                        items.getJSONObject(i).getString("id"),
                        JSONVolumeInfo.getString("title"),
                        JSONVolumeInfo.getJSONArray("authors").getString(0),
                        JSONVolumeInfo.getJSONObject("imageLinks").getString("thumbnail"),
                        JSONVolumeInfo.getString("infoLink"),
                        (JSONVolumeInfo.has("averageRating"))?
                                Double.parseDouble(JSONVolumeInfo.getString("averageRating")) : 0.0
                ));
            }


        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        }

        return books;
    }

    public static List<Book> queryBooks(String APIurl){
        List<Book> list = null;

        URL url = QueryUtils.createUrl(APIurl);

        try {
            list = QueryUtils.extractBooks(QueryUtils.makeHttpRequest(url));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making HTTP request for the book JSON results.", e);
        }

        return list;
    }

}