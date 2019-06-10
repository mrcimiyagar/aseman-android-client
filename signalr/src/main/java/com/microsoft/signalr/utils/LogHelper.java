package com.microsoft.signalr.utils;

import android.util.Log;

import androidx.core.util.Pair;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogHelper {

    private static LogHelper instance = new LogHelper();

    public LogHelper() {
        this.alive = true;
        this.runningThread.start();
    }

    private BlockingQueue<Pair<String, String>> logs = new LinkedBlockingQueue<>();
    private boolean alive;
    private Thread runningThread = new Thread(() -> {
        try {
            while (alive) {
                Pair<String, String> log = logs.take();
                if (log.first != null && log.second != null) {
                    Log.d(log.first, "--------------------------------------------------------------------");
                    String message = log.second;
                    int counter = 0;
                    while (message.length() > 200) {
                        String section = message.substring(0, 200);
                        Log.d(log.first + "#" + counter, section);
                        message = message.substring(200);
                        counter++;
                    }
                    if (message.length() > 0)
                        Log.d(log.first + "#" + counter, message);
                    Log.d(log.first, "--------------------------------------------------------------------");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });

    public static void log(String tag, String message) {
        instance.logs.offer(new Pair<>(tag, message));
    }
}
