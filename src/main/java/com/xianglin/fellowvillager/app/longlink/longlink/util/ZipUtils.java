package com.xianglin.fellowvillager.app.longlink.longlink.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtils {

	/**
	 * Gzip压缩bytes
	 * 
	 * @param data
	 *            bytes to be Gziped
	 * @return byte[]
	 */
	public static byte[] GZipBytes(byte[] data) {
		if (data == null || data.length <= 0) {
			return null;
		}

		byte[] result = null;

		ByteArrayOutputStream bytesZiped = new ByteArrayOutputStream();
		GZIPOutputStream out = null;
		try {
			out = new GZIPOutputStream(bytesZiped);

			out.write(data, 0, data.length);
			out.finish();
			// out.flush(); // Throws exception on Android KitKat
			
			result = bytesZiped.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (bytesZiped != null)
					bytesZiped.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Gzip解压缩byte[]
	 * 
	 * @param data
	 *            byte[] to be unGziped
	 * @return byte[]
	 */
	public static byte[] UnGZipBytes(byte[] data) {
		if (data == null || data.length <= 0) {
			return null;
		}

		byte[] result = null;

		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
		GZIPInputStream gZipIn = null;
		
		try {
			gZipIn = new GZIPInputStream(bytesIn);
			
			byte[] buffer = new byte[512];
			int n;
			while ((n = gZipIn.read(buffer)) >= 0) {
				bytesOut.write(buffer, 0, n);
			}
			
			bytesOut.flush();
			result = bytesOut.toByteArray();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			try {
				if (gZipIn != null)
					gZipIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (bytesOut != null)
					bytesOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}


	/**
	 * Gzip解压缩byte[]
	 *
	 * @param data
	 *            byte[] to be unGziped
	 * @return byte[]
	 */
	public static byte[] UnGZipByte(byte[] data) {
		if (data == null || data.length <= 0) {
			return null;
		}

		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gzip = new GZIPInputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;

	}
}
