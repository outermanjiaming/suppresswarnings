/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

public class TTL implements Comparable<TTL> {
	long ttl;
	String key;
	public TTL(){}
	public TTL(long ttl, String key) {
		this.ttl = ttl;
		this.key = key;
	}
	public long ttl(){return ttl;}
	public String key(){return key;}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TTL other = (TTL) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[" + key + " ("+ (marked() ? "######" : (ttl - System.currentTimeMillis())+"ms")+")]";
	}
	
	@Override
	public int compareTo(TTL o) {
		return Long.compare(this.ttl, o.ttl);
	}
	public void mark() {
		this.ttl = Long.MIN_VALUE;
	}
	public boolean marked(){
		return this.ttl == Long.MIN_VALUE;
	}
}
