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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Processes
{

	public static int trySimple(ProcessBuilder pb, String errorMessage)
	{
		int returnValue = -1;
		try {
			returnValue = execute(pb);
		} catch (IOException e) {
			System.out.println(errorMessage);
		}
		if (returnValue != 0) {
			System.out.println(errorMessage + ": " + returnValue);
		}
		return returnValue;
	}

	public static void tryWithTimeout(ProcessBuilder pb, String errorMessage,
			int seconds) throws TimeoutException
	{
		int returnValue = -1;
		try {
			returnValue = execute(pb, seconds);
		} catch (IOException e) {
			System.out.println(errorMessage);
			System.exit(1);
		} catch (TimeoutException e) {
			throw e;
		}
		if (returnValue != 0) {
			System.out.println(errorMessage + ": " + returnValue);
			System.exit(1);
		}
	}

	public static void tryAndExitIfFails(ProcessBuilder pb, String errorMessage)
	{
		int returnValue = -1;
		try {
			returnValue = execute(pb);
		} catch (IOException e) {
			System.out.println(errorMessage);
			System.exit(1);
		}
		if (returnValue != 0) {
			System.out.println(errorMessage + ": " + returnValue);
			System.exit(1);
		}
	}

	private static int execute(ProcessBuilder pb) throws IOException
	{
		Process process = pb.start();

		BufferedReader b = new BufferedReader(
				new InputStreamReader(process.getInputStream()));

		while (true) {
			int c = b.read();
			if (c < 0) {
				break;
			}
			System.out.print((char) c);
		}

		while (true) {
			try {
				return process.waitFor();
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	public static int execute(ProcessBuilder pb, long timeoutSeconds)
			throws IOException, TimeoutException
	{
		Process process = pb.start();

		Worker worker = new Worker(process);
		worker.start();
		long timeout = timeoutSeconds * 1000;
		long timeLeft = timeout;
		long start = System.currentTimeMillis();
		while (true) {
			try {
				worker.join(timeLeft);
				break;
			} catch (InterruptedException ex) {
				long now = System.currentTimeMillis();
				long passed = now - start;
				timeLeft = timeout - passed;
				if (timeLeft <= 0) {
					break;
				}
			}
		}
		if (worker.exit != null) {
			return worker.exit;
		} else {
			System.out.println();
			System.out.println("Timeout expired, killing process");
			process.destroy();
			while (true) {
				try {
					worker.join();
					break;
				} catch (InterruptedException e) {
					// continue;
				}
			}
			throw new TimeoutException();
		}
	}

	private static class Worker extends Thread
	{
		private final Process process;
		private Integer exit;

		private Worker(Process process)
		{
			this.process = process;
		}

		@Override
		public void run()
		{
			BufferedReader b = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			while (true) {
				int c;
				try {
					c = b.read();
					if (c < 0) {
						System.out.println("Process outputstream closed");
						break;
					}
					System.out.print((char) c);
				} catch (IOException e) {
					System.out.println("IOException: " + e.getMessage());
					// ignore
				}
			}

			while (true) {
				try {
					System.out.println("Waiting for thread to die");
					exit = process.waitFor();
					System.out.println("Thread finished");
					return;
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

}
