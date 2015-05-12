package com.kircherelectronics.accelerationexplorer.filter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;

/*
 * Copyright 2015, Kircher Electronics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Implements a median filter designed to smooth the data points based on a time
 * constant in units of seconds. The median filter will take the mean of the
 * samples that occur over a period defined by the time constant... the number
 * of samples that are considered is known as the filter window. The approach
 * allows the filter window to be defined over a period of time, instead of a
 * fixed number of samples. This is important on Android devices that are
 * equipped with different hardware sensors that output samples at different
 * frequencies and also allow the developer to generally specify the output
 * frequency. Defining the filter window in terms of the time constant allows
 * the mean filter to applied to all sensor outputs with the same relative
 * filter window, regardless of sensor frequency.
 * 
 * @author Kaleb
 * @version %I%, %G%
 * 
 */
public class MedianFilterSmoothing
{
	private static final String tag = MedianFilterSmoothing.class
			.getSimpleName();

	private float timeConstant = 1;
	private float startTime = 0;
	private float timestamp = 0;
	private float hz = 0;

	private int count = 0;
	// The size of the mean filters rolling window.
	private int filterWindow = 20;

	private boolean dataInit;

	private ArrayList<LinkedList<Number>> dataLists;

	/**
	 * Initialize a new MeanFilter object.
	 */
	public MedianFilterSmoothing()
	{
		dataLists = new ArrayList<LinkedList<Number>>();
		dataInit = false;
	}

	public void setTimeConstant(float timeConstant)
	{
		this.timeConstant = timeConstant;
	}

	public void reset()
	{
		startTime = 0;
		timestamp = 0;
		count = 0;
		hz = 0;
	}

	/**
	 * Filter the data.
	 * 
	 * @param iterator
	 *            contains input the data.
	 * @return the filtered output data.
	 */
	public float[] addSamples(float[] data)
	{
		// Initialize the start time.
		if (startTime == 0)
		{
			startTime = System.nanoTime();
		}

		timestamp = System.nanoTime();

		// Find the sample period (between updates) and convert from
		// nanoseconds to seconds. Note that the sensor delivery rates can
		// individually vary by a relatively large time frame, so we use an
		// averaging technique with the number of sensor updates to
		// determine the delivery rate.
		hz = (count++ / ((timestamp - startTime) / 1000000000.0f));

		filterWindow = (int) (hz * timeConstant);

		for (int i = 0; i < data.length; i++)
		{
			// Initialize the data structures for the data set.
			if (!dataInit)
			{
				dataLists.add(new LinkedList<Number>());
			}

			dataLists.get(i).addLast(data[i]);

			if (dataLists.get(i).size() > filterWindow)
			{
				dataLists.get(i).removeFirst();
			}
		}

		dataInit = true;

		float[] medians = new float[dataLists.size()];

		for (int i = 0; i < dataLists.size(); i++)
		{
			medians[i] = (float) getMedian(dataLists.get(i));
		}

		return medians;
	}

	/**
	 * Get the mean of the data set.
	 * 
	 * @param data
	 *            the data set.
	 * @return the mean of the data set.
	 */
	private float getMedian(List<Number> data)
	{
		double[] values = new double[data.size()];

		for (int i = 0; i < values.length; i++)
		{
			values[i] = data.get(i).floatValue();
		}

		return (float) StatUtils.percentile(values, 50);
	}

}
