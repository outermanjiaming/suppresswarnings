package com.suppresswarnings.osgi.corpus;

public class RequireLength extends RequireChain {
	int min;
	int max;
	public RequireLength(){}
	public RequireLength(int min, int max) {
		this.min = min < 0 ? 0 : min;
		this.max = max;
	}
	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	@Override
	public String desc() {
		String minV = (min < 0) ? "(0": "[" + min;
		String maxV = (max == Integer.MAX_VALUE) ? "+∞)": max + "]";
		return "字符类型，长度范围"+minV+", "+maxV;
	}

	@Override
	public boolean agree(String value) {
		int var = value.length();
		if(var > max || var < min) {
			return false;
		}
		return true;
	}

}
