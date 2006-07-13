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

package net.sf.mzmine.methods.filtering.zoomscan;

import java.io.IOException;

import net.sf.mzmine.data.Scan;
import net.sf.mzmine.io.RawDataFile;
import net.sf.mzmine.io.RawDataFileWriter;
import net.sf.mzmine.taskcontrol.Task;

/**
 * 
 */
class ZoomScanFilterTask implements Task {

    private RawDataFile rawDataFile;
    private ZoomScanFilterParameters parameters;
    private TaskStatus status;
    private String errorMessage;

    // private int[] scanNumbers;

    private int filteredScans;
    private int totalScans;

    private RawDataFile filteredRawDataFile;

    /**
     * @param rawDataFile
     * @param parameters
     */
    ZoomScanFilterTask(RawDataFile rawDataFile,
            ZoomScanFilterParameters parameters) {
        status = TaskStatus.WAITING;
        this.rawDataFile = rawDataFile;
        this.parameters = parameters;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getTaskDescription()
     */
    public String getTaskDescription() {
        return "Zoom scan filtering " + rawDataFile;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getFinishedPercentage()
     */
    public float getFinishedPercentage() {
        if (totalScans == 0)
            return 0.0f;
        return (float) filteredScans / totalScans;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getStatus()
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getErrorMessage()
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getResult()
     */
    public Object getResult() {
        Object[] results = new Object[3];
        results[0] = rawDataFile;
        results[1] = filteredRawDataFile;
        results[2] = parameters;

        return results;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#cancel()
     */
    public void cancel() {
        status = TaskStatus.CANCELED;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        status = TaskStatus.PROCESSING;

        // Create new temporary copy
        RawDataFileWriter rawDataFileWriter;
        try {
            rawDataFileWriter = rawDataFile.createNewTemporaryFile();
        } catch (IOException e) {
            status = TaskStatus.ERROR;
            errorMessage = e.toString();
            return;
        }

        int[] scanNumbers = rawDataFile.getScanNumbers();
        totalScans = scanNumbers.length;

        // Loop through all scans
        for (int scani = 0; scani < totalScans; scani++) {
            Scan sc;
            try {
                sc = rawDataFile.getScan(scanNumbers[scani]);
            } catch (IOException e) {
                status = TaskStatus.ERROR;
                errorMessage = e.toString();
                return;
            }

            // Check if mz range is wide enough
            double mzMin = sc.getMZRangeMin();
            double mzMax = sc.getMZRangeMax();
            if ((mzMax - mzMin) < parameters.minMZRange) {
                continue;
            }

            try {
                rawDataFileWriter.addScan(sc);

            } catch (IOException e) {
                status = TaskStatus.ERROR;
                errorMessage = e.toString();
                return;
            }

            filteredScans++;

        }

        // Finalize writing
        try {
            filteredRawDataFile = rawDataFileWriter.finishWriting();
        } catch (IOException e) {
            status = TaskStatus.ERROR;
            errorMessage = e.toString();
            return;
        }

        status = TaskStatus.FINISHED;

    }

}
