package com.ywd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件操作工具类
 * @author Administrator
 *
 */
public class FileCommons {
	
	public static String fileMD5(String inputFile) throws IOException {
			File f = new File(inputFile);
			return fileMD5(f);
	   }
	
	public static String fileMD5(File inputFile) throws IOException {
	      int bufferSize = 1024 * 1024;
	      FileInputStream fileInputStream = null;
	      DigestInputStream digestInputStream = null;
	      try {
	         MessageDigest messageDigest =MessageDigest.getInstance("MD5");
	         fileInputStream = new FileInputStream(inputFile);
	         digestInputStream = new DigestInputStream(fileInputStream,messageDigest);
	         byte[] buffer =new byte[bufferSize];
	         while (digestInputStream.read(buffer) > 0);
	         messageDigest= digestInputStream.getMessageDigest();
	         byte[] resultByteArray = messageDigest.digest();
	         return Encryptor.parseByte2HexStr(resultByteArray);
	      } catch (NoSuchAlgorithmException e) {
	         return null;
	      } finally {
	         try {
	            digestInputStream.close();
	            fileInputStream.close();
	         } catch (Exception e) {
	         }
	      }
	   }
	
	public static void main(String[] args) {
		String fileMD5 = null;
		long s = System.currentTimeMillis();
		try {
			fileMD5 = fileMD5("D:/test/cp/cuteinfo1.zip");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		long e = System.currentTimeMillis();
		System.out.println(fileMD5+" : "+(e-s));
	}
}
