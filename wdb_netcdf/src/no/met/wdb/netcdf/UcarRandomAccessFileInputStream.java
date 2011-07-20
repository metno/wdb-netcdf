package no.met.wdb.netcdf;

import java.io.IOException;
import java.io.InputStream;

import ucar.unidata.io.RandomAccessFile;

class UcarRandomAccessFileInputStream extends InputStream {

	private RandomAccessFile raf;

	public UcarRandomAccessFileInputStream(RandomAccessFile raf, boolean resetFilePosition) throws IOException {
		this(raf);
		if ( resetFilePosition )
			raf.seek(0);
	}
	
	public UcarRandomAccessFileInputStream(RandomAccessFile raf) {
		this.raf = raf;
	}
	
	@Override
	public int read() throws IOException {
		return raf.read();
	}
}
