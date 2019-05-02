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

public class KeyValue {
	String key;
	String value;
	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}
	public KeyValue key(String newKey) {
		this.key = newKey;
		return this;
	}
	public KeyValue value(String newValue) {
		this.value = newValue;
		return this;
	}
	
	public String key() {
		return key;
	}

	public String value() {
		return value;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		KeyValue other = (KeyValue) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return key + "=" + value;
	}
}