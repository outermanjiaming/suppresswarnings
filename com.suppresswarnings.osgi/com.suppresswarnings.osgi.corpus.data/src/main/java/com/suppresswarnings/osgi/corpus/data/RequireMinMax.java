package com.suppresswarnings.osgi.corpus.data;

public class RequireMinMax extends RequireChain {
	public static final String desc = "int类型，取值范围[min, max], 闭区间";
	int min;
	int max;
	public RequireMinMax(){}
	public RequireMinMax(int min, int max) {
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
		int var = Integer.valueOf(value);
		if(var > max || var < min) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		try {
			System.out.println(Integer.valueOf("a"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
}
