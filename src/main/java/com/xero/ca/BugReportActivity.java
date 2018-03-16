package com.xero.ca;
import android.app.*;
import android.content.*;
import android.os.*;

public class BugReportActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getActionBar() != null) {
			getActionBar().setTitle("BugReport");
		}
		Bundle ex = getIntent().getExtras();
		final String em = ex.getString("exception", "");
		new AlertDialog.Builder(this)
			.setTitle("错误")
			.setCancelable(false)
			.setMessage("您好，命令助手出现了一个致命错误。您可以将这个错误反馈给我们，来推动命令助手的更新。您也可以选择忽略。" +
					"作者联系方式：QQ-814518615(Xero)\n\n" +
					"错误信息：\n" + em)
			.setPositiveButton("复制并关闭", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dia, int w) {
					setClipText(em);
					finish();
				}
			})
			.show();
	}
	
	private void setClipText(String str) {
		if (Build.VERSION.SDK_INT >= 11) {
			((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("", str));
		} else {
			((android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setText(str);
		}
	}
}
