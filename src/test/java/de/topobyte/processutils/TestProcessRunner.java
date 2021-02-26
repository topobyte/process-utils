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

import java.io.IOException;

public class TestProcessRunner
{

	public static void main(String[] args) throws IOException
	{
		Process process = Runtime.getRuntime()
				.exec(new String[] { "file", "/tmp" });
		ProcessRunner processExec = new ProcessRunner(process);
		int value = processExec.waitForEnd();
		System.out.println(String.format("return value: %d", value));
		System.out.println("stderr:");
		System.out.println(
				String.format("'%s'", new String(processExec.getStdErr())));
		System.out.println("stdout:");
		System.out.println(
				String.format("'%s'", new String(processExec.getStdOut())));
	}

}
