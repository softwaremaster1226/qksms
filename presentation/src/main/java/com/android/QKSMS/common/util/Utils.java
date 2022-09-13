package com.android.QKSMS.common.util;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Utils {
    public static String FILE_NAME = "FILE_NAME";
    public static String IS_RINGTONE = "IS_RINGTONE";
    public static String IS_ALARM = "IS_ALARM";
    public static String IS_NOTIFICATION = "IS_NOTIFICATION";
    public static String IS_MUSIC = "IS_MUSIC";
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS=45;
    public static final int REQUEST_ID_RECORD_AUDIO_PERMISSION=46;
    public static final int REQUEST_ID_READ_CONTACTS_PERMISSION=47;
    public static HashMap<String, String> selectedContacts = new HashMap<>();

    public static int getDimensionInPixel(Context context, int dp) {
        return (int) TypedValue.applyDimension(0, dp, context.getResources().getDisplayMetrics());
    }

    private static final String[] INTERNAL_COLUMNS = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.ALBUM_ID,
            "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\""
    };
    private static final String[] EXTERNAL_COLUMNS = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.ALBUM_ID,
            "\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\""
    };

    public static ArrayList<SongsModel> getSongList(Context context, boolean internal, String searchString) {
        String[] selectionArgs = null;
        String selection = null;
        if (searchString != null && searchString.length() > 0) {
            selection = "title LIKE ?";
            selectionArgs = new String[]{"%" + searchString + "%"};
        }

        ArrayList<SongsModel> songsModels = new ArrayList<>();
        Uri CONTENT_URI;
        String[] COLUMNS;

        if (internal) {
            CONTENT_URI = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
            COLUMNS = INTERNAL_COLUMNS;
        } else {
            CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            COLUMNS = EXTERNAL_COLUMNS;
        }

        Cursor cursor = context.getContentResolver().query(
                CONTENT_URI,
                COLUMNS,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String fileType = "";
                try {
                    if (cursor.getString(6).equalsIgnoreCase("1")) {
                        fileType = IS_RINGTONE;
                    } else if (cursor.getString(7).equalsIgnoreCase("1")) {
                        fileType = IS_ALARM;
                    } else if (cursor.getString(8).equalsIgnoreCase("1")) {
                        fileType = IS_NOTIFICATION;
                    } else {
                        fileType = IS_MUSIC;
                    }
                } catch (Exception e) {
                    //lets assume its ringtone.
                    fileType = IS_RINGTONE;
                }

                SongsModel song = new SongsModel(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(10),
                        fileType);
                songsModels.add(song);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return songsModels;
    }

    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }


//    public static final String makeShortTimeString(final Context context, long secs) {
//        long hours, mins;
//
//        hours = secs / 3600;
//        secs %= 3600;
//        mins = secs / 60;
//        secs %= 60;
//
//        final String durationFormat = context.getResources().getString(
//                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
//        return String.format(durationFormat, hours, mins, secs);
//    }

    public static MediaPlayer playMp3(byte[] mp3SoundByteArray) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            // create temp file that will hold byte array
            File tempMp3 = new File(Environment.getExternalStorageDirectory().toString() + "/Music/temp.mp3");
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            mediaPlayer.reset();
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return mediaPlayer;
    }

    public static byte[] LoadRaw(Context context, Uri uri){
        InputStream inputStream = null;
        byte[] ret = new byte[0];

        //Open inputStream from the specified URI
        int Error;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);

            //Try read from the InputStream
            if(inputStream!=null)
                ret = inputStreamToByteArray(inputStream);

        }
        catch (FileNotFoundException e1) {
            Error = 1;
        }
        catch (IOException e) {
            Error = 2;
        }
        finally{
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    //Problem on closing stream.
                    //The return state does not change.
                    Error = 1;
                }
            }
        }

        //Return
        return ret;
    }
    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    public static ArrayList<CalendarEventData> readCalendarEvent(Context context) {
        ArrayList<CalendarEventData> result = new ArrayList<>();
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[] { "calendar_id", "title", "description",
                                "dtstart", "dtend", "eventLocation" }, null,
                        null, null);
        cursor.moveToFirst();
        String date = "";
        ArrayList<CalendarEventDetail> detail = new ArrayList<>();

        // fetching calendars name
        String CNames[] = new String[cursor.getCount()];

        // fetching calendars events
        for (int i = 0; i < CNames.length; i++) {
            String tmp = getDate(Long.parseLong(cursor.getString(3))).split(" ")[0];
            if(!tmp.equals(date)) {
                if(!date.isEmpty()) {
                    CalendarEventData ced = new CalendarEventData(date, detail);
                    result.add(ced);
                    detail = new ArrayList<>();
                }
                date = tmp;
            }
            detail.add(new CalendarEventDetail(cursor.getString(1), cursor.getString(2), getDate(Long.parseLong(cursor.getString(3))), getDate(Long.parseLong(cursor.getString(4))), cursor.getString(5)));
            CNames[i] = cursor.getString(1);
            cursor.moveToNext();
        }
        CalendarEventData ced = new CalendarEventData(date, detail);
        result.add(ced);
        return result;
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static void adjustAlertDialog(AlertDialog dialog, Drawable drawable) {
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setBackgroundDrawable(drawable);
    }

    public static void slideView(View view,
                                 int currentHeight,
                                 int newHeight) {

        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(500);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */

        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.requestLayout();
        });

        /*  We use an animationSet to play the animation  */

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    public static void scaleTextAnim(View view, float x, float y, float pivotX, float pivotY) {
        view.setPivotX(pivotX);
        view.setPivotY(pivotY);
        view.animate().scaleX(x).scaleY(y).setDuration(100).start();
    }

    public static void scaleHeight(View v, int start, int end) {
        ValueAnimator va;
        va = ValueAnimator.ofInt(start, end);
        va.setDuration(100);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) v.getLayoutParams();
        va.addUpdateListener(animation -> {
            params.height = (Integer) animation.getAnimatedValue();
            v.setLayoutParams(params);
        });
        va.start();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static class CalendarEventData {
        private String date;
        private ArrayList<CalendarEventDetail> detail;
        CalendarEventData(String d, ArrayList<CalendarEventDetail> ced) {
            date = d;
            detail = ced;
        }
        public String getDate() {
            return date;
        }
        public ArrayList<CalendarEventDetail> getDetail() {
            return detail;
        }

        @Override
        public String toString() {
            return "CalendarEventData{" +
                    "date='" + date + '\'' +
                    ", detail=" + detail.toString() +
                    '}';
        }
    }

    public static class CalendarEventDetail implements Serializable {
        private String startDate;
        private String endDate;
        private String description;
        private String nameOfEvents;
        private String location;
        CalendarEventDetail(String noe, String d, String sd, String ed, String l) {
            startDate = sd;
            endDate = ed;
            description = d;
            nameOfEvents = noe;
            location = l;
        }
        public String getStartDate() {
            return startDate;
        }
        public String getEndDate() {
            return endDate;
        }
        public String getDescription() {
            return description;
        }
        public String getNameOfEvents() {
            return nameOfEvents;
        }
        public String getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return "From='" + startDate + '\'' +
                    ", To='" + endDate + '\'' +
                    ", description='" + description + '\'' +
                    ", Events='" + nameOfEvents + '\'' +
                    ", Location='" + location + '\'' +
                    '\n';
        }
    }
}
