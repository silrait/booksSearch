package com.silrait.bookssearch;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.silrait.bookssearch.domain.Book;
import com.silrait.bookssearch.domain.adapter.BookAdapter;

import java.util.ArrayList;
import java.util.List;

class  BookSearchLoader extends AsyncTaskLoader<List<Book>>{
    private static final String LOG_TAG = BookSearchLoader.class.getName();
    private String url;

    public BookSearchLoader(@NonNull Context context, String url) {
        super(context);
        this.url = url;
    }

    @Nullable
    @Override
    public List<Book> loadInBackground() {
        Log.d(LOG_TAG, "BookSearchLoader.loadInBackground");
        return QueryUtils.queryBooks(url);
    }

    @Override
    protected void onStartLoading() {
        Log.d(LOG_TAG, "BookSearchLoader.onStartLoading");
        forceLoad();
    }
}

public class BookSearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>>{
    public static final String LOG_TAG = BookSearchActivity.class.getName();
    private static final String BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    private List<Book> books = new ArrayList<>();
    private ListView booksListView;
    private TextView emptyListView;
    private ProgressBar progressBar;
    private BookAdapter bookAdapter;

    private Boolean hasInternetConnectivity(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();

        return !(netInfo == null || !netInfo.isConnected() ||
                (netInfo.getType() != ConnectivityManager.TYPE_WIFI && netInfo.getType() != ConnectivityManager.TYPE_MOBILE));
    }

    private void warnsUser(){
        emptyListView.setText(R.string.no_internet);
    }

    private void InitialMessage(){
        emptyListView.setText(R.string.search_for_a_book);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "BookSearchActivity.onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "BookSearchActivity.onDestroy");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booksearch_activity);

        emptyListView = (TextView) findViewById(R.id.emptyList);
        booksListView = (ListView) findViewById(R.id.list);
        progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
        booksListView.setEmptyView(emptyListView);

        bookAdapter = new BookAdapter(BookSearchActivity.this, BookSearchActivity.this.books);

        if ((hasInternetConnectivity())) {
            InitialMessage();
        } else {
            warnsUser();
        }


        Log.d(LOG_TAG, "BookSearchActivity.onCreate");
    }

    //*********** The following methods are responsible by loading the books in background *********
    private void queryBooks(String searchText){
        Bundle b = new Bundle();
        b.putString("query", searchText);
        getSupportLoaderManager().restartLoader(1, b, this);
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "BookSearchActivity.onCreateLoader with query: " + args.getString("query"));
        progressBar.setVisibility(View.VISIBLE);
        String searchString = BOOKS_REQUEST_URL + args.getString("query");
        return new BookSearchLoader(BookSearchActivity.this, searchString);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        Log.d(LOG_TAG, "BookSearchActivity.onLoadFinished called");

        BookSearchActivity.this.progressBar.setVisibility(View.GONE);

        if(books == null || books.size() == 0){
            emptyListView.setText(R.string.no_resource);
            return;
        }

        Log.d(LOG_TAG, "BookSearchActivity.onLoadFinished: Books returned: " + books.size());

        BookSearchActivity.this.bookAdapter.clear();
        BookSearchActivity.this.books = books;
        BookSearchActivity.this.bookAdapter.addAll(books);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        booksListView.setAdapter(bookAdapter);

        booksListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Book b = BookSearchActivity.this.books.get(position);
                        Log.d(LOG_TAG, "booksListView.onItemClick: Book id: " + b.getId());
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(b.getUrl()));
                        if (browserIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(browserIntent);
                        }
                    }
                }
        );
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        Log.d(LOG_TAG, "EarthquakeActivity.onLoaderReset called");
    }

    //********************* The following methods are responsible by the search action ************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_list, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(LOG_TAG, "EarthquakeActivity.onQueryTextSubmit");
                BookSearchActivity.this.queryBooks(query);

                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchItem.collapseActionView();
                BookSearchActivity.this.setTitle(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return true;
    }
}
