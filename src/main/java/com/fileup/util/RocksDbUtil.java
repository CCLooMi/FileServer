package com.fileup.util;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class RocksDbUtil {
	private static RocksDB rDb;
	static {
		try {
			rDb=RocksDB.open(Paths.getBaseUserDir("db"));
		} catch (RocksDBException e) {
			e.printStackTrace();
		}
	}
	public static void put(byte[]key,byte[]value) {
		try {
			rDb.put(key, value);
		} catch (RocksDBException e) {
		}
	}
	public static void put(String id,byte[]value) {
		put(BytesUtil.hexStringToBytes(id), value);
	}
	public static byte[] get(byte[]key) {
		try {
			return rDb.get(key);
		} catch (RocksDBException e) {
		}
		return null;
	}
	public static byte[] get(String id) {
		return get(BytesUtil.hexStringToBytes(id));
	}
	public static void delete(byte[]key) {
		try {
			rDb.delete(key);
		} catch (RocksDBException e) {
		}
	}
	public static void delete(String id) {
		delete(BytesUtil.hexStringToBytes(id));
	}
}
