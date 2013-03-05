package equestria.canterlot.lunasdreamwalk.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Various methods to manipulate byte arrays, hex and ASCII strings and convert
 * between them
 * 
 */
public class Util {

	/**
	 * Parse a string from bytes in a byte array. Beginning at the specified
	 * offset, 4 bytes are copied, their order is reversed and then they are
	 * combined into a single integer.
	 * 
	 * @param data
	 *            a byte array of arbitrary size
	 * @param offset
	 *            an offset within the array, with (offset <= data.length - 4)
	 * @return the generated integer number
	 * @throws ArrayIndexOutOfBoundsException
	 *             if (offset > data.length - 4)
	 */
	public static int parseInteger(byte[] data, int offset)
			throws ArrayIndexOutOfBoundsException {

		if (data.length < offset + 4)
			throw new ArrayIndexOutOfBoundsException(
					"Not enough bytes for Reading an Integer at " + offset);

		return (data[offset] & 0xFF) | (data[offset + 1] & 0xFF) << 8
				| (data[offset + 2] & 0xFF) << 16
				| (data[offset + 3] & 0xFF) << 24;
	}

	/**
	 * Convert an array of bytes into a new array of integers. The order of 4
	 * bytes each is reversed and then they are combined into a new integer. If
	 * data has a length that is not a multiple of 4, the remaining bytes are
	 * simply dropped.
	 * 
	 * @param data
	 *            a byte array of arbitrary size
	 * @return an integer array
	 */
	public static int[] bytesToInts(byte[] data) {

		int[] a = new int[data.length / 4];

		for (int i = 0; i < a.length; i++) {
			a[i] = (data[i * 4] & 0xFF) | ((data[i * 4 + 1] & 0xFF) << 8)
					| ((data[i * 4 + 2] & 0xFF) << 16)
					| ((data[i * 4 + 3] & 0xFF) << 24);
		}

		return a;
	}

	/**
	 * Convert an array of integers into a new array of bytes. The order of 4
	 * bytes each is reversed after splitting the integers into bytes.
	 * 
	 * @param data
	 *            an integer array of arbitrary size
	 * @return a byte array
	 */
	public static byte[] intsToBytes(int[] data) {
		byte[] b = new byte[data.length * 4];

		for (int i = 0; i < data.length; i++) {
			b[i * 4] = (byte) (data[i] & 0xFF);
			b[i * 4 + 1] = (byte) ((data[i] >> 8) & 0xFF);
			b[i * 4 + 2] = (byte) ((data[i] >> 16) & 0xFF);
			b[i * 4 + 3] = (byte) ((data[i] >> 24) & 0xFF);
		}

		return b;
	}

	/**
	 * An implementation of the XXTEA algorithm encrypt method. The input data
	 * and key are converted into integers with the bytesToInts method before
	 * the encryption and are converted back into byte arrays with the
	 * intsToBytes method after the encryption.
	 * 
	 * Therefore the data array should have a length of a multiple of 4.
	 * 
	 * @param data
	 *            a byte array of arbitrary size (but should be multiple of 4)
	 * @param key
	 *            a byte array of arbitrary size. if it is smaller than 16, the
	 *            rest will be filled with 0s, if it is bigger, only the first
	 *            16 bytes are used.
	 * @return the encrypted byte array of the same size as data, or smaller if
	 *         data had a length that is not a multiple of 4-
	 */
	public static byte[] encrypt(byte[] data, byte[] key) {
		if (data.length == 0) {
			return data;
		}

		int[] s = Util.bytesToInts(data);
		int[] g = Util.bytesToInts(Arrays.copyOfRange(key, 0, 16));

		int d = s.length;
		int j = s[d - 1], l = s[0];
		int o = 0x9E3779B9;

		int m, i, a = (int) Math.floor(6 + 52 / d);
		int h = 0;
		for (; a > 0; a--) {
			h += o;
			i = h >>> 2 & 3;
			for (int b = 0; b < d; b++) {
				l = s[(b + 1) % d];
				m = (j >>> 5 ^ l << 2) + (l >>> 3 ^ j << 4) ^ (h ^ l)
						+ (g[b & 3 ^ i] ^ j);
				j = s[b] += m;
			}
		}

		return Util.intsToBytes(s);
	}

	/**
	 * An implementation of the XXTEA algorithm decrypt method. The input data
	 * and key are converted into integers with the bytesToInts method before
	 * the decryption and are converted back into byte arrays with the
	 * intsToBytes method after the decryption.
	 * 
	 * Therefore the data array should have a length of a multiple of 4.
	 * 
	 * @param data
	 *            a byte array of arbitrary size (but should be multiple of 4)
	 * @param key
	 *            a byte array of arbitrary size. if it is smaller than 16, the
	 *            rest will be filled with 0s, if it is bigger, only the first
	 *            16 bytes are used.
	 * @return the decrypted byte array of the same size as data, or smaller if
	 *         data had a length that is not a multiple of 4-
	 */
	public static byte[] decrypt(byte[] data, byte[] key) {

		if (data.length == 0) {
			return data;
		}

		int[] s = Util.bytesToInts(data);
		int[] g = Util.bytesToInts(Arrays.copyOfRange(key, 0, 16));

		int d = s.length;
		int j = s[d - 1], l = s[0];
		int o = 0x9E3779B9;

		int m, i, a = (int) Math.floor(6 + 52 / d);
		int h = a * o;

		boolean printed = false;
		while (h != 0) {
			i = (h >>> 2 & 3);
			for (int c = d - 1; c >= 0; c--) {
				j = s[c > 0 ? c - 1 : d - 1];
				m = (j >>> 5 ^ l << 2) + (l >>> 3 ^ j << 4) ^ (h ^ l)
						+ (g[(c & 3) ^ i] ^ j);
				if (!printed) {
					printed = true;
				}
				l = s[c] -= m;
			}
			h -= o;
		}

		return Util.intsToBytes(s);
	}

