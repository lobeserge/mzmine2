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


package net.sf.mzmine.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import net.sf.mzmine.data.Scan;
import net.sf.mzmine.methods.Method;
import net.sf.mzmine.methods.MethodParameters;


public interface RawDataFile extends Serializable {

    class Operation {
        public File previousFileName;
        public Method processingMethod;
        public MethodParameters parameters;
    }


    public enum PreloadLevel { NO_PRELOAD, PRELOAD_ALL_SCANS };

    public File getOriginalFile();
    public File getCurrentFile();

    public Vector<Operation> getHistory();
    public void addHistory(File previousFileName, Method processingMethod, MethodParameters parameters);

    public PreloadLevel getPreloadLevel();

    public int getNumOfScans();
    public int[] getMSLevels();
    
    /**
     * Returns sorted array of all scan numbers in given MS level 
     * @param msLevel MS level
     * @return Sorted array of scan numbers, never returns null
     */
    public int[] getScanNumbers(int msLevel);
    
    /**
     * Returns sorted array of all scan numbers in given MS level and retention time range
     * @param msLevel MS level
     * @param rtMin Minimum retention time
     * @param rtMax Maximum retention time
     * @return Sorted array of scan numbers, never returns null
     */
    public int[] getScanNumbers(int msLevel, double rtMin, double rtMax);
    
    /**
     * Returns sorted array of all scan numbers in this file
     * @return Sorted array of scan numbers, never returns null
     */
    public int[] getScanNumbers();
    
    /**
     * Returns a retention time of a given scan
     * @param scanNumber Desired scan number
     * @return Scan's retention time
     */
    public double getRetentionTime(int scanNumber);
    

    /**
     * This method may parse the RAW data file, therefore it may be quite slow.
     * @param scan Desired scan number
     * @return Desired scan
     */
    public Scan getScan(int scan) throws IOException;

    public String getDataDescription();

    public double getDataMinMZ(int msLevel);
    public double getDataMaxMZ(int msLevel);
    public double getDataMinRT(int msLevel);
    public double getDataMaxRT(int msLevel);
    public double getDataMaxBasePeakIntensity(int msLevel);
    public double getDataMaxTotalIonCurrent(int msLevel);

    /**
     * @return filename
     */
    public String toString();
    
    public RawDataFileWriter createNewTemporaryFile() throws IOException;

}
