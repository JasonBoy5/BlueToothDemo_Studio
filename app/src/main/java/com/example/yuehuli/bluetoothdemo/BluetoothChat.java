package com.example.yuehuli.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothChat extends AppCompatActivity {
    Toolbar mToolbar;
    TextView mTextView;
    private PopupMenu mOverflowMenu;
    private ImageView toolbarMoreIcon;
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private Button mUpSendButoon;
    private Button mDownSendButton;
    private Button mstopButton;
    private Button mlinkNumButton;
    private Button mbottomButton;
    private Button mtopButton;
    private Button m2floorButton;
    private Button mchkposButton;
    private Button mresetButton;
    private Button mdebugButton;
    private Button msetButton;

    private String mConnectedDeviceName = null;

    private ArrayAdapter<String> mConversationArrayAdapter;

    private StringBuffer mOutStringBuffer;

    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothChatService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_chat);
        mTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarMoreIcon = (ImageView) findViewById(R.id.toolbar_more);
        setSupportActionBar(mToolbar);
        mTitle.setText(R.string.toolbar_title);
        mOverflowMenu = buildOptionsMenu(toolbarMoreIcon);
        toolbarMoreIcon.setOnTouchListener(mOverflowMenu.getDragToOpenListener());
        toolbarMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOverflowMenu.show();
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();//Toast是Android中用来显示显示信息的一种机制
            finish();
            return;
        }
    }

    private OptionsPopupMenu buildOptionsMenu(View invoker) {
        final OptionsPopupMenu popupMenu = new OptionsPopupMenu(this, invoker) {
            @Override
            public void show() {
                super.show();
            }
        };
        popupMenu.inflate(R.menu.option_menu);
        popupMenu.setOnMenuItemClickListener(menuItemClickListener);
        return popupMenu;
    }

    protected class OptionsPopupMenu extends PopupMenu {
        public OptionsPopupMenu(Context context, View anchor) {
            super(context, anchor, Gravity.END);
        }

        @Override
        public void show() {
            super.show();
        }
    }

    PopupMenu.OnMenuItemClickListener menuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.scan:
                    Intent serverIntent = new Intent(BluetoothChat.this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                    break;
                case R.id.discoverable:
                    ensureDiscoverable();
                    break;
                case R.id.about_app:
                    Toast.makeText(getApplicationContext(), "Formate date", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        } else {
            if (mChatService == null) {
                setupChat();
                //setUpDownChat();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        mConversationArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mOutEditText.getText().toString();
                sendMessage(message);
            }
        });

    /////////////////////////////////////////////////////////////////////
        mUpSendButoon = (Button) findViewById(R.id.button_upsend);
        mUpSendButoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "up" ;
                send_click_bt_Message(message);
            }
        });
        mDownSendButton = (Button) findViewById( R.id.button_downsend);
        mDownSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "down" ;
                send_click_bt_Message(message);
            }
        });
        mstopButton = (Button) findViewById(R.id.button_stop);
        mstopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "stop" ;
                send_click_bt_Message(message);
            }
        });
        mlinkNumButton = (Button) findViewById(R.id.button_get_total_chain);
        mlinkNumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "get_total_chain" ;
                send_click_bt_Message(message);
            }
        });
        mbottomButton = (Button) findViewById(R.id.button_Record_pos_bottom);
        mbottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "record_position_floor_bottom" ;
                send_click_bt_Message(message);
            }
        });
        mtopButton = (Button) findViewById(R.id.button_Record_pos_top);
        mtopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "record_position_floor_top" ;
                send_click_bt_Message(message);
            }
        });
        m2floorButton = (Button) findViewById(R.id.button_Record_pos_floor2);
        m2floorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "record_position_floor2" ;
                send_click_bt_Message(message);
            }
        });

        mchkposButton = (Button) findViewById(R.id.button_chk_pos);
        mchkposButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "check_position" ;
                send_click_bt_Message(message);
            }
        });
        mresetButton = (Button) findViewById(R.id.button_reset);
        mresetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "reset" ;
                send_click_bt_Message(message);
            }
        });
        mdebugButton = (Button) findViewById(R.id.button_debug);
        mdebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "debug" ;
                send_click_bt_Message(message);
            }
        });
        msetButton = (Button) findViewById(R.id.button_setup);
        msetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "setup" ;
                send_click_bt_Message(message);
            }
        });
    ///////////////////////////////////////////////////////////////////*/

        mChatService = new BluetoothChatService(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
    }

