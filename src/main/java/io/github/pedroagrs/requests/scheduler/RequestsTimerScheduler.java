package io.github.pedroagrs.requests.scheduler;

import java.util.Timer;
import java.util.TimerTask;

public class RequestsTimerScheduler extends TimerTask {

    private final Runnable runnable;

    public RequestsTimerScheduler(long delayInSeconds, Runnable runnable) {
        this.runnable = runnable;

        new Timer().schedule(this, 1000 * delayInSeconds, 1000 * delayInSeconds);
    }

    @Override
    public void run() {
        runnable.run();
    }
}
