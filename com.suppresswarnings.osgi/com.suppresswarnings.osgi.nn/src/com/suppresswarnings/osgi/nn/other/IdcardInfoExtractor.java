package com.suppresswarnings.osgi.nn.other;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
  
/** 
 * <p> 
 * 类说明:提取身份证相关信息 
 * </p> 
 */  
public class IdcardInfoExtractor {  
    // 城市  
    private String city;  
    // 区县  
    private String region;  
    // 年份  
    private int year;  
    // 月份  
    private int month;  
    // 日期  
    private int day;  
    // 性别  
    private String gender;  
    // 出生日期  
    private Date birthday;  
    private String sSX[] = { "猪", "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗" }; 
    private String sTG[] = { "癸", "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "任" };  
    private String sDZ[] = { "亥", "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌" };  
  
    /** 
     * @return the city 
     */  
    public String getCity() {  
        return city;  
    }  
  
    /** 
     * @return the region 
     */  
    public String getRegion() {  
        return region;  
    }  
  
    /** 
     * @return the year 
     */  
    public int getYear() {  
        return year;  
    }  
  
    /** 
     * @return the month 
     */  
    public int getMonth() {  
        return month;  
    }  
  
    /** 
     * @return the day 
     */  
    public int getDay() {  
        return day;  
    }  
  
    /** 
     * @return the gender 
     */  
    public String getGender() {  
        return gender;  
    }  
  
    /** 
     * @return the birthday 
     */  
    public Date getBirthday() {  
        return birthday;  
    }  
  
    @Override  
    public String toString() {
        return "家乡：" + this.city + ", 性别：" + this.gender + ", 属相：" + this.region + ", 生日："  + this.birthday;  
    }  
  
    public static void main(String[] args) throws Exception{
    	Class.forName("City");
    	System.out.println(cityCodeMap);
        String idcard = guess("43048119890313", 'm');
    	IdcardValidator valid = new IdcardValidator();
    	boolean x = valid.isValidate18Idcard(idcard);
    	System.out.println(x);
        IdcardInfoExtractor ie = new IdcardInfoExtractor(idcard);  
        System.out.println(ie.toString());
    }
    
    public static String guess(String former, char gender) {
    	char[] is1 = {'0','1','2','3','4','5','6','7','8','9'};
    	char[] is2 = {'0','1','2','3','4','5','6','7','8','9'};
    	char[] is3 = {'0','1','2','3','4','5','6','7','8','9'};
    	char[] cs = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    	IdcardValidator valid = new IdcardValidator();
    	int count = 0;
    	String idcard = null;
    	String result = null;
    	for(char a1 : is1)
    		for(char a2 : is2)
    			for(char a3 : is3)
    				for(char a4 : cs) {
    					idcard = append(former, a1, a2, a3, a4);
    					if(valid.isValidate18Idcard(idcard) && (gender == 'm' ^ Integer.parseInt(idcard.substring(16, 17)) % 2 == 0)) {
    						System.out.println(idcard);
    						count ++;
    						result = idcard;
    					}
    				}
    	System.out.println("got " + count);
    	return result;
    }
    
    public static String append(String former, char ... cs) {
    	StringBuffer sb = new StringBuffer(former);
    	for(char c : cs) sb.append(c);
		return sb.toString();
    }
    
    public static Map<String, String> cityCodeMap = null;
  
    private IdcardValidator validator = null;  
  
    /** 
     * 通过构造方法初始化各个成员属性 
     */  
    public IdcardInfoExtractor(String idcard) {  
        try {  
            validator = new IdcardValidator();  
            if (validator.isValidatedAllIdcard(idcard)) { 
                if (idcard.length() == 15) {  
                    idcard = validator.convertIdcarBy15bit(idcard);  
                }  
                // 获取省份  
                String provinceId = idcard.substring(0, 6);  
                this.city = cityCodeMap.get(provinceId);
                int year = Integer.valueOf(idcard.substring(6, 10));  
                int i = (year - 3) % 10;  
                int j = (year - 3) % 12;  
                int month = Integer.valueOf(idcard.substring(10, 12));  
                int day = Integer.valueOf(idcard.substring(12, 14)); 
                String strValue = "";  
                if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) {  
                    strValue = "水瓶座";  
                } else if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) {  
                    strValue = "双鱼座";  
                } else if ((month == 3 && day > 20) || (month == 4 && day <= 19)) {  
                    strValue = "白羊座";  
                } else if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) {  
                    strValue = "金牛座";  
                } else if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) {  
                    strValue = "双子座";  
                } else if ((month == 6 && day > 21) || (month == 7 && day <= 22)) {  
                    strValue = "巨蟹座";  
                } else if ((month == 7 && day > 22) || (month == 8 && day <= 22)) {  
                    strValue = "狮子座";  
                } else if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) {  
                    strValue = "处女座";  
                } else if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) {  
                    strValue = "天秤座";  
                } else if ((month == 10 && day > 23) || (month == 11 && day <= 22)) {  
                    strValue = "天蝎座";  
                } else if ((month == 11 && day > 22) || (month == 12 && day <= 21)) {  
                    strValue = "射手座";  
                } else if ((month == 12 && day > 21) || (month == 1 && day <= 19)) {  
                    strValue = "魔羯座";  
                }  

                this.region = sTG[i] + sDZ[j] + "-" + sSX[j] + "-" + strValue;  
                // 获取性别  
                String id17 = idcard.substring(16, 17);  
                this.gender = (Integer.parseInt(id17) % 2 != 0 ? "男":"女");  
  
                // 获取出生日期  
                String birthday = idcard.substring(6, 14);  
                Date birthdate = new SimpleDateFormat("yyyyMMdd").parse(birthday);  
                this.birthday = birthdate;  
                GregorianCalendar currentDay = new GregorianCalendar();  
                currentDay.setTime(birthdate);  
                this.year = currentDay.get(Calendar.YEAR);  
                this.month = currentDay.get(Calendar.MONTH) + 1;  
                this.day = currentDay.get(Calendar.DAY_OF_MONTH);  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }
    }
}