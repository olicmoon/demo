package com.samsung.sdpdemo.filebrowser;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.sdpdemo.Constants;
import com.samsung.sdpdemo.R;
import com.sec.sdp.SdpFileSystem;
import com.sec.sdp.exception.SdpAccessDeniedException;
import com.sec.sdp.exception.SdpEngineNotExistsException;

public class FileChooser extends ActionBarActivity implements ListView.OnItemClickListener {

    private File mCurrentDir;
    private FileArrayAdapter mAdapter;
    private ListView mFileListView = null;
    private TextView mTitleView = null;
    private String mBaseDir = null;
    private List<Item> mDir = null;
    private String mEngineAlias = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_browser_list_activity);
        mFileListView = (ListView) findViewById(R.id.file_list);
        mTitleView = (TextView) findViewById(R.id.title_view);
        mBaseDir = getIntent().getStringExtra(Constants.INTENT_EXTRAS_BASE_DIR);
        mEngineAlias = getIntent().getStringExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS);
        if (mBaseDir != null) {
            mCurrentDir = new File(mBaseDir);
            fill(mCurrentDir);
        }
    }

    private void fill(File currentFile) {
        if (currentFile == null) return;
        File[] totalFiles = currentFile.listFiles();
        mTitleView.setText("Current Dir: " + currentFile.getAbsolutePath());

        Log.d("sarang", "filldor " + currentFile.getName());
        if (totalFiles != null && totalFiles.length > 0) {
            Log.d("sarang", ">>>>>>>>>>>>>>>>>>>>/sdcard/ is empty " + totalFiles);
            return;
        }
        mDir = new ArrayList<Item>();
        List<Item> fls = new ArrayList<Item>();
        try {
            for (File file : totalFiles) {
                Date lastModDate = new Date(file.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if (file.isDirectory()) {
                    int fileCount = file.listFiles().length;
                    String num_item = null;
                    if (fileCount == 0) {
                        num_item = fileCount + " item";
                    } else {
                        num_item = fileCount + " items";
                    }
                    mDir.add(new Item(file.getName(), num_item, date_modify, file.getAbsolutePath(), "directory_icon"));
                } else {
                    SdpFileSystem sdpFileSystem = new SdpFileSystem(this, mEngineAlias);
                    boolean isSensitiveFile = sdpFileSystem.isSensitive(file);
                    if (isSensitiveFile) {
                        fls.add(new Item(file.getName(), file.length() + " Byte", date_modify, file.getAbsolutePath(), "sensitive_file_icon"));
                    } else {
                        fls.add(new Item(file.getName(), file.length() + " Byte", date_modify, file.getAbsolutePath(), "file_icon"));
                    }
                }
            }
        } catch (Exception e) {
            Log.d("sarang", "XXXXXXXXXXX EX");
            e.printStackTrace();
        }
        Collections.sort(mDir);
        Collections.sort(fls);
        mDir.addAll(fls);
        if (!currentFile.getAbsolutePath().equalsIgnoreCase(mBaseDir))
            mDir.add(0, new Item("..", "Parent Directory", "", currentFile.getParent(), "directory_up"));
        mAdapter = new FileArrayAdapter(FileChooser.this, R.layout.file_browser_row, mDir);
        mFileListView.setAdapter(mAdapter);
        mFileListView.setOnItemClickListener(this);
        mFileListView.setOnCreateContextMenuListener(this);
        registerForContextMenu(mFileListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, 1, Menu.NONE, "Set Sensitive");
        menu.add(Menu.NONE, 2, Menu.NONE, "Move to Chamber");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int listPos = info.position;
        Item itm = mDir.get(listPos);
        File file = new File(itm.getPath());
        if (item.getItemId() == 1 && file.isFile()) {
            try {
                SdpFileSystem secureFileSystem = new SdpFileSystem(this, mEngineAlias);
                if(secureFileSystem.isSensitive(file)) {
                    Toast.makeText(this, "Set sensitive " + listPos + " " + itm.getPath() + " success.", Toast.LENGTH_SHORT).show();
                } else if (secureFileSystem.setSensitive(file)) {
                    Log.d("sarang", ">>>>>>>> updiating liust "+mCurrentDir.getAbsolutePath());
                    fill(mCurrentDir);
                    Toast.makeText(this, "Set sensitive " + listPos + " " + itm.getPath() + " success.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to set sensitive " + listPos + " " + itm.getPath(), Toast.LENGTH_SHORT).show();
                }
            } catch (SdpEngineNotExistsException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (item.getItemId() == 2 && file.isFile()) {
//            Toast.makeText(this, "copy to chamber " + listPos + " " + itm.getPath(), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Not implemented yet! " + listPos + " " + itm.getPath(), Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Item o = mAdapter.getItem(position);
        if (o.getImage().equalsIgnoreCase("directory_icon") || o.getImage().equalsIgnoreCase("directory_up")) {
            mCurrentDir = new File(o.getPath());
            fill(mCurrentDir);
        }
    }

}