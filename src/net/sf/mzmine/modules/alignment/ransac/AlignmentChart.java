/*
 * Copyright 2006-2009 The MZmine 2 Development Team
 *
 * This file is part of MZmine 2.
 *
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package net.sf.mzmine.modules.alignment.ransac;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class AlignmentChart extends JInternalFrame implements ActionListener {

	XYSeriesCollection dataset;
	Vector<XYSeriesCollection> datasets;
	Vector<String> names;
	JFreeChart chart;
	JButton right, left;
	int cont = 0;

	public AlignmentChart(String name) {
		super(name, true, true, true, true);
		try {
			this.setSize(900, 800);
			dataset = new XYSeriesCollection();
			chart = ChartFactory.createXYLineChart(
					name,
					"RT1",
					"RT2",
					dataset,
					PlotOrientation.VERTICAL,
					true,
					true,
					false);
			ChartPanel chartPanel = new ChartPanel(chart);
			right = new JButton(">");
			right.addActionListener(this);
			left = new JButton("<");
			left.addActionListener(this);

			JPanel buttons = new JPanel();
			buttons.add(left);
			buttons.add(right);
			JPanel chartAndButtons = new JPanel();

			chartAndButtons.add(chartPanel, BorderLayout.NORTH);
			chartAndButtons.add(buttons, BorderLayout.SOUTH);
			this.add(chartAndButtons);



		} catch (Exception e) {
		}
	}

	/**
	 * Remove all series from the chart
	 */
	public void removeSeries() {
		try {
			dataset.removeAllSeries();
		} catch (Exception e) {
		}
	}

	/**
	 * Add new serie.
	 * @param v Vector with the alignments
	 * @param Name Name of the type of lipids in this alignment
	 */
	public void addSeries(Vector<AlignStructMol> data, String title) {
		try {
			chart.setTitle(title);
			XYSeries s1 = new XYSeries("Aligned Molecules");
			XYSeries s2 = new XYSeries("Non aligned Molecules");

			for (AlignStructMol point : data) {

				if (point.Aligned) {
					s1.add(point.row1.getPeaks()[0].getRT(), point.row2.getPeaks()[0].getRT());
				} else {
					s2.add(point.row1.getPeaks()[0].getRT(), point.row2.getPeaks()[0].getRT());
				}

			}

			this.dataset.addSeries(s1);
			this.dataset.addSeries(s2);

			datasets.addElement(dataset);
			names.addElement(title);


		} catch (Exception e) {
		}
	}

	/**
	 * Print the chart
	 */
	public void printAlignmentChart() {
		try {
			XYPlot plot = chart.getXYPlot();
			NumberAxis xAxis = new NumberAxis("RT 1");
			NumberAxis yAxis = new NumberAxis("RT 2");
			xAxis.setAutoRangeIncludesZero(false);
			yAxis.setAutoRangeIncludesZero(false);
			plot.setDomainAxis(xAxis);
			plot.setRangeAxis(yAxis);

			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			renderer.setBaseLinesVisible(false);
			renderer.setBaseShapesVisible(true);
			plot.setRenderer(renderer);

			chart.setBackgroundPaint(Color.white);
			plot.setOutlinePaint(Color.black);

		} catch (Exception e) {
		}

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.right) {
			try {
				if (cont < datasets.size()) {
					chart = ChartFactory.createXYLineChart(
							names.elementAt(cont),
							"RT1",
							"RT2",
							datasets.elementAt(cont++),
							PlotOrientation.VERTICAL,
							true,
							true,
							false);
					printAlignmentChart();

				}
			} catch (Exception exception) {
			}
		}
		if (e.getSource() == this.left) {

			try {
				if (cont >= 0) {
					chart = ChartFactory.createXYLineChart(
							names.elementAt(cont),
							"RT1",
							"RT2",
							datasets.elementAt(cont--),
							PlotOrientation.VERTICAL,
							true,
							true,
							false);
					printAlignmentChart();
				}
			} catch (Exception exception) {
			}
		}
	}
}
