package com.xero.ca;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class BugReportActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setTitle("BugReport");
        }
        Bundle ex = getIntent().getExtras();
        if (ex != null) {
            final String em = ex.getString("exception", "");
            final int epid = ex.getInt("pid", 0);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("错误")
                    .setCancelable(false)
                    .setMessage("您好，命令助手出现了一个错误。您可以将这个错误反馈给我们，来推动命令助手的更新。您也可以选择忽略。" +
                            "作者联系方式：QQ-814518615(Xero);QQ群-303697689\n\n" +
                            "错误信息：\n" + em)
                    .setPositiveButton("复制并关闭", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dia, int w) {
                            setClipText(em);
                            finish();
                        }
                    })
                    .setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dia, int w) {
                            finish();
                        }
                    });
            if (epid != 0) {
                builder.setNeutralButton("立即停止", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dia, int w) {
                        finish();
                        android.os.Process.killProcess(epid);
                        System.exit(0);
                    }
                });
            }
            builder.show();
        }
    }

    private void setClipText(String str) {
        ClipboardManager cm = ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("", str));
    }
}
