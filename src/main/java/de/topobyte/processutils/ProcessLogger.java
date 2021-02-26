// Copyright 2021 Sebastian Kuerten
//
// This file is part of process-utils.
//
// process-utils is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// process-utils is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with process-utils. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.processutils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

public class ProcessLogger
{

	private Process process;

	public ProcessLogger(Process process, final Logger logger)
	{
		this.process = process;
		InputStream input = process.getInputStream();
		InputStream error = process.getErrorStream();
		BufferedInputStream stdin = new BufferedInputStream(input);
		BufferedInputStream stderr = new BufferedInputStream(error);

		Collector collector1 = new Collector(stdin) {

			@Override
			protected void print(String line)
			{
				logger.info(line);
			}

		};
		Collector collector2 = new Collector(stderr) {

			@Override
			protected void print(String line)
			{
				logger.error(line);
			}

		};
		Thread thread1 = new Thread(collector1);
		Thread thread2 = new Thread(collector2);
		thread1.start();
		thread2.start();
	}

	public int waitForEnd()
	{
		int returnValue = 0;
		while (true) {
			try {
				returnValue = process.waitFor();
				break;
			} catch (InterruptedException e) {
				// continue
			}
		}
		return returnValue;
	}

	abstract class Collector implements Runnable
	{

		private BufferedReader reader;

		public Collector(InputStream input)
		{
			reader = new BufferedReader(new InputStreamReader(input));
		}

		protected abstract void print(String line);

		@Override
		public void run()
		{
			while (true) {
				try {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					print(line);
				} catch (IOException e) {
					print("Error: " + e.getMessage());
				}
			}
		}
	}

}
