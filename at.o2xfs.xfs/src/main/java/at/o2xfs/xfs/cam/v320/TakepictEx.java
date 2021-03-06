/*
 * Copyright (c) 2017, Andreas Fagschlunger. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package at.o2xfs.xfs.cam.v320;

import at.o2xfs.win32.LPSTR;
import at.o2xfs.win32.Pointer;
import at.o2xfs.xfs.cam.Camera;
import at.o2xfs.xfs.cam.Takepict;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class TakepictEx
		extends Takepict {

	private final LPSTR pictureFile = new LPSTR();

	private TakepictEx() {
		add(pictureFile);
	}

	public TakepictEx(Pointer p) {
		this();
		assignBuffer(p);
	}

	public TakepictEx(Camera aCamera, String aCamData, String aUnicodeCamData, String aPictureFile) {
		this();
		allocate();
		camera.set(aCamera);
		camData.set(aCamData);
		unicodeCamData.set(aUnicodeCamData);
		pictureFile.set(aPictureFile);
	}

	public TakepictEx(TakepictEx copy) {
		this(copy.getCamera(), copy.getCamData(), copy.getUnicodeCamData(), copy.getPictureFile());
	}

	public String getPictureFile() {
		return pictureFile.toString();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString())
										.append("pictureFile", getPictureFile())
										.toString();
	}
}