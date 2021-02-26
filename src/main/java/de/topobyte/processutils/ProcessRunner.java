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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProcessRunner
{

	private Process process;

	private Collector collector1;
	private Collector collector2;

	private Thread thread1;
	private Thread thread2;

	private byte[] stdOut = null;
	private byte[] stdErr = null;

	public ProcessRunner(Process process)
	{
		this.process = process;
		InputStream input = process.getInputStream();
		InputStream error = process.getErrorStream();
		InputStream stdin = new BufferedInputStream(input);
		InputStream stderr = new BufferedInputStream(error);

		collector1 = new Collector(stdin);
		collector2 = new Collector(stderr);
		thread1 = new Thread(collector1);
		thread2 = new Thread(collector2);
		thread1.start();
		thread2.start();
	}

	/**
	 * Wait for the process passed to the constructor to finish. Afterwards you
	 * can use {@link #getStdOut()} and {@link #getStdErr()} to retrieve the
	 * bytes that the process has written to its output streams.
	 * 
	 * @return the return value of the process.
	 */
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

		Threads.join(thread1);
		Threads.join(thread2);
		stdOut = collector1.baos.toByteArray();
		stdErr = collector2.baos.toByteArray();
		return returnValue;
	}

	/**
	 * Get the bytes that the process has written to stdout. Make sure to call
	 * this after calling {@link #waitForEnd()}.
	 * 
	 * @return the bytes the process has written to stdout.
	 */
	public byte[] getStdOut()
	{
		return stdOut;
	}

	/**
	 * Get the bytes that the process has written to stderr. Make sure to call
	 * this after calling {@link #waitForEnd()}.
	 * 
	 * @return the bytes the process has written to stderr.
	 */
	public byte[] getStdErr()
	{
		return stdErr;
	}

	class Collector implements Runnable
	{

		private InputStream input;
		private ByteArrayOutputStream baos = new ByteArrayOutputStream();

		public Collector(InputStream input)
		{
			this.input = input;
		}

		@Override
		public void run()
		{
			while (true) {
				try {
					int b = input.read();
					if (b < 0) {
						break;
					}
					baos.write(b);
				} catch (IOException e) {
					continue;
				}
			}
		}
	}

}
