package com.andryr.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.andryr.musicplayer.Album;
import com.andryr.musicplayer.Artist;
import com.andryr.musicplayer.Genre;
import com.andryr.musicplayer.MusicLibraryHelper;
import com.andryr.musicplayer.Song;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by andry on 21/08/15.
 */
public class SongLoader extends AsyncTaskLoader<List<Song>>
{
    private static final int ALL_SONGS = 1;
    private static final int ALBUM_SONGS = 2;
    private static final int ARTIST_SONGS = 3;
    private static final int ARTIST_ALBUM_SONGS = 4;
    private static final int GENRE_SONGS = 5;

    private int mSongListType = ALL_SONGS;
    private long mArtistId;
    private long mAlbumId;
    private long mGenreId;

    private List<Song> mSongList = null;

    private static final String[] sProjection = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK};

    public SongLoader(Context context, int mSongListType, long mArtistId, long mAlbumId, long mGenreId) {
        super(context);
        this.mSongListType = mSongListType;
        this.mArtistId = mArtistId;
        this.mAlbumId = mAlbumId;
        this.mGenreId = mGenreId;
    }

    @Override
    public List<Song> loadInBackground() {
        mSongList = new ArrayList<>();

        Cursor cursor = getSongCursor();
        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            if (idCol == -1) {
                idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            }
            int titleCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumIdCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int trackCol  = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TRACK);

            do {
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);

                String artist = cursor.getString(artistCol);

                String album = cursor.getString(albumCol);

                long albumId = cursor.getLong(albumIdCol);

                int track = cursor.getInt(trackCol);


                mSongList.add(new Song(id, title, artist, album, albumId, track));
            } while (cursor.moveToNext());

            Collections.sort(mSongList, new Comparator<Song>() {

                @Override
                public int compare(Song lhs, Song rhs) {
                    Collator c = Collator.getInstance(Locale.getDefault());
                    c.setStrength(Collator.PRIMARY);
                    return c.compare(lhs.getTitle(), rhs.getTitle());
                }
            });

        }

        if(cursor != null)
        {
            cursor.close();
        }
        Log.e("test", "  d " + mSongList.size());

        return mSongList;
    }

    @Override
    protected void onStartLoading() {
        if (mSongList != null) {
            deliverResult(mSongList);
        }
        if (takeContentChanged() || mSongList == null) {
            forceLoad();
        }
    }

    private Cursor getSongCursor()
    {
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = null;

        switch (mSongListType) {
            case ALL_SONGS:
                cursor = getContext().getContentResolver().query(musicUri, sProjection,
                        null, null, null);
                break;
            case ARTIST_SONGS:
                cursor = getContext().getContentResolver().query(musicUri, sProjection,
                        MediaStore.Audio.Media.ARTIST_ID + " = " + mArtistId,
                        null, null);
                break;
            case ALBUM_SONGS:
                cursor = getContext().getContentResolver().query(musicUri, sProjection,
                        MediaStore.Audio.Media.ALBUM_ID + " = " + mAlbumId,
                        null, null);
                break;
            case ARTIST_ALBUM_SONGS:
                // TODO
                break;
            case GENRE_SONGS:
                musicUri = MediaStore.Audio.Genres.Members.getContentUri(
                        "external", mGenreId);
                cursor = getContext().getContentResolver().query( musicUri, sProjection,
                        null, null, null);
                break;

        }
        return cursor;
    }
}