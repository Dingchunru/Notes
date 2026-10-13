package com.dingchunru.app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.dingchunru.app.R;

public class PasswordDialog {

    public interface OnPasswordEntered {
        void onEntered(String password);
    }

    /** 解锁：只输入一次 */
    public static void showUnlock(Context ctx, String title, OnPasswordEntered cb) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_password, null);
        EditText input = v.findViewById(R.id.password_input);

        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setView(v)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String pwd = input.getText().toString();
                    if (!pwd.isEmpty()) cb.onEntered(pwd);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** 设置/修改：输入两次确认 */
    public static void showSetup(Context ctx, String title, OnPasswordEntered cb) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_password, null);
        EditText input   = v.findViewById(R.id.password_input);
        EditText confirm = v.findViewById(R.id.password_confirm);
        confirm.setVisibility(View.VISIBLE);

        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setView(v)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String p1 = input.getText().toString();
                    String p2 = confirm.getText().toString();
                    if (p1.isEmpty()) return;
                    if (!p1.equals(p2)) return;
                    cb.onEntered(p1);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}