package com.suppresswarnings.osgi.leveldb;

import java.util.Arrays;
import java.util.Scanner;

import com.leveldb.common.Iterator;
import com.leveldb.common.Slice;
import com.leveldb.common.Status;
import com.leveldb.common.db.DB;
import com.leveldb.common.options.Options;
import com.leveldb.common.options.ReadOptions;
import com.leveldb.common.options.WriteOptions;

public class LiJiamingTest{

	public static void main(String[] args) {
		String dbname_ = "/Users/lijiaming/Codes/SuppressWarnings/SuppressWarnings/account";
		DB db_;
		WriteOptions woption = new WriteOptions();
		ReadOptions roption = new ReadOptions();
		Options options = new Options();
		options.create_if_missing = true;
		db_ = DB.Open(options, dbname_);
		
		
//		db_.Put(woption, new Slice("foo"), new Slice("world"));
//		db_.Put(woption, new Slice("foo"), new Slice("world1"));
//		db_.Put(woption, new Slice("foo2"), new Slice("world2"));
//		db_.Put(woption, new Slice("foo3"), new Slice("world3"));
//		
//		long start = System.currentTimeMillis();
//		int size = 100;
//		Random rand = new Random();
//		for(int i=0;i<size;i++) {
//			String key = Long.toHexString(rand.nextLong());
//			db_.Put(woption, new Slice(key), new Slice("value" + i));
//		}
//		long stop = System.currentTimeMillis();
//		long time = stop - start;
//		double second = time / 1000.0d;
//		System.out.println(String.format("====== write " +size+ " keys, cost=%s, speed=%s", time, (double)time/size));
//		System.out.println(String.format("====== write " +size+ " keys, second=%s, TPS=%s", second, (double)size/second));
//		
//		long begbin = System.currentTimeMillis();
//		for(int i=10;i<size;i++) {
//			Status status = new Status();
//			Slice ret = db_.Get(roption, new Slice("foo" + i), status);
//			System.out.print(status + " - " + ret + ",");
//		}
//		long end = System.currentTimeMillis();
//		long period = end - begbin;
//		second = period / 100.0d;
//		System.out.println(String.format("====== read " +size+ " keys, cost=%s, speed=%s", period, (double)period/size));
//		System.out.println(String.format("====== read " +size+ " keys, second=%s, TPS=%s", second, (double)size/second));
//		
//		try {
//			TimeUnit.SECONDS.sleep(1);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		Iterator itr = db_.NewIterator(new ReadOptions());
//		for(itr.Seek(new Slice("f5"));itr.Valid();itr.Next()) {
//			System.out.println(itr.key() + " = " + itr.value());
//		}
		System.out.println("go...");
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] commands = line.split("\\s+");
			System.out.println(Arrays.toString(commands));
			if(commands.length > 0) {
				String cmd = commands[0];
				if("get".equals(cmd)) {
					String key = commands[1];
					Status status = new Status();
					Slice ret = db_.Get(roption, new Slice(key), status);
					System.out.print(status + " Get " + key + " = " + ret.toString());
				} else if("put".equals(cmd)) {
					String key = commands[1];
					String value = commands[2];
					Status status = db_.Put(woption, new Slice(key), new Slice(value));
					System.out.print(status + " Put " + key + "="+value);
				} else if("list".equals(cmd)) {
					String head = commands[1];
					long limit = Long.parseLong(commands[2]);
					long count = 1;
					Iterator iterator = db_.NewIterator(new ReadOptions());
					for(iterator.Seek(new Slice(head));iterator.Valid();iterator.Next()) {
						System.out.println("[list] " + count + ": " +iterator.key() + " = " + iterator.value());
						if(count >= limit) {
							System.out.println("list exceeds limit, last key:" + iterator.key());
							break;
						}
						count ++;
					}
				} else if("exit".equals(cmd)) {
					break;
				}
			}
			
		}
		scanner.close();
		

		db_.Close();
		System.out.println("end");
	}
}
