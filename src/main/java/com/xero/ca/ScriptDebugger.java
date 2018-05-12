package com.xero.ca;
import java.io.*;
import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;
import java.util.*;
import android.util.*;
import android.os.*;

public class ScriptDebugger implements Debugger {
	private PrintWriter mWriter;
	
	public ScriptDebugger(File output) throws FileNotFoundException {
		mWriter = new PrintWriter(output);
		d("Start debug at " + new Date().toString());
	}
	
	public void d(String s) {
		mWriter.append(Long.toString(SystemClock.uptimeMillis())).append(":").append(s).append("\n");
		Log.d("ca", s);
	}
	
	@Override
	public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {}

	@Override
	public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
		int[] ln = fnOrScript.getLineNumbers();
		d("Entering " + (ln.length > 0 ? Integer.toString(ln[0]) : "unknown"));
		return new Frame(cx, fnOrScript);
	}
	
	public class Frame implements DebugFrame {
		private Context cx;
		private DebuggableScript script;
		public Frame(Context cx, DebuggableScript script) {
			this.cx = cx;
			this.script = script;
		}
		
		@Override
		public void onEnter(Context cx, Scriptable activation, Scriptable thisObj, Object[] args) {
			d("Exec " + activation);
		}

		@Override
		public void onLineChange(Context cx, int lineNumber) {}

		@Override
		public void onExceptionThrown(Context cx, Throwable ex) {
			d("Caught error:" + ex);
		}

		@Override
		public void onExit(Context cx, boolean byThrow, Object resultOrException) {
			d("Back:" + resultOrException);
		}

		@Override
		public void onDebuggerStatement(Context cx) {}
	}
}
