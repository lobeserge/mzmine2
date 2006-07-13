/*
 * Copyright 2006 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.taskcontrol.impl;

import java.beans.PropertyVetoException;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.table.TableModel;

import net.sf.mzmine.io.IOController;
import net.sf.mzmine.main.MZmineModule;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskController;
import net.sf.mzmine.taskcontrol.TaskListener;
import net.sf.mzmine.taskcontrol.Task.TaskPriority;
import net.sf.mzmine.taskcontrol.Task.TaskStatus;
import net.sf.mzmine.userinterface.Desktop;
import net.sf.mzmine.userinterface.dialogs.TaskProgressWindow;
import net.sf.mzmine.userinterface.mainwindow.MainWindow;

/**
 * 
 */
public class TaskControllerImpl implements TaskController, MZmineModule, Runnable {

    private IOController ioController;
    private Desktop desktop;
    private Logger logger;
    
    // TODO: always create a worker thread for high priority tasks
    
    
    private static TaskControllerImpl myInstance;

    private final int TASKCONTROLLER_THREAD_SLEEP = 100;

    private Thread taskControllerThread;

    private WorkerThread[] workerThreads;

    private TaskQueue taskQueue;

    /**
     * 
     */
    public TaskControllerImpl(int numberOfThreads) {

        assert myInstance == null;
        myInstance = this;

        taskQueue = new TaskQueue();

        taskControllerThread = new Thread(this, "Task controller thread");
        taskControllerThread.setPriority(Thread.MIN_PRIORITY);
        taskControllerThread.start();

        workerThreads = new WorkerThread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            workerThreads[i] = new WorkerThread(i, desktop);
            workerThreads[i].start();
        }

    }

    public void addTask(Task task) {
        addTask(task, TaskPriority.NORMAL, null);
    }

    public void addTask(Task task, TaskPriority priority) {
        addTask(task, priority, null);
    }

    public void addTask(Task task, TaskListener listener) {
        addTask(task, TaskPriority.NORMAL, listener);
    }

    public void addTask(Task task, TaskPriority priority, TaskListener listener) {

        assert task != null;

        WrappedTask newQueueEntry = new WrappedTask(task, priority, listener);

        logger.finest("Adding task " + task.getTaskDescription()
            + " to the task controller queue");

        taskQueue.addWrappedTask(newQueueEntry);

        synchronized (this) {
            this.notifyAll();
        }

        /*
         * show the task list component
         */
        MainWindow mainWindow = (MainWindow) desktop;
        if (mainWindow != null) {
            TaskProgressWindow tlc = mainWindow.getTaskList();
            JInternalFrame selectedFrame = desktop.getSelectedFrame();

            tlc.setVisible(true);
            if (selectedFrame != null) {
                try {
                    selectedFrame.setSelected(true);
                } catch (PropertyVetoException e) {
                    // do nothing
                }
            }
            // currentFocus.requestFocus();
        }

    }

    /**
     * Task controller thread main method.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        while (true) {

            /* if the queue is empty, we can sleep */
            while (taskQueue.isEmpty()) {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            WrappedTask[] queueSnapshot = taskQueue.getQueueSnapshot();

            // for each task, check if it's assigned
            for (WrappedTask task : queueSnapshot) {

                TaskListener listener = task.getListener();

                if (!task.isAssigned()) {
                    // poll local threads

                    for (WorkerThread worker : workerThreads) {

                        if (worker.getCurrentTask() == null) {
                            logger.finest("Assigning task " + task.getTask().getTaskDescription() + " to the worker thread " + worker.toString());
                            if (listener != null)
                                listener.taskStarted(task.getTask());
                            worker.setCurrentTask(task);
                            break;
                        }

                    }

                    // TODO: poll remote nodes

                }

                /* check whether the task is finished */
                TaskStatus status = task.getTask().getStatus();
                if ((status == TaskStatus.FINISHED)
                        || (status == TaskStatus.ERROR)
                        || (status == TaskStatus.CANCELED)) {
                    if (listener != null)
                        listener.taskFinished(task.getTask());
                    taskQueue.removeWrappedTask(task);
                }

            }

            MainWindow mainWindow = (MainWindow) desktop;
            if (taskQueue.isEmpty() && (mainWindow != null)) {
                TaskProgressWindow tlc = mainWindow.getTaskList();
                tlc.setVisible(false);
            } else {
                taskQueue.refresh();
            }

            try {
                Thread.sleep(TASKCONTROLLER_THREAD_SLEEP);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

    }

    public void setTaskPriority(Task task, TaskPriority priority) {
        WrappedTask wt = taskQueue.getWrappedTask(task);
        if (wt != null)
            wt.setPriority(priority);
    }

    public TableModel getTaskTableModel() {
        return taskQueue;
    }

    public Task getTask(int index) {
        WrappedTask wt = taskQueue.getWrappedTask(index);
        if (wt != null)
            return wt.getTask();
        else
            return null;
    }

    /**
     * @see net.sf.mzmine.main.MZmineModule#initModule(net.sf.mzmine.io.IOController, net.sf.mzmine.taskcontrol.TaskController, net.sf.mzmine.userinterface.Desktop, java.util.logging.Logger)
     */
    public void initModule(IOController ioController, TaskController taskController, Desktop desktop, Logger logger) {
        this.ioController = ioController;
        this.desktop = desktop;
        this.logger = logger;
        
    }

}
