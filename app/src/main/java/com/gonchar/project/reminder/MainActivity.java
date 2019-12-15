package com.gonchar.project.reminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.gonchar.project.reminder.service.ReminderService;
import com.gonchar.project.reminder.utils.Tools;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import static com.gonchar.project.reminder.utils.Constants.*;


public class MainActivity extends AppCompatActivity {


    private TextInputLayout reminderMessage;
    private TextInputLayout timeValue;
    private SharedPreferences userVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tools.makeCustomToolBar(Objects.requireNonNull(getSupportActionBar()));

        reminderMessage = findViewById(R.id.reminderMessage);
        timeValue = findViewById(R.id.timeValue);
        checkUserSetting();
        reminderMessage.getEditText().addTextChangedListener(counter);

    }


    /**
     * this object - (listener) check length of the reminder message (in field, which user fill).
     */
    TextWatcher counter = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (reminderMessage.getEditText().length() < MIN_MESSAGE_LENGTH) {
                Tools.showError(getText(R.string.MainActivity_showError_method_Error).toString(), reminderMessage);
            } else {
                Tools.showError(null, reminderMessage);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * this method rewrite text field in TextInputLayouts reminderMessage and timeValue if application
     * wos closet but service wos`t stop
     */
    private void checkUserSetting() {
        userVariable = getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);

        if (userVariable.contains(SHARED_PREFERENCES_REMINDER_KEY)) {
            reminderMessage.getEditText().setText(userVariable.getString(SHARED_PREFERENCES_REMINDER_KEY, EMPTY_STRING));
        }
        if (userVariable.contains(USER_SETTING_TIME_VALUE_KEY)) {
            timeValue.getEditText().setText(userVariable.getString(USER_SETTING_TIME_VALUE_KEY, EMPTY_STRING));
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(EXTRAS_MESSAGE_KEY, reminderMessage.getEditText().getText().toString());
        outState.putString(EXTRAS_TIME_VALUE_KEY, timeValue.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        reminderMessage.getEditText().setText(savedInstanceState.getString(EXTRAS_MESSAGE_KEY));
        timeValue.getEditText().setText(savedInstanceState.getString(EXTRAS_TIME_VALUE_KEY));
        super.onRestoreInstanceState(savedInstanceState);
    }


    public void onClickForOffButton(View view) {
        if (Tools.checkServiceRunning(ReminderService.class.getName(), view.getContext())) {
            changeUserSetting(EMPTY_STRING, EMPTY_STRING);
            stopService(new Intent(view.getContext(), ReminderService.class));
        }

    }

    /**
     * this method save user settings (text  rom text fields in reminderMassage and timeValue)
     *
     * @param newReminder  last variant from user or empty string if method coll in onClickForOffButton method
     * @param newTimeValue last variant from user or empty string if method coll in onClickForOffButton method
     */
    void changeUserSetting(String newReminder, String newTimeValue) {
        SharedPreferences.Editor editor = userVariable.edit();
        editor.putString(SHARED_PREFERENCES_REMINDER_KEY, newReminder);
        editor.putString(USER_SETTING_TIME_VALUE_KEY, newTimeValue);
        editor.apply();
    }


    public void onCLickForOnButton(View view) {

        if (Tools.isEmptyMessage(timeValue.getEditText().getText().toString()) ||
                Tools.shouldShowError(MIN_TIME_VALUE, Integer.parseInt(timeValue.getEditText().getText().toString()))) {
            Tools.showError(getText(R.string.MainActivity_onClickForOnButton_argumentInShowErrorMethod_timeValueError).toString(), timeValue);
        } else if (Tools.shouldShowError(MIN_MESSAGE_LENGTH, reminderMessage.getEditText().length())) {
            Tools.showError(null, timeValue);
        } else {
            serviceCheck(view);
        }
    }

    /**
     * this method stat or restart reminder service (check service, is it work in this moment or not)
     * if service is working - calls the stopService method (stop service) then startReminderService
     * method (start with new parameters), if service ist work - calls the tartReminderService method
     * (start with new parameters)
     *
     * @param view object with user interface
     */
    private void serviceCheck(View view) {

        changeUserSetting(reminderMessage.getEditText().getText().toString(),
                timeValue.getEditText().getText().toString());
        if (Tools.checkServiceRunning(ReminderService.class.getName(), view.getContext())) {
            stopService(new Intent(view.getContext(), ReminderService.class));
            startReminderService(view);
        } else {
            startReminderService(view);
        }
    }

    /**
     * this method delete all error message, create new intent object and start reminder service
     *
     * @param view object with user interface
     */
    public void startReminderService(View view) {

        Tools.showError(null, timeValue);
        Intent intent = new Intent(view.getContext(), ReminderService.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRAS_MESSAGE_KEY, Objects.requireNonNull(reminderMessage.getEditText()).getText())
                .putExtra(EXTRAS_TIME_VALUE_KEY, Integer.parseInt(Objects.requireNonNull(timeValue.getEditText()).getText().toString()));
        intent.setAction(ACTION_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);

        } else {
            startService(intent);
        }
    }

}
