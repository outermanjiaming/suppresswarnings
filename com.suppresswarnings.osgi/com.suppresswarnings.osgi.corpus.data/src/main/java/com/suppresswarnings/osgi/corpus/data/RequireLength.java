package com.suppresswarnings.osgi.corpus.data;

public class RequireLength extends RequireChain {
	public static final String desc = "长度int类型，取值范围[min, max], 闭区间";
	int min;
	int max;
	public RequireLength(){}
	public RequireLength(int min, int max) {
		this.min = min;
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
		return desc;
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
