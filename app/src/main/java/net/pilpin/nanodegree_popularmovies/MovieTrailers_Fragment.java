package net.pilpin.nanodegree_popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.pilpin.nanodegree_popularmovies.data.MovieContract;

public class MovieTrailers_Fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int TRAILERS_LOADER = 10;

    private static final String[] TRAILERS_COLUMNS = {
            MovieContract.TrailerEntry.NAME,
            MovieContract.TrailerEntry.KEY,
            MovieContract.TrailerEntry.SITE,
            MovieContract.TrailerEntry.SIZE};

    static final int COL_TRAILER_NAME = 0;
    static final int COL_TRAILER_KEY = 1;
    static final int COL_TRAILER_SITE = 2;
    static final int COL_TRAILER_SIZE = 3;

    private long mMovie_id;
    private long mMovieApiId;

    private ShareActionProvider mShareActionProvider;
    private MenuItem mShareItem;
    private Intent mShareIntent;
    private boolean mOptionsMenuItemVisible = false;

    private LinearLayout content;
    private TextView title;

    public static MovieTrailers_Fragment newInstance(Uri data, long movieApiId){
        Bundle args = new Bundle();
        args.putLong(MovieDetails_Activity.MOVIE_ID, Long.decode(data.getLastPathSegment()));
        args.putLong(MovieDetails_Activity.MOVIE_API_ID, movieApiId);

        MovieTrailers_Fragment fragment = new MovieTrailers_Fragment();
        fragment.setArguments(args);
        fragment.setHasOptionsMenu(true);

        return fragment;
    }

    public MovieTrailers_Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            mMovie_id = args.getLong(MovieDetails_Activity.MOVIE_ID);
            mMovieApiId = args.getLong(MovieDetails_Activity.MOVIE_API_ID);
        }

        content = (LinearLayout) inflater.inflate(R.layout.fragment_movie_trailers, container, false);
        title = (TextView) container.findViewById(R.id.trailers_title);

        getLoaderManager().initLoader(TRAILERS_LOADER, null, this);

        return content;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trailers, menu);
        mShareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareItem);
        if(mShareIntent != null){
            mShareItem.setVisible(mOptionsMenuItemVisible);
            mShareActionProvider.setShareIntent(mShareIntent);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        FetchTrailersTask fetchTrailersTask = new FetchTrailersTask(getActivity(), mMovie_id, mMovieApiId);
        fetchTrailersTask.execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                MovieContract.TrailerEntry.buildMovieUri(mMovie_id),
                TRAILERS_COLUMNS,
                MovieContract.TrailerEntry.SITE + " = ?",
                new String[]{"YouTube"},
                MovieContract.TrailerEntry.ORDER_BY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() == 0){
            content.setVisibility(View.GONE);
            mOptionsMenuItemVisible = false;
        }else {
            content.setVisibility(View.VISIBLE);
            content.removeViews(1, content.getChildCount() - 1);
            while(data.moveToNext()){
                final String trailerUrl = "http://www.youtube.com/watch?v=" + data.getString(COL_TRAILER_KEY);

                TextView trailer = new TextView(getActivity());
                trailer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                int padding = (int) getResources().getDimension(R.dimen.padding);
                trailer.setPadding(padding, padding, padding, padding);
                trailer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.item_poster_selector));

                trailer.setText(data.getString(COL_TRAILER_NAME));
                trailer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                        startActivity(intent);
                    }
                });

                if(data.isFirst()){
                    mShareIntent = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, trailerUrl);
                    mOptionsMenuItemVisible = true;
                }

                content.addView(trailer);
            }
        }

        if(mShareActionProvider != null){
            mShareItem.setVisible(mOptionsMenuItemVisible);
            mShareActionProvider.setShareIntent(mShareIntent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