//    /////////////////////////////////////////////////////////////////////////////
//    private void setUpDownChat() {
//        mUpSendButoon = (Button) findViewById(R.id.button_upsend);
//        mUpSendButoon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String message = "up" ;
//                sendUpDownMessage(message);
//            }
//        });
//
//
//        mChatService = new BluetoothChatService(this, mHandler);
//        mOutStringBuffer = new StringBuffer("");
//    }
//    /////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mChatService != null)
            mChatService.stop();
    }

    private void ensureDiscoverable() {
        Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    private void send_click_bt_Message(String message) {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (message.length() > 0) {
            byte[] send1 = message.getBytes();
            byte[] send2 = {13,10};
            byte[] send = new byte[send1.length+send2.length];
            System.arraycopy(send1,0,send,0,send1.length);
            System.arraycopy(send2,0,send,send1.length,send2.length);
            mChatService.write(send);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    private void sendMessage(String message) {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (message.length() > 0) {
            String byBuffer = "Rewrite:1:";
            byte[] send0 = byBuffer.getBytes();
            byte[] send1 = message.getBytes();
            byte[] send2 = {13,10};
            byte[] send = new byte[send0.length+send1.length+send2.length];
            System.arraycopy(send0,0,send,0,send0.length);
            System.arraycopy(send1,0,send,send0.length,send1.length);
            System.arraycopy(send2,0,send,send0.length+send1.length,send2.length);
            mChatService.write(send);
            mOutStringBuffer.setLength(0);//清空发送区
            mOutEditText.setText(mOutStringBuffer);
            Toast.makeText(this, "总楼层数设置完成",
                    Toast.LENGTH_LONG).show();
        }
    }
//    private void setMessage(String message) {
//        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
//            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
//                    .show();
//            return;
//        }
//        if (message.length() > 0) {
//            byte[] send = message.getBytes();
//            mChatService.write(send);
//            mOutStringBuffer.setLength(0);//清空发送区
//            mOutEditText.setText(mOutStringBuffer);
//        }
//    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            // If the action is a key-up event on the return key, send the
            // message
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Send:  " + writeMessage);
                    break;
                case MESSAGE_READ:
//                    byte[] temp = (byte[])msg.obj;
//                    byte[] readBuf = new byte[20480];
//                    System.arraycopy(temp,0,readBuf,0,temp.length);
//                    int ret = readBuf.length - 1 ;
//                    while (!(readBuf[ret-1] == 13 && readBuf[ret] == 10)){
//                        byte[] temp1 =readBuf;
//                        byte[] temp2 = (byte[]) msg.obj;
//                        System.arraycopy(temp1,0,readBuf,0,temp1.length);
//                        System.arraycopy(temp2,0,readBuf,temp1.length,temp2.length);
//                    }

//                    byte[] readBuf =(byte[]) msg.obj;
//                    int ret = readBuf.length - 1;
//                    if(!(readBuf[ret-1] == 13 && readBuf[ret] == 10)){
//                        byte[] temp = (byte[]) msg.obj;
//                        byte[] mreadBuf = new byte[readBuf.length+temp.length];
//                        System.arraycopy(readBuf,0,mreadBuf,0,readBuf.length);
//                        System.arraycopy(temp,0,mreadBuf,readBuf.length,temp.length);
//                        String readMessage = new String(mreadBuf, 0, msg.arg1);
//                        mConversationArrayAdapter.add("Receive:  " + readMessage);
//                        break;
//                    }


//                    byte[] readBuf =new byte[msg.arg1];
//                    System.arraycopy(msg.obj,0,readBuf,0,readBuf.length);




                    // construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
                    String readMessage = msg.obj.toString();
//                    if(readMessage.equals("shutdown")){
//                        setMessage("ok");
//                    }
                    mConversationArrayAdapter.addAll(readMessage);
                    /////////////////////
//                    try {
//                        Thread.sleep(1);                 //1000 毫秒，也就是1秒.
//                    } catch(InterruptedException ex) {
//                        Thread.currentThread().interrupt();
//                    }
                    ////////////////////
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                    //setUpDownChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

}
