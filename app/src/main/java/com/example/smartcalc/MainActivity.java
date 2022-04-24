package com.example.smartcalc;

import static android.view.View.*;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private int[] numericButtons = {R.id.btnzero, R.id.btnone, R.id.btntwo, R.id.btnthree, R.id.btnfour, R.id.btnfive, R.id.btnsix, R.id.btnseven, R.id.btneight, R.id.btnnine};
    private int[] operatorsButtons = {R.id.btnadd, R.id.btnmultiply, R.id.btndivide, R.id.btnsub};
    private TextView txtScreen;
    private boolean lastNumeric;
    private boolean stateError;
    private boolean lastDot;
    private ImageButton btnSpeak;
    private  ImageButton getBtnSpeak;
    TextToSpeech textToSpeech ;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    Switch aSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        btnSpeak = findViewById(R.id.btnspk);
        txtScreen = findViewById(R.id.txtscreen);
        aSwitch =findViewById(R.id.switcher);
        setNUmericOnClickListner();
        setOperatorOnClickListner();


        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if(b){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
        textToSpeech = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i== TextToSpeech.SUCCESS){
                            int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                        }
                    }

                });
        btnSpeak.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                String s = txtScreen.getText().toString();

                int speech = textToSpeech.speak(s,TextToSpeech.QUEUE_FLUSH,null);
            }



        });


    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        int action,keycode;
        action=event.getAction();
        keycode = event.getKeyCode();
        switch (keycode) {
            case KeyEvent.KEYCODE_VOLUME_UP:{
                if(KeyEvent.ACTION_UP==action ){
                       promptSpeechInput();
                    }

                }

            case KeyEvent.KEYCODE_VOLUME_DOWN:{
                if(KeyEvent.ACTION_DOWN==action){

                    String s = txtScreen.getText().toString();
                    int speech = textToSpeech.speak(s,TextToSpeech.QUEUE_FLUSH,null);
            }}
            }


        return super.dispatchKeyEvent(event);

    }

    private void setNUmericOnClickListner() {
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (stateError) {
                    txtScreen.setText(button.getText());
                    stateError = false;
                } else {
                    txtScreen.append(button.getText());
                }lastNumeric =true;
            }
     };
        for (int id : numericButtons) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void setOperatorOnClickListner() {
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (lastNumeric && !stateError) {
                    Button button = (Button) view;
                    txtScreen.append(button.getText());
                    lastNumeric = false;
                    lastDot = false;

                }
            }
        };
        for (int id : operatorsButtons) {
            findViewById(id).setOnClickListener(listener);
        }
        findViewById(R.id.btndot).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !stateError && !lastDot) {
                    txtScreen.append(".");
                    lastNumeric = false;
                    lastDot = false;
                }
            }
        });//clear button
        findViewById(R.id.btnac).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtScreen.setText("");
                lastNumeric = false;
                stateError = false;
                lastDot = false;
            }
        });
        //equal button
        findViewById(R.id.btnequal).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onEqual();
            }
        });
        //speak button
        findViewById(R.id.btnmic).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateError) {
                    txtScreen.setText("Try Again ..!!");
                    stateError = false;
                } else {
                    promptSpeechInput();
                }
                lastNumeric = true;
            }

        });

    }


    private void onEqual() {
        if (lastNumeric && !stateError) {
            String txt = txtScreen.getText().toString();
            try {
                Expression expression = null;
                try {
                    expression = new ExpressionBuilder(txt).build();
                    double result = expression.evaluate();
                    txtScreen.setText(Double.toString(result));
                } catch (Exception e) {
                    txtScreen.setText("Error");
                }

            } catch (ArithmeticException ex) {
                txtScreen.setText("ERROR");
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode,Intent data  ) {
        super.onActivityResult(requestCode,resultCode ,data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:{
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String change = result.get(0);
                    change = change.replace("x", "*");
                    change = change.replace("X", "*");
                    change = change.replace("into", "*");
                    change = change.replace("Multiply", "*");
                    change = change.replace("times", "*");
                    change = change.replace("in2", "*");
                    change = change.replace("plus", "+");
                    change = change.replace("addition", "+");
                    change = change.replace("add", "+");
                    change = change.replace("minus", "-");
                    change = change.replace("subract", "-");
                    change = change.replace("Divide by", "/");
                    change = change.replace("Divided by", "/");
                    change = change.replace("equal ", "=");
                    change = change.replace("equals", "=");
                    if (change.contains("=")) {
                         change = change.replace("=", "=");
                        txtScreen.setText(change);
                        onEqual();
                    } else {
                        txtScreen.setText(change);}
                    }
                    break;

                }
        }
    }
}