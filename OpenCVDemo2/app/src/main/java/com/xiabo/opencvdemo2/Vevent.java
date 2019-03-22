package com.xiabo.opencvdemo2;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Vevent {
    public void executeCommand(String command) throws InterruptedException, IOException, TimeoutException, RootDeniedException {
        Command cmd = new Command(0, command);
        RootTools.getShell(false).add(cmd);
    }

    public void sendevent(String event_num, int param_1, int param_2, long param_3) {
        try {
            executeCommand(String.format("sendevent /dev/input/%s %d %d %s", event_num, param_1, param_2, param_3));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }
}
