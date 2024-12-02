package com.cocoa.myjavaapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static Context context;
    private BluetoothAdapter bluetoothAdapter; //블루투스 어댑터
    private Set<BluetoothDevice> devices; //블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; //블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; //블루투스 소켓
    private OutputStream outputStream = null; //블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; //블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; //문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; //수신된 문자열 저장 버퍼
    private int readBufferPosition; //버퍼 내 문자 저장 위치
    String[] array = {"0"}; //수신된 문자열을 쪼개서 저장할 배열

    private boolean isActuatingUp = false;
    private boolean isActuatingDown = false;

    public static User defaultUser = new User("홍길동", 0);
    public static User user = defaultUser;

    Button btnSet;
    Button btnView;
    Button btnDelete;
    Button btnPairing;
    Button btnActUp;
    Button btnActDown;
    Button btnActStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        refreshTitle();

        btnSet = findViewById(R.id.btnMainProfileSet);
        btnSet.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), ProfileSetActivity.class);
                startActivity(intent1);
            }
        });
        btnView = findViewById(R.id.btnMainProfileView);
        btnView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getApplicationContext(), ProfileViewActivity.class);
                startActivity(intent2);
            }
        });
        btnDelete = findViewById(R.id.btnMainProfileReset);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("프로필 삭제").setMessage("저장된 프로필을 초기화하겠습니까? 되돌릴 수 없습니다.");

                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.resetProfile();
                        Toast.makeText(getApplicationContext(), "프로필이 초기화되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {}
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch bluetoothSwitch = findViewById(R.id.btnMainSwitch);
        bluetoothSwitch.setChecked(bluetoothAdapter.isEnabled());
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableBluetooth();
                } else {
                    disableBluetooth();
                }
            }
        });

        btnPairing = findViewById(R.id.btnMainPairing);
        btnPairing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled()) {
                    selectBluetoothDevice();
                } else {
                    showToast(getApplicationContext(), "블루투스를 먼저 켜 주세요.");
                }
            }
        });

        btnActUp = findViewById(R.id.btnMainActUp);
        btnActUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actUp();
            }
        });

        btnActDown = findViewById(R.id.btnMainActDown);
        btnActDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actDown();
            }
        });

        btnActStop = findViewById(R.id.btnMainActStop);
        btnActStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actStop();
            }
        });
    }

    public void refreshTitle() {
        TextView greetings = findViewById(R.id.tvMainTitle);
        greetings.setText(Messages.get("반갑습니다, %s님.", MainActivity.user.name));
    }

    public void bluetoothPrint(String s) {
        TextView btOutput = findViewById(R.id.tvMainBluetoothOutput);
        btOutput.setText(Messages.get(s));
    }

    public void setBluetoothName(String name) {
        TextView connected = findViewById(R.id.tvMainBluetoothName);
        if (name != null) {
            connected.setText(Messages.get("연결된 기기: " + name));
        } else {
            connected.setText(Messages.get(""));
        }
    }

    private void actUp() {
        if (isActuatingUp) {
            actStop();
            return;
        }
        isActuatingUp = true;
        isActuatingDown = false;
        sendData("up");
    }

    private void actDown() {
        if (isActuatingDown) {
            actStop();
            return;
        }
        isActuatingUp = false;
        isActuatingDown = true;
        sendData("down");
    }

    private void actStop() {
        isActuatingUp = false;
        isActuatingDown = false;
        sendData("stop");
    }

    private void sendData(String message) {
        if (outputStream != null) {
            try {
                outputStream.write((message + "\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void resetProfile() {
        MainActivity.user = MainActivity.defaultUser;
        ((MainActivity)MainActivity.context).refreshTitle();
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            @SuppressLint("MissingPermission")
            boolean success = bluetoothAdapter.enable();
            if (success) {
                showToast("블루투스를 켰습니다.");
            } else {
                showToast("블루투스를 켤 수 없었습니다.");
            }
        }
    }

    private void disableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            @SuppressLint("MissingPermission")
            boolean success = bluetoothAdapter.disable();
            if (success) {
                showToast("블루투스를 껐습니다.");
                setBluetoothName(null);
            } else {
                showToast("블루투스를 끌 수 없었습니다.");
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void selectBluetoothDevice() {
        int pairedDeviceCount;
        //이미 페어링되어 있는 블루투스 기기를 탐색
        devices = bluetoothAdapter.getBondedDevices();
        //페어링된 디바이스 배열의 크기 저장
        pairedDeviceCount =  devices.size();
        //페어링된 장치가 없는 경우
        if (pairedDeviceCount == 0) {
            //페어링 하기 위한 함수 호출
        } else {
            //디바이스를 선택하기 위한 대화상자 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 된 블루투스 디바이스 목록");
            //페어링된 각각의 디바이스의 이름과 주소를 저장하기 위한 리스트 생성
            List<String> list = new ArrayList<>();
            //모든 디바이스의 이름을 리스트에 추가
            for (BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");

            //list를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            //해당 항목을 눌렀을 때 호출되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });
            //뒤로가기 버튼을 눌러도 창이 닫히지 않도록 설정
            builder.setCancelable(false);
            //다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @SuppressLint("MissingPermission")
    public void connectDevice(String deviceName) {
        //페어링된 디바이스 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                showToast(getApplicationContext(), bluetoothDevice.getName() + " 연결 완료");
                //UUID 생성
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

                setBluetoothName(bluetoothDevice.getName());

                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    bluetoothSocket.connect();

                    outputStream = bluetoothSocket.getOutputStream();
                    inputStream = bluetoothSocket.getInputStream();
                    receiveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void receiveData() {
        final Handler handler = new Handler();
        //데이터 수신을 위한 버퍼 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        //데이터 수신을 위한 스레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        //데이터 수신 확인
                        int byteAvailable = inputStream.available();
                        //데이터가 수신된 경우
                        if (byteAvailable > 0) {
                            //입력 스트림에서 바이트 단위로 읽어 옴
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            //입력 스트림 바이트를 한 바이트씩 읽어 옴
                            for (int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                //개행문자를 기준으로 받음
                                if (tempByte == '\n') {
                                    //readBuffer 배열을 encodeBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    //인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, StandardCharsets.UTF_8);
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //여기에서 센서값을 받는다.
                                            bluetoothPrint(text + " g"); //받은 값 출력
                                        }
                                    });
                                } else { //개행문자가 아닐 경우
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    //0.1초마다 받아옴
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        workerThread.start();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


}