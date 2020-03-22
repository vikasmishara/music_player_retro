/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package themusicplayer.audioplayer.mp3player.retromusic.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import themusicplayer.audioplayer.mp3player.retromusic.R;
import themusicplayer.audioplayer.mp3player.retromusic.helper.M3UWriter;
import themusicplayer.audioplayer.mp3player.retromusic.model.Playlist;
import themusicplayer.audioplayer.mp3player.retromusic.model.PlaylistSong;
import themusicplayer.audioplayer.mp3player.retromusic.model.Song;

import static android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

public class PlaylistsUtil {

    public static void addToPlaylist(@NonNull Context context,
                                     @NonNull List<Song> songs,
                                     int playlistId,
                                     boolean showToastOnFinish) {

        ArrayList<Song> noSongs = new ArrayList<Song>();
        for (Song song : songs) {
            if (!doPlaylistContains(context, playlistId, song.getId())) {
                noSongs.add(song);
            }
        }

        final int size = noSongs.size();
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{"max(" + MediaStore.Audio.Playlists.Members.PLAY_ORDER + ")",};
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);

        int base = 0;
        try (Cursor cursor = resolver.query(uri, projection, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } catch (SecurityException ignored) {
        }

        int numInserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            numInserted += resolver.bulkInsert(uri, makeInsertItems(noSongs, offSet, 1000, base));
        }

        if (showToastOnFinish) {
            Toast.makeText(context, context.getResources().getString(
                    R.string.inserted_x_songs_into_playlist_x, numInserted, getNameForPlaylist(context, playlistId)),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static int createPlaylist(@NonNull final Context context, @Nullable final String name) {
        int id = -1;
        if (name != null && name.length() > 0) {
            try {
                Cursor cursor = context.getContentResolver()
                        .query(EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Playlists._ID},
                                MediaStore.Audio.PlaylistsColumns.NAME + "=?", new String[]{name}, null);
                if (cursor == null || cursor.getCount() < 1) {
                    final ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                    final Uri uri = context.getContentResolver().insert(EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        Toast.makeText(context, context.getResources().getString(R.string.created_playlist_x, name),
                                Toast.LENGTH_SHORT).show();
                        id = Integer.parseInt(uri.getLastPathSegment());
                    }
                } else {
                    // Playlist exists
                    if (cursor.moveToFirst()) {
                        id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SecurityException ignored) {
            }
        }
        if (id == -1) {
            Toast.makeText(context, context.getResources().getString(
                    R.string.could_not_create_playlist), Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public static void deletePlaylists(@NonNull final Context context, @NonNull final ArrayList<Playlist> playlists) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Playlists._ID + " IN (");
        for (int i = 0; i < playlists.size(); i++) {
            selection.append(playlists.get(i).id);
            if (i < playlists.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        try {
            context.getContentResolver().delete(EXTERNAL_CONTENT_URI, selection.toString(), null);
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } catch (SecurityException ignored) {
        }
    }

    static boolean doPlaylistContains(@NonNull final Context context, final long playlistId,
                                      final int songId) {
        if (playlistId != -1) {
            try {
                Cursor c = context.getContentResolver().query(
                        MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                        new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID},
                        MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", new String[]{String.valueOf(songId)},
                        null);
                int count = 0;
                if (c != null) {
                    count = c.getCount();
                    c.close();
                }
                return count > 0;
            } catch (SecurityException ignored) {
            }
        }
        return false;
    }

    public static boolean doesPlaylistExist(@NonNull final Context context, final int playlistId) {
        return playlistId != -1 && doesPlaylistExist(context,
                MediaStore.Audio.Playlists._ID + "=?",
                new String[]{String.valueOf(playlistId)});
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        try {
            Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.PlaylistsColumns.NAME},
                    BaseColumns._ID + "=?",
                    new String[]{String.valueOf(id)},
                    null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (SecurityException ignored) {
        }
        return "";
    }

    @NonNull
    public static ContentValues[] makeInsertItems(@NonNull final List<Song> songs, final int offset, int len,
                                                  final int base) {
        if (offset + len > songs.size()) {
            len = songs.size() - offset;
        }

        ContentValues[] contentValues = new ContentValues[len];

        for (int i = 0; i < len; i++) {
            contentValues[i] = new ContentValues();
            contentValues[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            contentValues[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songs.get(offset + i).getId());
        }
        return contentValues;
    }

    public static boolean moveItem(@NonNull final Context context, int playlistId, int from, int to) {
        return MediaStore.Audio.Playlists.Members.moveItem(context.getContentResolver(),
                playlistId, from, to);
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final Song song, int playlistId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        String selection = MediaStore.Audio.Playlists.Members.AUDIO_ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(song.getId())};

        try {
            context.getContentResolver().delete(uri, selection, selectionArgs);
        } catch (SecurityException ignored) {
        }
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final List<PlaylistSong> songs) {
        final int playlistId = songs.get(0).getPlaylistId();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        String selectionArgs[] = new String[songs.size()];
        for (int i = 0; i < selectionArgs.length; i++) {
            selectionArgs[i] = String.valueOf(songs.get(i).getIdInPlayList());
        }
        String selection = MediaStore.Audio.Playlists.Members._ID + " in (";
        //noinspection unused
        for (String selectionArg : selectionArgs) {
            selection += "?, ";
        }
        selection = selection.substring(0, selection.length() - 2) + ")";

        try {
            context.getContentResolver().delete(uri, selection, selectionArgs);
        } catch (SecurityException ignored) {
        }
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.PlaylistsColumns.NAME, newName);
        try {
            context.getContentResolver().update(EXTERNAL_CONTENT_URI,
                    contentValues,
                    MediaStore.Audio.Playlists._ID + "=?",
                    new String[]{String.valueOf(id)});
        } catch (SecurityException ignored) {
        }
    }

    @Nullable
    public static File savePlaylist(@NonNull Context context,
                                    @NonNull Playlist playlist) throws IOException {
        return M3UWriter.write(context, new File(Environment.getExternalStorageDirectory(), "Playlists"), playlist);
    }

    static void addToPlaylist(@NonNull Context context,
                              @NonNull Song song,
                              int playlistId,
                              boolean showToastOnFinish) {
        List<Song> helperList = new ArrayList<>();
        helperList.add(song);
        addToPlaylist(context, helperList, playlistId, showToastOnFinish);
    }

    private static boolean doesPlaylistExist(@NonNull Context context, @NonNull final String selection,
                                             @NonNull final String[] values) {
        Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI,
                new String[]{}, selection, values, null);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() != 0;
            cursor.close();
        }
        return exists;
    }
}