	/**
	 * Write an integer into a specified offset of an existing byte array. The
	 * bytes order gets reversed.
	 * 
	 * @param data
	 *            the target byte array
	 * @param offset
	 *            the target offset within the byte array
	 * @param value
	 *            the integer value that should be written
	 * @throws ArrayIndexOutOfBoundsException
	 *             if (offset > data.length - 4)
	 */
	public static void writeInteger(byte[] data, int offset, int value)
			throws ArrayIndexOutOfBoundsException {
		if (data.length < offset + 4) {
			throw new ArrayIndexOutOfBoundsException(
					"Not enough space for writing an Integer at " + offset);
		}

		data[offset] = (byte) ((value) & 0xFF);
		data[offset + 1] = (byte) ((value >>> 8) & 0xFF);
		data[offset + 2] = (byte) ((value >>> 16) & 0xFF);
		data[offset + 3] = (byte) ((value >>> 24) & 0xFF);
	}

	/**
	 * Convert a hex string like "AF307E" (without leadin "0x") into a byte
	 * array. If the hex string has odd length, the last digit/character is
	 * ignored
	 * 
	 * @param hexString
	 *            the hex string
	 * @return the byte array
	 */
	public static byte[] hexStringToByteArray(String hexString) {
		byte[] target = new byte[hexString.length() / 2];

		for (int i = 0; i < target.length; i++) {
			target[i] = (byte) Integer.parseInt(
					hexString.substring(i * 2, i * 2 + 2), 16);

		}
		return target;
	}

	/**
	 * Compress a byte array with the zlib "DEFAULT_COMPRESSION" algorithm
	 * 
	 * @param data
	 *            the to be compressed data
	 * @return the compressed data
	 * @throws IOException
	 * @throws DataFormatException
	 */
	public static byte[] compress(byte[] data) throws IOException,
			DataFormatException {
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
		deflater.setInput(data);
		deflater.finish();

		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

		byte[] buffer = new byte[1024];

		while (!deflater.finished()) {
			int bytesCompressed = deflater.deflate(buffer);
			bos.write(buffer, 0, bytesCompressed);
		}

		bos.close();

		return bos.toByteArray();
	}

	/**
	 * Decompress a byte array that got compressed with zlib.
	 * 
	 * @param data
	 *            the compressed data
	 * @return the uncompressed data
	 * @throws IOException
	 * @throws DataFormatException
	 */
	public static byte[] decompress(byte[] data) throws IOException,
			DataFormatException {
		Inflater inflater = new Inflater();
		inflater.setInput(data);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				data.length);
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();

		inflater.end();

		return output;
	}

	/**
	 * Calculate the md5 hash value of a byte array
	 * 
	 * @param data
	 *            the data
	 * @return the calculated hash as a 16 byte array
	 */
	public static byte[] md5(byte[] data) {
		byte[] md5;
		// Calculate MD5
		try {
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			md5Digest.update(data, 0, data.length);
			md5 = md5Digest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Couldn't generate MD5, not supported!");
		}

		return md5;
	}

	/**
	 * Convert a byte array into a hexadecimal String (without leading "0x")
	 * 
	 * @param data
	 *            the data
	 * @return the hex string
	 */
	public static String byteArrayToHexString(byte[] data) {

		StringBuilder b = new StringBuilder("");

		if (data.length == 0) {
			return b.toString();
		}

		for (int i = 0; i < data.length; i++) {
			// This is not a bug, the original also produces "broken" MD5
			// values in this location (broken in case data[i] < 10)
			b.append(String.format("%02X", data[i]));
		}

		return b.toString();
	}

	/**
	 * Convert the 2 byte per character Java Strings into a byte array by
	 * chopping off the first byte of each character.
	 * 
	 * @param text
	 *            The input string
	 * @return the byte array
	 */
	public static byte[] stringToASCIIByteArray(String text) {
		byte[] result = new byte[text.length()];

		char[] chars = text.toCharArray();

		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (chars[i] & 0xFF);
		}

		return result;
	}

	/**
	 * Convert a byte array into a 2 byte per character Java String by adding a
	 * 0-byte in front of each byte.
	 * 
	 * @param data
	 *            the input byte array
	 * @return the Java String
	 */
	public static String ASCIIByteArrayToString(byte[] data) {
		StringBuilder r = new StringBuilder("");

		for (byte b : data) {
			r.append((char) (b & 0xFF));
		}

		return r.toString();
	}

	/**
	 * Change the order of the bytes in a byte array, by taking 4 bytes each and
	 * reversing their locations.
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] reorderBytes(byte[] data) {
		byte[] a = new byte[data.length];

		for (int i = 0; i < a.length; i += 4) {
			a[i] = data[i + 3];
			a[i + 1] = data[i + 2];
			a[i + 2] = data[i + 1];
			a[i + 3] = data[i];
		}

		return a;
	}

	/**
	 * Calculate a CRC32 checksum of a byte array
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] CRC32(byte[] data) {
		java.util.zip.CRC32 x = new java.util.zip.CRC32();
		x.update(data);

		return ByteBuffer.allocate(4).putInt((int)x.getValue()).array();
	}

}
