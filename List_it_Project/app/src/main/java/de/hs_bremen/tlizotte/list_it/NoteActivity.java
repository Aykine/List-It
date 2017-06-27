package de.hs_bremen.tlizotte.list_it;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import de.hs_bremen.tlizotte.list_it.Adapters.FinishedItemsArrayAdapter;
import de.hs_bremen.tlizotte.list_it.Adapters.ItemsArrayAdapter;
import de.hs_bremen.tlizotte.list_it.contentprovider.NoteContentProvider;
import de.hs_bremen.tlizotte.list_it.database.NoteTable;

/**
 * Created by T_Liz on 12.06.2017.
 */

public class NoteActivity extends AppCompatActivity {

    private ShareDialog shareDialog;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final static String TAG = NoteActivity.class.getSimpleName();
    public final static int SLASHED = 1;
    public final static int UNSLASHED = 0;

    //Two seperate Lists. One unfinished-, one finished items
    ItemsArrayAdapter mItemsArrayAdapter;
    FinishedItemsArrayAdapter mFinishedItemsArrayAdapter;

    EditText mNewItemText;
    DynamicListView mItemsListView;
    ListView mFinishedItemsListView;
    ArrayList<String> mItems;
    ArrayList<String> mFinishedItems;
    //    FloatingActionButton fab;
    private Uri noteUri;
    public ArrayList<String> slashes;
    EditText mNoteTitle;
    ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_note);
        verifyStoragePermissions(this);
        slashes = new ArrayList<>();
        shareDialog = new ShareDialog(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.sendToFacebook);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                takeScreenshot();
            }
        });

        mFinishedItems = new ArrayList<>();
        mFinishedItemsArrayAdapter = new FinishedItemsArrayAdapter(this, mFinishedItems, true);
        mFinishedItemsListView = (ListView) findViewById(R.id.finishedItems);
        mFinishedItemsListView.setAdapter(mFinishedItemsArrayAdapter);
        mFinishedItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView item = (TextView) view.findViewById(R.id.itemText);
                String text = item.getText().toString();
                mFinishedItems.remove(text);
                mItems.add(text);
                mFinishedItemsArrayAdapter.notifyDataSetChanged();
                mItemsArrayAdapter.notifyDataSetChanged();
            }
        });

        mItems = new ArrayList<String>();
        mItemsArrayAdapter = new ItemsArrayAdapter(this, mItems, false);
        mItemsListView = (DynamicListView) findViewById(R.id.itemsListView);
        mItemsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mItemsListView.setAdapter(mItemsArrayAdapter);
        mItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView item = (TextView) view.findViewById(R.id.itemText);
                String text = item.getText().toString();
                mItems.remove(text);
                mFinishedItems.add(text);
                mFinishedItemsArrayAdapter.notifyDataSetChanged();
                mItemsArrayAdapter.notifyDataSetChanged();
            }
        });

        Bundle extras = getIntent().getExtras();

        // check from the saved Instance
        noteUri = (bundle == null) ? null : (Uri) bundle
                .getParcelable(NoteContentProvider.CONTENT_ITEM_TYPE);

        mActionBar = getSupportActionBar();
        View view = getLayoutInflater().inflate(R.layout.note_actionbar, null);

        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setCustomView(view);
        mActionBar.setDisplayShowCustomEnabled(true);
        mNoteTitle = (EditText) mActionBar.getCustomView().findViewById(R.id.noteName);

        // Or passed from the other activity
        if (extras != null) {
            if(extras.getParcelable(NoteContentProvider.CONTENT_ITEM_TYPE) != null) {
                noteUri = extras
                        .getParcelable(NoteContentProvider.CONTENT_ITEM_TYPE);
            }
            fillData(noteUri);

        }

        mItemsListView.setCheeseList(mItems);
    }
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void takeScreenshot(){
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try{
            //File mPathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            //mPathFile.mkdirs();
            Log.e("Start", "Start");
            String mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/" + now + ".jpg";
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Log.e("set", "drawingcache");
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            Log.e("new", "File");
            File imageFile = new File(mPath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            Log.e("output", "stream");
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            Log.e("share", "photo");
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();

            Log.e("share", "dialog");
            Log.e("shareDialog", content.toString());
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);

        }
        catch(Throwable e)
        {
            Toast.makeText(getApplicationContext(), "This is not working on emulator, please use real hardware", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void fillData(Uri uri) {

        String[] projection = {NoteTable.COLUMN_ITEMS, NoteTable.COLUMN_SLASHED, NoteTable.COLUMN_NOTE_TITLE};
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(uri, projection, null, null,
                    null);
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException caught: ", e);
        }
        if (cursor != null) {
            cursor.moveToFirst();

            String sItems = cursor.getString(cursor
                    .getColumnIndexOrThrow(NoteTable.COLUMN_ITEMS));

            String sSlashes = cursor.getString(cursor.getColumnIndexOrThrow(NoteTable.COLUMN_SLASHED));

            String title = cursor.getString(cursor.getColumnIndexOrThrow(NoteTable.COLUMN_NOTE_TITLE));

            try {
                JSONArray jsonArray = new JSONArray(sItems);

                mNoteTitle.setText(title);

                JSONArray slashesJsonArray = new JSONArray(sSlashes);
                for (int i = 0; i < slashesJsonArray.length(); i++) {
                    slashes.add("" + slashesJsonArray.get(i));
                    if(slashesJsonArray.get(i).equals(NoteActivity.UNSLASHED)){
                        mItems.add((String) jsonArray.get(i));
                    }
                    else{
                        mFinishedItems.add((String) jsonArray.get(i));
                    }
                }
                mFinishedItemsArrayAdapter.notifyDataSetChanged();
                mItemsArrayAdapter.notifyDataSetChanged();
            } catch (JSONException ignored) {
            }

            // always close the cursor
            cursor.close();
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putParcelable(NoteContentProvider.CONTENT_ITEM_TYPE, noteUri);
    }

    protected void onPause() {
        super.onPause();
        saveState();
    }

    private void saveState()
    {
        mItems.addAll(mFinishedItems);
        String note = new JSONArray(mItems).toString();
        ArrayList<Integer> slashes = new ArrayList<>();

        for (int i = 0; i < mItemsListView.getChildCount(); i++) {
            slashes.add(NoteActivity.UNSLASHED);
        }
        for (int i = 0; i < mFinishedItemsListView.getChildCount(); i++) {
            slashes.add(NoteActivity.SLASHED);
        }

        String sSlashes = new JSONArray(slashes).toString();

        if (mItems.isEmpty()) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(NoteTable.COLUMN_ITEMS, note);
        values.put(NoteTable.COLUMN_SLASHED, sSlashes);

        String noteTitle = mNoteTitle.getText().toString();
        if(noteTitle.isEmpty()){
            noteTitle = "Untitled";
        }

        values.put(NoteTable.COLUMN_NOTE_TITLE, noteTitle);
        if (noteUri == null) {
            noteUri = getContentResolver().insert(NoteContentProvider.CONTENT_URI, values);
            String firstPart = NoteContentProvider.CONTENT_URI.toString();
            Long id = ContentUris.parseId(noteUri);
            String correctURIs = firstPart + "/" + id;
            Uri correctedUri = Uri.parse(correctURIs);
            try{
                getContentResolver().update(noteUri, values, null, null);
            }
            catch (Exception e){
                noteUri = correctedUri;
            }

        } else {
            getContentResolver().update(noteUri, values, null, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds mItems to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void addItem(View v) {
        Toast.makeText(getApplicationContext(),"item adding", Toast.LENGTH_LONG);
        if (mItems.size() < 100) {
            AlertDialog alertToShow = getDialog().create();
            alertToShow.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            alertToShow.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Max Items")
                    .setMessage("You have reached the maximum " +
                            "number of items (100) one note can hold.")
                    .setPositiveButton("OK", null);
            builder.show();
        }

    }

    public AlertDialog.Builder getDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        LinearLayout newNoteBaseLayout = (LinearLayout) li.inflate(R.layout.new_item_dialog, null);

        mNewItemText = (EditText) newNoteBaseLayout.getChildAt(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeKeyboard();
                String text = mNewItemText.getText().toString();
                if (!mItems.contains(text) && !text.isEmpty()) {
                    mItems.add(text);
                    mItemsArrayAdapter.update();
                    mItemsArrayAdapter.notifyDataSetChanged();
                }
                else if(mItems.contains(text)){
                    Toast.makeText(NoteActivity.this, text + " is already added.", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeKeyboard();
            }
        })
                .setTitle("New Item");

        builder.setView(newNoteBaseLayout);
        return builder;
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNewItemText.getWindowToken(), 0);
    }

    public void deleteItem(int position) {
        mItems.remove(position);
        mItemsArrayAdapter.update();
        mItemsArrayAdapter.notifyDataSetChanged();
    }

    public void editItem(final int position) {
        AlertDialog.Builder builder = getDialog();
        mNewItemText.setText(mItems.get(position));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeKeyboard();
                mItems.set(position, mNewItemText.getText().toString());
                mItemsArrayAdapter.update();
                mItemsArrayAdapter.notifyDataSetChanged();
            }
        });
        AlertDialog alertToShow = builder.create();
        alertToShow.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertToShow.show();
        mNewItemText.setSelection(mNewItemText.getText().length());
    }

    public void uncheckAll(View view) {
        mItems.addAll(mFinishedItems);
        mFinishedItems.clear();
        mItemsArrayAdapter.notifyDataSetChanged();
        mFinishedItemsArrayAdapter.notifyDataSetChanged();
    }

    public void deleteFinishedItem(int position) {
        mFinishedItems.remove(position);
        mFinishedItemsArrayAdapter.notifyDataSetChanged();
    }

    public void editFinishedItem(final int position) {
        AlertDialog.Builder builder = getDialog();
        mNewItemText.setText(mFinishedItems.get(position));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFinishedItems.set(position, mNewItemText.getText().toString());
                mFinishedItemsArrayAdapter.notifyDataSetChanged();
            }
        });
        builder.show();
    }

}
