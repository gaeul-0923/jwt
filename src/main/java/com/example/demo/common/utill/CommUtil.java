package com.example.demo.common.utill;


import com.example.demo.api.auth.CustomUserDetails;
import com.example.demo.api.auth.LoginVO;
import com.example.demo.common.response.UploadFileVO;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 공통 유틸
 * @author Jayden
 * @since 2021.12.17
 */
@Log4j2
@Component(value = "CommUtil")
public class CommUtil {

	private static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	private static final String YYYYMMDD = "yyyyMMdd";
	private static final String KOREA = "KOREA";
	private static final String KOREAN = "KOREAN";
	private static final String UTF = "UTF-8";

	/**
	 * 휴대전화 마스킹 규칙
	 * - 가운데 네자리를 암호화
	 * @since 2021-12-11
	 * @author Jayden
	 */
	public static String telMasking(String tel) {

		if (CommUtil.isEmpty(tel)) {
			return tel;
		}

		String replaceString = tel;
		String pattern = "^(\\d{3})-?(\\d{3,4})-?(\\d{4})$";
		Matcher matcher = Pattern.compile(pattern).matcher(tel);

		if(matcher.matches()) {
			replaceString = "";

			boolean isHyphen = false;
			if(tel.indexOf("-") > -1) {
				isHyphen = true;
			}

			for(int i=1;i<=matcher.groupCount();i++) {
				String replaceTarget = matcher.group(i);
				if(i == 3) {
					char[] c = new char[replaceTarget.length()];
					Arrays.fill(c, '*');

					replaceString = replaceString + String.valueOf(c);
				} else {
					replaceString = replaceString + replaceTarget;
				}

				if(isHyphen && i < matcher.groupCount()) {
					replaceString = replaceString + "-";
				}
			}
		}
		return replaceString;
	}

	/**
	 * 이름 마스킹 규칙
	 * - 양끝 한 자리를 제외 가운데를 마스킹
	 * @since 2021-12-11
	 * @author Jayden
	 */
	public static String nameMasking(String name) {

		if(CommUtil.isEmpty(name)) {
			return name;
		}

		if(name.length() == 2) {
			return name.substring(0, 1) + "*";
		}

		String format = name.replaceAll("(?<=.{1})." , "*");
		name = format.substring(0, format.length()-1) + name.substring(name.length()-1, name.length());
		return name;
	}

	/**
	 * 이메일 마스킹 규칙
	 * - @앞4자리를 마스킹한다
	 * - 그러나 이메일이 5자 이하일 경우 최소 뒤에 두 자리는 알아볼 수 있도록 남긴다.
	 * @since 2021-12-15
	 * @author Jayden
	 */
	public static String emailMasking(String email) {

		if (CommUtil.isEmpty(email)) {
			return email;
		}
		String p = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*[.][a-zA-Z]{2,3}$";
		String front = "";
		String back = "";
		if(Pattern.matches(p, email)) {
			front = email.split("@")[0];
			back = email.split("@")[1];

			if(front.length() > 5) {
				front = front.substring(0, front.length() - 4) + "****";
			}else {
				String format = "";
				for (int i = 0; i < front.length(); i++) {

					if(i > front.length()-3) {
						format += "*";
					}else {
						format += front.charAt(i);
					}
				}
				front = format;
			}
			email = front + "@" + back;
		}
		return email;
	}

	/**
	 * 카드번호 마스킹 규칙
	 * - 가운데 8자리 마스킹
	 * @since 2022-01-05
	 * @author Jayden
	 */
	public static String cardMasking(String cardNo) {
		if (CommUtil.isEmpty(cardNo)) {
			return cardNo;
		}
		String regex = "(\\d{4})-?(\\d{4})-?(\\d{4})-?(\\d{3,4})$";
		Matcher matcher = Pattern.compile(regex).matcher(cardNo);
		if (matcher.find()) {
			String target = matcher.group(2) + matcher.group(3);
			int length = target.length();
			char[] c = new char[length];
			Arrays.fill(c, '*');
			return cardNo.replace(target, String.valueOf(c));
		}
		return cardNo;

	}

	/**
	 * 주민번호마스킹
	 * 생년월일 + 한자리이후는 마스킹처리
	 * @since 2022-01-05
	 * @author Jayden
	 */
	public static String pernoMasking(String perno) {
		String regex = "(.{6}$)";
		if (perno == null || "".equals(perno) || perno.length() < 6) {
			return perno;
		} else {
			perno = perno.substring(0, 6) + "-" + perno.substring(6);
		}
		return perno.replaceAll(regex, "******");
	}

	/**
	 * 이름 마스킹 규칙
	 * - 양끝 한 자리를 제외 가운데를 마스킹
	 * @since 2021-12-11
	 * @author Jayden
	 */
	/**
	 * 계좌번호 마스킹
	 * 끝 5자리 마스킹
	 * @author JJM
	 * @since 2022.03.25
	 * @param bank_account
	 * @return
	 */
	public static String bankAccountMasking(String bank_account) {

		if(CommUtil.isEmpty(bank_account) ||  bank_account.length() < 6) {
			return bank_account;
		}
		String regex = "(.{5}$)";

		return bank_account.replaceFirst(regex, "*****");
	}

	/**
	 * 계좌번호 마스킹
	 * 뒷번호 5자리 마스킹 처리
	 * @since 2022-03-25
	 * @author Jayden
	 */
	public static String accnoMasking(String accno) {
		String regex = "(.{5}$)";
		if (accno == null || "".equals(accno) || accno.length() < 5) {
			return accno;
		}
		return accno.replaceAll(regex, "*****");
	}

	/**
	 * 주민번호포맷
	 * @since 2022-01-05
	 * @author Jayden
	 */
	public static String pernoFormat(String perno) {
		if (perno == null || "".equals(perno) || perno.length() < 6) {
			return perno;
		} else {
			perno = perno.substring(0, 6) + "-" + perno.substring(6);
		}
		return perno;
	}

	/**
	 * 계좌번호마스킹
	 * 뒷번호 5자리 마스킹
	 * @since 2022-03-24
	 * @author hazle
	 */
	public static String accountMasking(String account) {
		String regex = "(.{5}$)";

		return account.replaceAll(regex, "*****");
	}

	// 3XXX XXXXXX XXXXX
	public static void main(String[] args) {
		System.out.println(cardMasking("1111222333334444"));
	}


	/**
	 * 지난달을 구한다 yyyyMM
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static String getPassMonth() {
		Calendar cal = Calendar.getInstance(Locale.FRANCE);
		String now_yyyymmdd = CommUtil.getCurrentDateYYYYMMDD();
		int new_yy = Integer.parseInt(now_yyyymmdd.substring(0, 4));
		int new_mm = Integer.parseInt(now_yyyymmdd.substring(5, 7));
		int new_dd = Integer.parseInt(now_yyyymmdd.substring(8, 10));

		cal.set(new_yy, new_mm - 1, new_dd);
		cal.add(Calendar.MONTH, -1);

		return getYyyymmdd(cal).substring(0, 6);
	}

	/**
	 * 파라미터 확인
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	@SuppressWarnings("unchecked")
	public static void viewParams(HttpServletRequest request) {
		Enumeration<String> paramsss = request.getParameterNames();

		System.out.println("---------- requests @ option ----------");

		System.out.println("request_uri >> " + request.getRequestURI());
		System.out.println("client_ip >> " + CommUtil.getClientIp(request));
		System.out.println("client_os >> " + CommUtil.getOs(request));

		System.out.println("---------- requests @ param ----------");
		while (paramsss.hasMoreElements()) {
			String name = (String) paramsss.nextElement();
			System.out.println(name + "=" + request.getParameter(name));
		}
		System.out.println("--------------------------------------------");
	}

	@SuppressWarnings("unchecked")
	public static void viewParam(HttpServletRequest request) {
		Enumeration<String> paramsss = request.getParameterNames();

		System.out.println("---------- requests @ option ----------");

		System.out.println("request_uri >> " + request.getRequestURI());
		System.out.println("client_ip >> " + CommUtil.getClientIp(request));
		System.out.println("client_os >> " + CommUtil.getOs(request));

		System.out.println("---------- requests @ param ----------");
		while (paramsss.hasMoreElements()) {
			String name = (String) paramsss.nextElement();
			String[] values = request.getParameterValues(name);
			String value= request.getParameter(name);
			if (values !=null) {
				for (String s : values) {
					System.out.println(name + " values =" + s);
				}
			}
			else {
				System.out.println(name + " values =" + value);
			}
		}
		System.out.println("--------------------------------------------");
	}
	/**
	 * 두 시간에 대한 차리를 분 단위로 계산한다.
	 *
	 * @param startDate yyyyMMddHHmmss
	 * @param endDAte   yyyyMMddHHmmss
	 * @return 차이 분
	 */
	public static long getDifferSec(String startDate, String endDAte) {

		try {
			Date frDate = new SimpleDateFormat(YYYYMMDDHHMMSS).parse(startDate);
			Date toDate = new SimpleDateFormat(YYYYMMDDHHMMSS).parse(endDAte);

			long diffMil = toDate.getTime() - frDate.getTime();
			long diffSec = diffMil / 1000;
			return diffSec;

		} catch (ParseException e) {

			return -1;
		}

	}

	/**
	 * @param start millisec
	 * @param end   millisec
	 * @return 차이 시/분/초/밀리초
	 */
	public static String getDifferTime(Long start, Long end) {

		try {
			Long diffMil = end - start;
			Long diffSec = diffMil / 1000;

			String hour = String.valueOf(diffSec / 3600);
			String min = String.valueOf((diffSec % 3600) / 60);
			String sec = String.valueOf(diffSec % 60);
			String millis = String.valueOf(diffMil % 60);
			String time = "";
			if (!"0".equals(hour)) {
				time += hour + "h ";
			}
			if (!"0".equals(min)) {
				time += min + "m ";
			}
			if (!"0".equals(min)) {
				time += sec + "s ";
			}
			if (!"0".equals(millis)) {
				time += millis + "ms";
			}
			return time;

		} catch (Exception e) {
			return e.getMessage();
		}

	}

	/**
	 * 접속환경 확인(웹/모바일)
	 *
	 * @return boolean
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static boolean isMobile(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		boolean mobile1 = userAgent.matches(
			".*(iPhone|iPod|iPad|Android|Windows CE|BlackBerry|Symbian|Windows Phone|webOS|Opera Mini|Opera Mobi|POLARIS|IEMobile|lgtelecom|nokia|SonyEricsson).*");
		boolean mobile2 = userAgent.matches(".*(LG|SAMSUNG|Samsung).*");
		if (mobile1 || mobile2) {
			return true;
		}
		return false;
	}

	/**
	 * 휴대폰번호양식에 맞는지 확인
	 *
	 * @param str
	 * @return boolean
	 */
	public static boolean isCellphone(String str) {
		//010, 011, 016, 017, 018, 019
		return str.matches("(01[016789])\\d{7,8}");
	}

	/**
	 * 이메일 양식에 맞는지 확인
	 *
	 * @param str
	 * @return boolean
	 */
	public static boolean isEmail(String str) {
		//aaa@bbb.com
		return str.matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$");
	}

	/**
	 * 특정자리수만큼 난수 발생
	 * @author Jayden
	 * @since 2021.12.17
	 **/
	public static String generateNumber(int length) {
		String numStr = "1";
		String plusNumStr = "1";

		for (int i = 0; i < length; i++) {
			numStr += "0";
			if (i != length - 1) {
				plusNumStr += "0";
			}
		}
		long seed = System.currentTimeMillis();
		Random random = new Random(seed);
		int result = random.nextInt(Integer.parseInt(numStr)) + Integer.parseInt(plusNumStr);
		if (result > Integer.parseInt(numStr)) {
			result = result - Integer.parseInt(plusNumStr);
		}
		return "" + result;
	}

	/**
	 * 널값 "" 로 변경
	 * @author Jayden
	 * @since 2021.12.17
	 **/
	public static String nullToEmpty(String str) {
		return str == null ? "" : str;
	}

	/**
	 * 현재 일시에 해당하는 Calendar 객체를 반환함.
	 * @return 결과 calendar객체
	 */
	public static Calendar getCalendarInstance() {
		Calendar retCal = Calendar.getInstance();
		return retCal;
	}


	/**
	 * 입력한 년, 월, 일에 해당하는 Calendar 객체를 반환함.
	 * @param year  년
	 * @param month 월
	 * @param date  일
	 * @return 결과 calendar객체
	 */
	public static Calendar getCalendarInstance(int year, int month, int date) {
		Calendar retCal = Calendar.getInstance();
		month--;

		retCal.set(year, month, date);

		return retCal;
	}

	/**
	 * 입력한 년, 월, 일, 시, 분, 초에 해당하는 Calendar 객체를 반환함.
	 * @param year   년
	 * @param month  월
	 * @param date   일
	 * @param hour   시
	 * @param minute 분
	 * @param second 초
	 * @return 결과 calendar객체
	 */
	public static Calendar getCalendarInstance(int year, int month, int date,
		int hour, int minute, int second) {
		Calendar retCal = Calendar.getInstance();
		month--;

		retCal.set(year, month, date, hour, minute, second);

		return retCal;
	}

	/**
	 * calendar에 해당하는 일자를 type의 날짜형식으로 반환
	 * @param cal  calender객체
	 * @param type 변환타입
	 * @return 변환된 문자열
	 */
	public static String getDateFormat(Calendar cal, String type) {
		SimpleDateFormat dfmt = new SimpleDateFormat(type);
		return dfmt.format(cal.getTime());
	}

	/**
	 * 현재 일자를 입력된 type의 날짜로 반환
	 * @param type 날짜타입
	 * @return 결과 문자열
	 */
	public static String getDateFormat(String type) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(type, Locale.KOREA);
		return sdf.format(date);
	}

	/**
	 * 현재 일자를 입력된 type의 날짜로 반환
	 * @param type 날짜타입
	 * @return 결과 문자열
	 */
	public static Date getStringToDate(String type, String dateString) throws Exception {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(type);
		date = sdf.parse(dateString);
		return date;
	}

	/**
	 * 현재 일자를 입력된 type의 날짜로 반환
	 * @param type 날짜타입
	 * @return 결과 문자열
	 */
	public static String getDateToString(String type, Date date) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(type, Locale.KOREA);
		return sdf.format(date);
	}

	/**
	 * Calender에 해당하는 날짜와 시각을 yyyyMMdd 형태로 변환
	 * @param cal Calender객체
	 * @return 결과 일자
	 */
	public static String getYyyymmdd(Calendar cal) {
		Locale currentLocale = new Locale(KOREAN, KOREA);
		String pattern = YYYYMMDD;
		SimpleDateFormat formatter = new SimpleDateFormat(pattern,
			currentLocale);
		return formatter.format(cal.getTime());
	}

	/**
	 * 현재 날짜와 시각을 yyyyMMddhhmmss 형태로 변환
	 * @return 현재 년월일시분초
	 */
	public static String getCurrentDateTime() {
		System.setProperty("user.timezone", "Asia/Seoul");
		Date today = new Date();
		Locale currentLocale = new Locale(KOREAN, KOREA);
		String pattern = YYYYMMDDHHMMSS;
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, currentLocale);
		return formatter.format(today);
	}

	/**
	 * 현재 시각을 hhmmss 형태로 변환
	 * @return 현재 시분초
	 */
	public static String getCurrentTime() {
		Date today = new Date();
		Locale currentLocale = new Locale(KOREAN, KOREA);
		String pattern = "HHmmss";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern,
			currentLocale);
		return formatter.format(today);

	}

	/**
	 * 현재 날짜를 yyyyMMdd 형태로 변환
	 * @return 현재 년월일
	 */
	public static String getCurrentDate() {
		return getCurrentDateTime().substring(0, 8);
	}

	/**
	 * 현재 날짜를 yyyy-MM-dd 형태로 변환
	 * @return 현재 년월일
	 */
	public static String getCurrentDateYYYYMMDD() {
		return getCurrentDateTime().substring(0, 4) + "-" + getCurrentDateTime().substring(4, 6)
			+ "-" + getCurrentDateTime().substring(6, 8);
	}

	public static String getCurrentDateYYYYMMDDHHMISS() {
		return getCurrentDateTime().substring(0, 4) + "-" + getCurrentDateTime().substring(4, 6)
				+ "-" + getCurrentDateTime().substring(6, 8)
				+ " " + getCurrentDateTime().substring(8, 10)
				+ ":" + getCurrentDateTime().substring(10, 12)
				+ ":" + getCurrentDateTime().substring(12, 14);
	}

	public static String getCurrentDateYYYY() {
		return getCurrentDateTime().substring(0, 4);
	}
	public static String getCurrentDateMM() {
		return getCurrentDateTime().substring(4, 6);
	}
	public static String getCurrentDateDD() {
		return getCurrentDateTime().substring(6, 8);
	}

	/**
	 * 입력된 일자를 더한 날짜를 yyyyMMdd 형태로 변환
	 * @param yyyymmdd 기준일자
	 * @param addDay   추가일
	 * @return 연산된 일자
	 * @see Calendar
	 */
	public static String getDate(String yyyymmdd, int addDay) {
		Calendar cal = Calendar.getInstance(Locale.FRANCE);
		int new_yy = Integer.parseInt(yyyymmdd.substring(0, 4));
		int new_mm = Integer.parseInt(yyyymmdd.substring(4, 6));
		int new_dd = Integer.parseInt(yyyymmdd.substring(6, 8));

		cal.set(new_yy, new_mm - 1, new_dd);
		cal.add(Calendar.DATE, addDay);

		SimpleDateFormat sdf = new SimpleDateFormat(YYYYMMDD);

		return sdf.format(cal.getTime());
	}

	/**
	 * 년월주로 일자를 구하는 메소드.
	 * @param yyyymm  년월
	 * @param week    몇번째 주
	 * @param pattern 리턴되는 날짜패턴 (ex:yyyyMMdd)
	 * @return 연산된 날짜
	 * @see Calendar
	 */
	public static String getWeekToDay(String yyyymm, int week, String pattern) {

		Calendar cal = Calendar.getInstance(Locale.FRANCE);

		int new_yy = Integer.parseInt(yyyymm.substring(0, 4));
		int new_mm = Integer.parseInt(yyyymm.substring(4, 6));
		int new_dd = 1;

		cal.set(new_yy, new_mm - 1, new_dd);

		// 임시 코드
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			week = week - 1;
		}

		cal.add(Calendar.DATE, (week - 1) * 7
			+ (cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK)));

		SimpleDateFormat formatter = new SimpleDateFormat(pattern,
			Locale.FRANCE);

		return formatter.format(cal.getTime());

	}

	/**
	 * 입력된 일자를 더한 주를 구하여 return한다
	 * @param yyyymmdd 년도별
	 * @param addDay   추가일
	 * @return 연산된 주
	 * @see Calendar
	 */
	public static int getWeek(String yyyymmdd, int addDay) {
		Calendar cal = Calendar.getInstance(Locale.FRANCE);
		int new_yy = Integer.parseInt(yyyymmdd.substring(0, 4));
		int new_mm = Integer.parseInt(yyyymmdd.substring(4, 6));
		int new_dd = Integer.parseInt(yyyymmdd.substring(6, 8));

		cal.set(new_yy, new_mm - 1, new_dd);
		cal.add(Calendar.DATE, addDay);

		int week = cal.get(Calendar.DAY_OF_WEEK);
		return week;
	}

	/**
	 * 입력된 년월의 마지막 일수를 return
	 * @param year  년
	 * @param month 월
	 * @return 마지막 일수
	 * @see Calendar
	 */
	public static int getLastDayOfMon(int year, int month) {

		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 1);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);

	}// :

	/**
	 * 입력된 년월의 마지막 일수를 return
	 * @param yyyymm 년월
	 * @return 마지막 일수
	 */
	public static int getLastDayOfMon(String yyyymm) {

		Calendar cal = Calendar.getInstance();
		int yyyy = Integer.parseInt(yyyymm.substring(0, 4));
		int mm = Integer.parseInt(yyyymm.substring(4)) - 1;

		cal.set(yyyy, mm, 1);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 입력된 날자가 올바른지 확인
	 * @param yyyymmdd
	 * @return boolean
	 */
	public static boolean isCorrect(String yyyymmdd) {
		boolean flag = false;
		if (yyyymmdd.length() < 8) {
			return false;
		}
		try {
			int yyyy = Integer.parseInt(yyyymmdd.substring(0, 4));
			int mm = Integer.parseInt(yyyymmdd.substring(4, 6));
			int dd = Integer.parseInt(yyyymmdd.substring(6));
			flag = CommUtil.isCorrect(yyyy, mm, dd);
		} catch (Exception ex) {
			return false;
		}
		return flag;
	}

	/**
	 * 입력된 날자가 올바른 날자인지 확인
	 * @param yyyy
	 * @param mm
	 * @param dd
	 * @return boolean
	 */
	public static boolean isCorrect(int yyyy, int mm, int dd) {
		if (yyyy < 0 || mm < 0 || dd < 0) {
			return false;
		}
		if (mm > 12 || dd > 31) {
			return false;
		}

		String year = "" + yyyy;
		String month = "00" + mm;
		String year_str = year + month.substring(month.length() - 2);
		int endday = CommUtil.getLastDayOfMon(year_str);

		if (dd > endday) {
			return false;
		}

		return true;

	}//:

	/**
	 * 현재의 요일을 구한다.
	 * @return 요일
	 * @see Calendar
	 */
	public static int getDayOfWeek() {
		Calendar rightNow = Calendar.getInstance();
		int day_of_week = rightNow.get(Calendar.DAY_OF_WEEK);
		return day_of_week;
	}//:


	/**
	 * 입력받은 날짜의 요일을 반환한다.
	 * @param yyyymmdd
	 * @return
	 */
	public static int getDayOfWeek(String yyyymmdd) {
		Calendar cal = Calendar.getInstance(Locale.KOREA);
		int new_yy = Integer.parseInt(yyyymmdd.substring(0, 4));
		int new_mm = Integer.parseInt(yyyymmdd.substring(4, 6));
		int new_dd = Integer.parseInt(yyyymmdd.substring(6, 8));

		cal.set(new_yy, new_mm - 1, new_dd);

		int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
		return day_of_week;
	}//:


	/**
	 * 현재주가 올해 전체의 몇째주에 해당되는지 계산
	 * @return 주
	 * @see Calendar
	 */
	public static int getWeekOfYear() {
		Locale LOCALE_COUNTRY = Locale.KOREA;
		Calendar rightNow = Calendar.getInstance(LOCALE_COUNTRY);
		int week_of_year = rightNow.get(Calendar.WEEK_OF_YEAR);
		return week_of_year;
	}//:

	/**
	 * 입력받은 yyyymmdd 가 전체의 몇주에 해당되는지 계산
	 * @param yyyymmdd
	 * @return 주
	 */
	public static int getWeekOfYear(String yyyymmdd) {
		Calendar cal = Calendar.getInstance(Locale.KOREA);
		int new_yy = Integer.parseInt(yyyymmdd.substring(0, 4));
		int new_mm = Integer.parseInt(yyyymmdd.substring(4, 6));
		int new_dd = Integer.parseInt(yyyymmdd.substring(6, 8));

		cal.set(new_yy, new_mm - 1, new_dd);

		int week = cal.get(Calendar.WEEK_OF_YEAR);
		return week;
	}//:


	/**
	 * 현재주가 현재월에 몇째주에 해당되는지 계산
	 * @return 주
	 * @see Calendar
	 */
	public static int getWeekOfMonth() {
		Locale LOCALE_COUNTRY = Locale.KOREA;
		Calendar rightNow = Calendar.getInstance(LOCALE_COUNTRY);
		int week_of_month = rightNow.get(Calendar.WEEK_OF_MONTH);
		return week_of_month;
	}//:


	/**
	 * 입력받은 yyyymmdd 해당월에 몇째주에 해당되는지 계산
	 * @return 주
	 * @see Calendar
	 */
	public static int getWeekOfMonth(String yyyymmdd) {

		Calendar cal = Calendar.getInstance(Locale.KOREA);
		int new_yy = Integer.parseInt(yyyymmdd.substring(0, 4));
		int new_mm = Integer.parseInt(yyyymmdd.substring(4, 6));
		int new_dd = Integer.parseInt(yyyymmdd.substring(6, 8));

		cal.set(new_yy, new_mm - 1, new_dd);

		int week = cal.get(Calendar.WEEK_OF_MONTH);
		return week;

	}//:


	/**
	 * 두 날짜간의 날짜수를 반환(윤년을 감안함)
	 * @param startDate 시작 날짜
	 * @param endDate   끝 날짜
	 * @return 날수
	 * @see GregorianCalendar
	 */
	public static long getDifferDays(String startDate, String endDate) {
		GregorianCalendar StartDate = getGregorianCalendar(startDate);
		GregorianCalendar EndDate = getGregorianCalendar(endDate);
		long difer = (EndDate.getTime().getTime() - StartDate.getTime()
			.getTime()) / 86400000;
		return difer;
	}//:


	/**
	 * 두 시간에 대한 차리를 분 단위로 계산
	 * @param startDate yyyyMMddHHmmss
	 * @param endDAte   yyyyMMddHHmmss
	 * @return 차이 분
	 */
	public static long getDifferMin(String startDate, String endDAte) {

		try {
			Date frDate = new SimpleDateFormat(YYYYMMDDHHMMSS).parse(startDate);
			Date toDate = new SimpleDateFormat(YYYYMMDDHHMMSS).parse(endDAte);

			long diffMil = toDate.getTime() - frDate.getTime();
			long diffSec = diffMil / 1000;
			long Min = (diffSec) / 60;

			if (Min < 0) {
				Min = Min * -1;
			}

			return Min;

		} catch (ParseException e) {
			return -1;
		}

	}//:

	/**
	 * 두 날짜간의 월수를 반환
	 * @param startDate 시작 날짜
	 * @param endDate   끝 날짜
	 * @return 월수
	 * @see GregorianCalendar
	 */
	public static int getDifferMonths(String startDate, String endDate) {
		GregorianCalendar cal1 = getGregorianCalendar(startDate);
		GregorianCalendar cal2 = getGregorianCalendar(endDate);

		int m = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
		int months = (m * 12) + (cal1.get(Calendar.MONTH) - cal2.get(Calendar.MONTH));
		return Math.abs(months);
	}//:


	/**
	 * GregorianCalendar 객체를 반환
	 * @param yyyymmdd 날짜 인수
	 * @return GregorianCalendar
	 * @see Calendar
	 * @see GregorianCalendar
	 */

	private static GregorianCalendar getGregorianCalendar(String yyyymmdd) {

		int yyyy = Integer.parseInt(yyyymmdd.substring(0, 4));
		int mm = Integer.parseInt(yyyymmdd.substring(4, 6));
		int dd = Integer.parseInt(yyyymmdd.substring(6));

		GregorianCalendar calendar = new GregorianCalendar(yyyy, mm - 1, dd, 0,
			0, 0);

		return calendar;

	}//:

	/**
	 * 날짜 형식 출력
	 * @param date 대상 날짜
	 * @return 문자열
	 */
	public String formatDate(Timestamp date) {
		if (date == null) {
			return "";
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");

		return format.format(date);
	}

	/**
	 * 날짜 형식 출력
	 * @param date 대상 날짜
	 * @return 문자열
	 */
	public String formatDateWithTime(Timestamp date) {
		if (date == null) {
			return "";
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return format.format(date);
	}

	/**
	 * 날짜 형식 출력
	 * @param date      대상 날짜 문자열
	 * @param separator 구분자
	 * @return formatted date string
	 */
	public String formatDate(String date, String separator) {
		if (date == null || date.length() != 8) {
			return "";
		}
		if (separator == null) {
			separator = ". ";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(date.substring(0, 4))
			.append(separator)
			.append(date.substring(4, 6))
			.append(separator)
			.append(date.substring(6, 8));
		return sb.toString();
	}

	/**
	 * 월 컨트롤
	 * @param c      대상 날짜
	 * @param gap    변경할 월
	 * @param format 날짜의 포맷 지정
	 * @return formatted date string
	 */
	public static String getYearMonth(Calendar c, int gap, String format) {
		String resultString = "";
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1 + gap;
		c.set(year, month - 1, 1);
		resultString = getDateFormat(c, format);
		return resultString;
	}

	/**
	 * 입력된 yyyyMMddHHmmss 문자열에 해당하는 millesecond를 반환
	 *
	 * @param datetime
	 * @return long
	 */
	public static long getDateTimeMillisecond(String datetime) {
		long ret = 0L;
		DateFormat df = new SimpleDateFormat(YYYYMMDDHHMMSS);
		Date d = null;
		try {
			d = df.parse(datetime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isEmpty(d)) {
			ret = d.getTime();
		}
		return ret;
	}

	/**
	 * 날짜 문자열을 원하는 형식으로 변경한다
	 * @param orgDateString // 변경대상 날짜 문자열
	 * @param pattern	// 변경 패턴
	 * @return // 변경 후 날짜 문자열
	 */
	public static String changeDateFormat(String orgDateString, String pattern) {
		String orgDate = nullToEmpty(orgDateString).replaceAll("[^0-9]", "");
		String patternChar = nullToEmpty(pattern).replaceAll("[^yMdHms]", "");
		try {
			if(orgDate.length() > 0 && orgDate.length() == patternChar.length()) {
				SimpleDateFormat dateParser = new SimpleDateFormat(patternChar, Locale.KOREA);
				Date newdate = dateParser.parse(orgDate);
				dateParser.applyPattern(pattern);
				orgDateString = dateParser.format(newdate);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return orgDateString;
	}

	/**
	 * value값이 null이면 replacement를 리턴하고, value값이 null이 아니면 value값을 리턴한다.
	 *
	 * @param value       원래 문자열
	 * @param replacement 치환문자열
	 * @return 결과문자열
	 */
	public String nvl(String value, String replacement) {
		if (value == null || "".equals(value)) {
			return replacement;
		}
		return value;
	}

	/**
	 * nvl0
	 * value값이 null이거나 '0'이면 replacement를 리턴하고, value값이 null이 아니면 value값을 리턴한다.
	 *
	 * @param value       원래 문자열
	 * @param replacement 치환문자열
	 * @return 결과문자열
	 */
	public String nvl0(String value, String replacement) {
		if (value == null || "".equals(value) || "0".equals(value)) {
			return replacement;
		}
		return value;
	}

	/**
	 * 문자열로부터 HTML/XML 태그를 제거함
	 *
	 * @param message
	 * @return String message without XML or HTML
	 */
	public String stripHTMLTags(String message) {
		String noHTMLString = message.replace("\\<.*?\\>", "");
		return noHTMLString;
	}

	/**
	 * 정규화 표현식을 문자열로 변환
	 *
	 * @param str
	 * @return
	 */
	public static String codeToStr(String str) {
		str = str.replace("&lt;", "<")
			.replace("&gt;", ">")
			.replace("&#039;", "'")
			.replace("&#034;", "\"")
			.replace("&amp;", "&");

		return str;
	}

	/**
	 * 문자열을 정규화 표현식으로 변환
	 *
	 * @param str
	 * @return
	 */
	public static String strToCode(String str) {
		str = str.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("'", "&#039;")
			.replace("\"", "&#034;");

		return str;
	}

	/**
	 * 문자열 길이만큼 줄임
	 *
	 * @param content 문자열
	 * @param length  길이
	 * @return 잘린 문자열
	 */
	public static String getFitString(String content, int length) {
		if (content == null) {
			return "";
		}
		String tmp = content;
		int slen = 0;
		int blen = 0;
		if (tmp.getBytes().length > length) {
			while (blen + 1 < length && slen < tmp.length()) {
				char c = tmp.charAt(slen);
				blen++;
				slen++;
				if (c > '\177') {
					blen++;
				}
			}
			tmp = tmp.substring(0, slen);
			if (content.length() > tmp.length()) {
				tmp += "...";
			}
		}
		return tmp;
	}

	/**
	 * CLOB로 되어 있는 것을 String 으로 변경한다.
	 *
	 * @param clob
	 * @return
	 */
	public static String getStringFromCLOB(java.sql.Clob clob) {
		StringBuffer sbf = new StringBuffer();
		Reader br = null;
		char[] buf = new char[1024];
		int readcnt;
		try {
			br = clob.getCharacterStream();
			while ((readcnt = br.read(buf, 0, 1024)) != -1) {
				sbf.append(buf, 0, readcnt);
			}
		} catch (Exception e) {

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {

				}
			}
		}
		return sbf.toString();
	}

	/**
	 * 이름을 숨길때 사용 예) 홍길동  ==> 홍○동
	 *
	 * @param name
	 * @return
	 */
	public String getHiddenName(String name) {
		String tmp = "";

		if (name.length() > 1) {
			tmp = name.substring(0, 1) + "○" + name.substring(2);
		} else {
			tmp = name;
		}

		return tmp;
	}

	/**
	 * Compute the hash value to check for "real person" submission.
	 *
	 * @param value the entered value
	 * @return its hash value
	 */
	public String rpHash(String value) {
		int hash = 5381;
		value = value.toUpperCase();
		for (int i = 0; i < value.length(); i++) {
			hash = ((hash << 5) + hash) + value.charAt(i);
		}
		return String.valueOf(hash);
	}


	/**
	 * 만 20세 이상인지 확인
	 *
	 * @param year  년
	 * @param month 월 (1~12)
	 * @param day   일
	 * @return 만 20세 이상인지 여부
	 */
	public boolean isOver20Years(int year, int month, int day) {
		Calendar birthday = Calendar.getInstance();
		birthday.set(Calendar.YEAR, year);
		birthday.set(Calendar.MONTH, month - 1);
		birthday.set(Calendar.DAY_OF_MONTH, day);

		// 14년전 오늘 Calendar
		Calendar theday = Calendar.getInstance();
		theday.add(Calendar.YEAR, -19);

		return birthday.before(theday);
	}


	/**
	 * 주민번호를 받아서 생년월일(yyyyMMdd)을 리턴한다.
	 *
	 * @param ssn 주민번호
	 * @return 생년월일
	 */
	public String getBirth8(String ssn) {
		if (ssn == null) {
			//"ssn is null."
			return null;
		}
		if (ssn.length() < 7) {
			//ssn.length()
			return null;
		}
		String prefix = "20";
		char sexCode = ssn.charAt(6);
		if (sexCode == '1' || sexCode == '2' ||
			sexCode == '5' || sexCode == '6') {
			prefix = "19";
		}
		return prefix + ssn.substring(0, 6);
	}

	/**
	 * 응답을 HTML로 보낸다.
	 *
	 * @param response 응답
	 * @param data     HTML DATA
	 */
	public static void outHTML(HttpServletResponse response, String data, String charset) {
		charset = (charset == null) ? UTF : charset;
		response.setContentType("text/html; charset=" + charset);
		//response.setStatus(response.SC_OK);  // 정상

		response.setStatus(HttpServletResponse.SC_OK);  // 정상

		printToClient(response, data, charset);
	}//:

	/**
	 * data를 응답으로 보낸다.
	 *
	 * @param response 웹응답
	 * @param data     응답데이타
	 * @param charset  문자셋
	 */
	public static void printToClient(HttpServletResponse response, String data, String charset) {
		PrintWriter out = null;

		try {
			out = new PrintWriter(response.getWriter());
			out.print(data);
			out.flush();
		} catch (Exception e) {
			response.setStatus(500);
			log.error(e.getMessage());
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * 난수 생성
	 *
	 * @param scerno 난수영역 시작 값
	 * @param ecerno 난수영역 끝 값
	 * @return 생성된 난수
	 * @throws Exception
	 */
	public static int makeRandomInt(int scerno, int ecerno) {
		int result = 0;
		double cerno_range = ecerno - scerno + 1;

		try {
			long seed = System.currentTimeMillis();
			Random random = new Random(seed);
			result = (int) (random.nextDouble() * cerno_range + scerno);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return result;
	}


	/**
	 * request 정보 로그 출력
	 *
	 * @param request
	 */
	@SuppressWarnings("rawtypes")
	public void logforRequestParameter(HttpServletRequest request) {

		Map map = request.getParameterMap();
		Iterator it = map.keySet().iterator();
		Object key = null;
		String[] value = null;

		while (it.hasNext()) {
			key = it.next();
			value = (String[]) map.get(key);
			for (int i = 0; i < value.length; i++) {
				// log.info("key ==> " + key +  " value ===> " +value[i]  + " index i ==> " + i);
			}
		}

	}

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj) {
		if (obj instanceof String) {
			return obj == null || "".equals(obj.toString().trim());
		} else if (obj instanceof List) {
			return obj == null || ((List<?>) obj).isEmpty();
		} else if (obj instanceof Map) {
			return obj == null || ((Map) obj).isEmpty();
		} else if (obj instanceof Object[]) {
			return obj == null || Array.getLength(obj) == 0;
		} else {
			return obj == null;
		}
	}

	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	public static boolean isEquals(Object sobj, Object tobj) {
		if (CommUtil.isNotEmpty(sobj)) {
			return sobj.equals(tobj);
		}
		return false;
	}

	public static boolean isNotEquals(Object sobj, Object tobj) {
		return !isEquals(sobj, tobj);
	}

	public static String subString(String str, int start, int end) {
		return str.substring(start, end);
	}

	/**
	 * convertMapToObject
	 * @param map
	 * @param objClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Object convertMapToObject(Map map, Object objClass) {
		String keyAttribute = null;
		String setMethodString = "set";
		String methodString = null;
		Iterator itr = map.keySet().iterator();

		while (itr.hasNext()) {

			keyAttribute = (String) itr.next();
			methodString = setMethodString + keyAttribute.substring(0, 1).toUpperCase()
				+ keyAttribute.substring(1);

			try {
				Class<? extends Object> paramClass = objClass.getClass();

				Method[] methods = paramClass.getDeclaredMethods();    //VO
				Method[] superClassmethods = paramClass.getSuperclass()
					.getDeclaredMethods();    //BaseVO

				for (int i = 0; i <= methods.length - 1; i++) {
					if (methodString.equals(methods[i].getName())) {
						if (map.get(keyAttribute) instanceof Boolean) {
							if ((Boolean) map.get(keyAttribute)) {
								methods[i].invoke(objClass, true);
							} else {
								methods[i].invoke(objClass, false);
							}
						} else if (!(map.get(keyAttribute) instanceof ArrayList)) {
							if (map.get(keyAttribute) == null) {
								methods[i].invoke(objClass, "");
							} else {
								methods[i].invoke(objClass, map.get(keyAttribute).toString());
							}
						}
					}
				}

				for (int i = 0; i <= superClassmethods.length - 1; i++) {

					if (methodString.equals(superClassmethods[i].getName())) {
						if (map.get(keyAttribute) instanceof Boolean) {
							if ((Boolean) map.get(keyAttribute)) {
								superClassmethods[i].invoke(objClass, true);
							} else {
								superClassmethods[i].invoke(objClass, false);
							}
						} else if (!(map.get(keyAttribute) instanceof ArrayList)) {
							if (map.get(keyAttribute) == null) {
								superClassmethods[i].invoke(objClass, "");
							} else {
								superClassmethods[i].invoke(objClass,
									map.get(keyAttribute).toString());
							}
						}
					}
				}
			} catch (SecurityException e) {

			} catch (IllegalAccessException e) {

			} catch (IllegalArgumentException e) {

			} catch (InvocationTargetException e) {

			}
		}
		return objClass;
	}

	/**
	 * ConverObjectToMap
	 * @param obj
	 * @return
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Map ConverObjectToMap(Object obj) {
		try {
			//Field[] fields = obj.getClass().getFields(); //private field는 나오지 않음.
			Field[] fields = obj.getClass().getDeclaredFields();
			Map resultMap = new HashMap();
			for (int i = 0; i <= fields.length - 1; i++) {
				fields[i].setAccessible(true);
				resultMap.put(fields[i].getName(), fields[i].get(obj));
			}

			log.debug("resultMap[{}]", resultMap);

			return resultMap;
		} catch (IllegalArgumentException e) {

		} catch (IllegalAccessException e) {

		}
		return null;
	}

	/**
	 * 렌덤 스트링 생성
	 *
	 * @return
	 */
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	/**
	 * Description  : 숫자인지 여부 리턴
	 * @param s
	 * @return
	 */
	public static boolean isStringDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/*
	 * globals.properties 파일 읽기
	 */
	public static Properties loadPropertiesFile(String pathandFile) {
		Properties prop = System.getProperties();
		String webRoot = prop.getProperty("web.root");
		log.info("tmpRoot=" + webRoot);
		String properties_dir = "";
		if (webRoot == null) {
			properties_dir = prop.getProperty("user.dir") + "/src/main/resources/properties/";
		} else {
			properties_dir = webRoot + "WEB-INF/classes/resources/properties/";
		}

		String properties_file = "";
		if (pathandFile == null) {
			properties_file = properties_dir + "globals.xml";
		} else {
			properties_file = pathandFile;
		}

		log.info("properties_file=" + properties_file);

		// read globals.properties
		Properties global_properties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(properties_file);
			global_properties.load(fis);
		} catch (IOException ioe) {
			ioe.getMessage();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return global_properties;
	}

	/**
	 * alert 호출 MOBILE
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static void sendMobileAlert(HttpServletResponse response, String info, String contents,
		String message) {

		String sContents = "";
		PrintWriter out = null;
		try {
			response.setContentType("text/html;charset=utf-8");
			response.setHeader("Set-Cookie", "fileDownload=true; path=/");
			out = response.getWriter();
			sContents += "<script type='text/javascript' src='/js/jquery.min.js'></script>";
			sContents += "<script type='text/javascript'>";
			sContents += "$(document).ready(function(){";
			sContents += "alert('[" + info + ":" + contents + "] " + message + "');";
			sContents += "})";
			sContents += "</script>";

			out.println(sContents);
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * alert 메시지 전송후 메인으로 이동
	 *
	 * @param response
	 * @param message
	 */
	public static void sendAlertMsg(HttpServletResponse response, String message) {

		String sContents = "";
		PrintWriter out = null;

		try {
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();

			sContents = "<script type='text/javascript'>";
			sContents += "alert('" + message + "');";
			sContents += "window.parent.location.href='/index.go';";
			sContents += "</script>";

			out.println(sContents);
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (out != null) {
				out.close();
			}
		}

	}

	// 임시비밀번호 만들기
	public static String temporaryPassword(int size) {

		StringBuffer buffer = new StringBuffer();
		long seed = System.currentTimeMillis();
		Random random = new Random(seed);

		String chars[] = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,0,1,2,3,4,5,6,7,8,9".split(
			",");
		String SpecialChars[] = "~,!,@,#,$,%,^,&,*,(,),+,-".split(",");

		int SpCnt = random.nextInt(9);

		for (int i = 0; i < size; i++) {
			if (SpCnt == i) {
				// 특수문자
				buffer.append(SpecialChars[random.nextInt(SpecialChars.length)]);
			} else {
				// 영어+숫자
				buffer.append(chars[random.nextInt(chars.length)]);
			}
		}

		return buffer.toString();
	}

	public static String parseMessage(String message, String... args) {
		if (message == null || message.trim().length() <= 0) {
			return message;
		}

		if (args == null || args.length <= 0) {
			return message;
		}

		String[] splitMsgs = message.split("%");
		if (splitMsgs == null || splitMsgs.length <= 1) {
			return message;
		}

		for (int i = 0; i < args.length; i++) {
			String replaceChar = "%" + (i + 1);
			message = message.replaceFirst(replaceChar, args[i]);
		}
		return message;
	}

	public static String getClientIp(HttpServletRequest request) {
		String ip = InetUtil.getClientIP(request);

		if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
			ip = InetUtil.getCurrentEnvironmentNetworkIp();
		}

		return ip;
	}

	/**
	 * 페이징 쿼리에서 페이지 시작점 구하기
	 * @return Integer
	 */
	public static Integer getPageOffset(String page, String pageSize) {
		return ((Integer.parseInt(page) - 1) * Integer.parseInt(pageSize));
	}

	/**
	 * request 매핑
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, String> setParams(HttpServletRequest request) throws Exception {
		Enumeration params = request.getParameterNames();
		Map<String, String> map = new HashMap<String, String>();
		String key = "";
		String mapData = "";

		while (params.hasMoreElements()) {
			key = (String) params.nextElement();
			mapData = request.getParameter(key) == null ? "" : request.getParameter(key).toString();
			if (mapData.length() > 0) {
				if ("request".equals(key)) {
					continue;
				}
				map.put(key, mapData);
			}
		}

		return map;
	}

	/**
	 * OS 종류 조회
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static String getOs(HttpServletRequest request) {
		log.debug("CommUtil - getOs");
		// OS 구분
		String os = "";
		String userAgent = "";

		if (null != request.getHeader("User-Agent")) {
			userAgent = request.getHeader("User-Agent").toLowerCase();
		}

		if (userAgent.contains("windows nt 10.0")) {
			os = "windows10";
		}else if (userAgent.contains("windows nt 6.1")) {
			os = "windows7";
		}else if (userAgent.contains("windows nt 6.2") || userAgent.contains("windows nt 6.3")) {
			os = "windows8";
		}else if (userAgent.contains("windows nt 6.0")) {
			os = "windowsVista";
		}else if (userAgent.contains("windows nt 5.1")) {
			os = "windowsXP";
		}else if (userAgent.contains("windows nt 5.0")) {
			os = "windows2000";
		}else if (userAgent.contains("windows nt 4.0")) {
			os = "windowsNT";
		}else if (userAgent.contains("windows 98")) {
			os = "windows98";
		}else if (userAgent.contains("windows 95")) {
			os = "windows95";
		}else if (userAgent.contains("iphone")) {
			os = "iphone";
		}else if (userAgent.contains("ipad")) {
			os = "ipad";
		}else if (userAgent.contains("android")) {
			os = "android";
		}else if (userAgent.contains("mac")) {
			os = "mac";
		}else if (userAgent.contains("linux")) {
			os = "linux";
		}else{
			os = "other";
		}
		return os;
	}

	/**
	 * 브라우저 종류 조회
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static String getBrowser(HttpServletRequest request) {
		log.debug("CommUtil - getBrowser");

		String userAgent = "";
		if (null != request.getHeader("User-Agent")) {
			userAgent = request.getHeader("User-Agent").toLowerCase();
		}
		String browser = "other";

		if(userAgent.contains("trident")) {
			// IE
			browser = "explorer";
		} else if(userAgent.contains("edge") || userAgent.contains("edg")) {
			// Edge
			browser = "edge";
		} else if(userAgent.contains("whale")) {
			// Naver Whale
			browser = "whale";
		} else if(userAgent.contains("opera") || userAgent.contains("opr")) {
			// Opera
			browser = "opera";
		} else if(userAgent.contains("firefox")) {
			// Firefox
			browser = "firefox";
		} else if(userAgent.contains("safari") && !userAgent.contains("chrome")) {
			// Safari
			browser = "safari";
		} else if(userAgent.contains("chrome")) {
			// Chrome
			browser = "chrome";
		}

		return browser;
	}

	/**
	 * 년월일을 문자로 반환
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static String getCal() {
		Calendar retCal = Calendar.getInstance();
		Locale currentLocale = new Locale(KOREAN, "KOREA");
		String pattern = YYYYMMDD;
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, currentLocale);
		return formatter.format(retCal.getTime());
	}

	/**
	 * 임시 비밀번호 발급  [영문4자, 숫자3자, 특문(@, !)]
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static String getTempPassword() {
		StringBuffer password = new StringBuffer();
		int size = 8;
		int wordsize = 4;
		int numsize = 3;
		int spectialsize = 1;

		String[] wordchar = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z".split(",");
		String[] numchar = "0,1,2,3,4,5,6,7,8,9".split(",");
		String[] spectioalchar = "@,!".split(",");

		Random random = new Random();
		random.setSeed(new Date().getTime());
		Random random2 = new Random();
		random2.setSeed(new Date().getTime());

		while (password.length() < size) {

			switch (random2.nextInt(3)) {
				case 0:
					if (wordsize > 0) {
						password.append(wordchar[random.nextInt(wordchar.length)]);
						wordsize--;
					}
					break;
				case 1:
					if (numsize > 0) {
						password.append(numchar[random.nextInt(numchar.length)]);
						numsize--;
					}
					break;
				case 2:
					if (spectialsize > 0) {
						password.append(spectioalchar[random.nextInt(spectioalchar.length)]);
						spectialsize--;
					}
					break;
			}
		}
		return password.toString();
	}

	/**
	 * null변수 공백으로 변환
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static String spaceMapping(String value, String empty) {
		return isEmpty(value) ? empty : value;
	}

	/**
	 * 문자 추출하기
	 *
	 * @author Jayden
	 * @since 2021.12.17
	 */
	public static String getPatternWord(String word, String pattern) {
		String result = "";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(word);
		int count = 0;
		while (matcher.find()) {
			result = matcher.group(count);
			if (result.length() > 0) {
				break;
			}
		}
		return result;
	}


	/**
	 * object String return;
	 * @author yuritrap
	 * @since 2022.02.10
	 * @param s
	 * @return
	 */
	public static String ToStr(Object s) {
		if (isEmpty(s)) {
			return "";
		}

		return String.valueOf(s);
	}

	/**
	 * object convert int
	 * @author yuritrap
	 * @since 2022.02.23
	 * @param s
	 * @return
	 */
	public static int ToInt(Object s) {
		if (isEmpty(s)) {
			return 0;
		}
		else {
			if (ToStr(s).trim().equals("")) {
				return 0;
			}
		}

		return Integer.valueOf(ToStr(s).trim());
	}
	public static int ToParseInt(Object s) {
		if (isEmpty(s)) {
			return 0;
		}
		else {
			if (ToStr(s).trim().equals("")) {
				return 0;
			}
		}

		return  (int) Double.parseDouble(ToStr(s).trim().replace(",",""));
	}
	/**
	 * @Method Name : randomNum
	 * @date        : 2021. 9. 17.
	 * @author      : yuritrap
	 * @param len   : 생성할 난수의 길이
	 * @param dupCd : 중복 허용 여부 (1: 중복허용, 2:중복제거)
	 * @return
	 * @return type : String
	 * @Method Memo : randomNum(4,2)
	 */
	public static String randomNum(int len, int dupCd ) {
		Random rand = new Random();
        String numStr = ""; //난수가 저장될 변수

        for(int i=0;i<len;i++) {

            //0~9 까지 난수 생성
            String ran = Integer.toString(rand.nextInt(10));

            if(dupCd==1) {
                //중복 허용시 numStr에 append
                numStr += ran;
            } else if(dupCd==2) {
                //중복을 허용하지 않을시 중복된 값이 있는지 검사한다
                if(!numStr.contains(ran)) {
                    //중복된 값이 없으면 numStr에 append
                    numStr += ran;
                } else {
                    //생성된 난수가 중복되면 루틴을 다시 실행한다
                    i-=1;
                }
            }
        }
        return numStr;
	}


	/**
	 * multipart로 넘어온 파일들의 목록
	 * @author yuritrap
	 * @since 2022.02.18
	 * @param request
	 * @param filePath application.properties에서 설정된 기본 위치값
	 * @return
	 * @throws Exception
	 */
	public static List<UploadFileVO> multiPartFileList(HttpServletRequest request, String filePath, String filePath1) throws Exception  {
		List<UploadFileVO> uploadFiles = new ArrayList<UploadFileVO>();
		Object fieldName    = "";

		MultipartFile mfile = null;

		MultipartHttpServletRequest mhsr = (MultipartHttpServletRequest) request;
		Iterator iter = mhsr.getFileNames();


		//mfile = mhsr.getFile(String.valueOf(fieldName));
		try {
			while (iter.hasNext()){
				fieldName = iter.next(); // 내용을 가져와서

				if (fieldName.equals("hospitalImg_file") || fieldName.equals("pharmacyImg_file")) {
					for (MultipartFile hosImg : mhsr.getFiles(String.valueOf(fieldName))) {
						if (!hosImg.isEmpty()) {
							UploadFileVO fvo = new UploadFileVO();
							String uid = getUUID();
							String dt  = CommUtil.getCurrentDateYYYYMMDD().replace("-", "");
							String fulldt = CommUtil.getCurrentDateTime();
							File ff = new File(String.format("%s%s%s%s%s", filePath, File.separator, filePath1, File.separator, dt));

							//폴더 생성
							if (!ff.exists()) {
								ff.mkdir();
							}

							int lastIndexof = hosImg.getOriginalFilename().lastIndexOf(".");
							if (lastIndexof != -1){
								fvo.setExt(hosImg.getOriginalFilename().substring(lastIndexof+1));
								fvo.setOriFileName(hosImg.getOriginalFilename());
							}
							else {
								fvo.setExt("");
								fvo.setOriFileName(hosImg.getOriginalFilename());
							}
							mfile = mhsr.getFile(String.valueOf(fieldName));
							fvo.setSize(mfile.getSize());
							fvo.setFieldName(String.valueOf(fieldName));
							fvo.setFileName(String.format("%s%s%s%s", uid, fulldt ,".", fvo.getExt())); //실제 업로드 된 파일명
							fvo.setServerlocation(filePath);
							fvo.setServerPath(String.format("%s%s%s", filePath1, "/", dt));
							fvo.setServerFullPath(String.format("%s%s%s%s%s%s", filePath, "/",  filePath1, "/", dt,File.separator));
							fvo.setContentType(mfile.getContentType());

							File newFile = new File(String.format("%s%s",fvo.getServerFullPath(),fvo.getFileName()) );

							hosImg.transferTo(newFile);

							//mime는 파일 생성후 확인 한다.
							fvo.setMimeType(fileMimeType(String.format("%s%s%s",fvo.getServerFullPath(),File.separator,fvo.getFileName())));

							uploadFiles.add(fvo);
						}
					}
					continue;
				}

				mfile = mhsr.getFile(String.valueOf(fieldName));

				if (!mfile.isEmpty()) {
					UploadFileVO fvo = new UploadFileVO();
					String uid = getUUID();
					String dt  = CommUtil.getCurrentDateYYYYMMDD().replace("-", "");
					String fulldt = CommUtil.getCurrentDateTime();
					File ff = new File(String.format("%s%s%s%s%s", filePath, File.separator, filePath1, File.separator, dt));

					//폴더 생성
					if (!ff.exists()) {
						if (ff.mkdirs()) {
							log.debug("폴더생성성공");
						} else {
							log.error("폴더생성실패");
						}
					}

					int lastIndexof = mfile.getOriginalFilename().lastIndexOf(".");
					if (lastIndexof != -1){
						fvo.setExt(mfile.getOriginalFilename().substring(lastIndexof+1));
						fvo.setOriFileName(mfile.getOriginalFilename());
					}
					else {
						fvo.setExt("");
						fvo.setOriFileName(mfile.getOriginalFilename());
					}
					fvo.setSize(mfile.getSize());
					fvo.setFieldName(String.valueOf(fieldName));
					fvo.setFileName(String.format("%s%s%s%s", uid, fulldt ,".", fvo.getExt())); //실제 업로드 된 파일명
					fvo.setServerlocation(filePath);
					fvo.setServerPath(String.format("%s%s%s", filePath1, "/", dt));
					fvo.setServerFullPath(String.format("%s%s%s%s%s%s", filePath, "/",  filePath1, "/", dt,File.separator));
					fvo.setContentType(mfile.getContentType());

					File newFile = new File(String.format("%s%s",fvo.getServerFullPath(),fvo.getFileName()) );

					mfile.transferTo(newFile);

					//mime는 파일 생성후 확인 한다.
					fvo.setMimeType(fileMimeType(String.format("%s%s%s",fvo.getServerFullPath(),File.separator,fvo.getFileName())));

					uploadFiles.add(fvo);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		return uploadFiles;
	}

	/**
	 * File MimeType return 없으면 ""
	 * @author yuritrap
	 * @since 2022.02.21
	 * @param fileFullPath
	 * @return
	 * @throws IOException
	 */
	public static String fileMimeType(String fileFullPath) throws IOException {
	    Path source = Paths.get(fileFullPath);

	    String mimeType = Files.probeContentType(source);
	    return ToStr(mimeType);
	}

	/**
	 * String 배열 을 int 배열로
	 * @author yuritrap
	 * @since 2022.02.21
	 * @param s
	 * @return
	 */
	public static int[] StringArrayToInt(String[] s) {
		return Arrays.stream(s).mapToInt(Integer::parseInt).toArray();
	}

	/**
	 * image file Base64 String
	 * @author yuritrap
	 * @since 2022.02.25
	 * @param filePathName
	 * @return
	 * @throws Exception
	 */
	public static String getBase64String(String filePathName) throws Exception{
        String imageString = "";
        String changeString = "";
        FileInputStream inputStream = null;
        ByteArrayOutputStream byteOutStream = null;

        try
        {
            String fileExtName = filePathName.substring( filePathName.lastIndexOf(".") + 1);
            File file = new File( filePathName );
            if( file.exists() )
            {
	            inputStream = new FileInputStream( file );
	            byteOutStream = new ByteArrayOutputStream();

	            int len = 0;
	            byte[] buf = new byte[1024];
	            while( (len = inputStream.read( buf )) != -1 ) {
	                byteOutStream.write(buf, 0, len);
	            }

	            byte[] fileArray = byteOutStream.toByteArray();
	            imageString = new String( Base64.encodeBase64( fileArray ) );

	            changeString = "data:image/"+ fileExtName +";base64, "+ imageString;
            }
        }
        catch( Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
        	if (inputStream!=null) {
        		try {inputStream.close();}catch(Exception ex) {}
        	}
        	if (byteOutStream!=null) {
        		try {byteOutStream.close();}catch(Exception ex) {}
        	}
        	inputStream = null;
        	byteOutStream = null;
        }

	    return changeString;
	}

	/**
	 * 접속 url로 json 값 리턴
	 * @author yuritrap
	 * @since 2022.02.03
	 * @param pURL  접속 url 주소
	 * @param referer referer url 값
	 * @param inputParam 요청 값 (json)
	 * @return
	 */
	public static JsonObject getJsonUrl(String pURL, String referer,JsonObject inputParam) throws Exception {
		JsonObject jsonObject = null;
		JsonParser jsonParser = new JsonParser();

		BufferedReader in     = null;
		String inputLine      = "";
		URL url;
		try {
			if (isEmpty(pURL)) {
				throw new Exception("url 필수 값입니다.");
			}
			if (isEmpty(referer)) {
				throw new Exception("referer 필수 값입니다.");
			}
			if (isEmpty(inputParam) || inputParam.size() ==0) {
				throw new Exception("JsonObject 필수 값입니다.");
			}


			url = new URL(pURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			/*
			 * ※ Referer 설정 방법
			 * TEST : referer에는 테스트 결제창을 띄우는 도메인을 넣어주셔야합니다.
			 * 		  결제창을 띄울 도메인과 referer값이 다르면 [AUTH0007] 응답이 발생합니다.
			 * REAL : referer에는 가맹점 도메인으로 등록된 도메인을 넣어주셔야합니다.
			 * 		  다른 도메인을 넣으시면 [AUTH0004] 응답이 발생합니다.
			 * 		  또한, TEST에서와 마찬가지로 결제창을 띄우는 도메인과 같아야 합니다.
			 */
			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("charset", "UTF-8");
			con.setRequestProperty("referer", referer);
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(inputParam.toString().getBytes());
			wr.flush();
			wr.close();

			//int responseCode = con.getResponseCode();
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			in.close();
			in = null;

			log.info("리턴값:",response.toString());

			jsonObject = (JsonObject) jsonParser.parse(response.toString());

		} catch (Exception e) {
			throw e;
		}
		finally{
			if (in != null){
				try {
					in.close();
					in = null;
				} catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}

		return jsonObject;
	}

	/**
	 * 토큰에서 user_id, user_name 가져오기
	 * @return
	 */
	public static LoginVO getLoginVO() {
		LoginVO loginVO = new LoginVO();
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (!"anonymousUser".equals(authentication.getPrincipal())) {
				CustomUserDetails springSecurityUser = (CustomUserDetails) authentication.getPrincipal();
				loginVO.setUser_id(springSecurityUser.getUsername());
				loginVO.setUser_name(CryptoUtil.decrypt(springSecurityUser.getUser_name()));
				return loginVO;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

//
//	public static List<FileVO> ToFile(List<UploadFileVO> uploadFiles, String doctor_id) {
//		List<FileVO> result = new List<FileVO>();
//
//		if (uploadFiles!=null && uploadFiles.size()>0) {
//			UploadFileVO uploadFileVO = uploadFiles.get(0);
//			FileVO fileVO = new FileVO();
//			//파일 등록
//			int file_group_seq = fileMapper.selectMaxPlusKey(fileVO);
//			fileVO.setFile_group_seq(file_group_seq);
//			fileVO.setFile_type     ("DOCTOR_SPECIAL");
//			fileVO.setFile_reference("");
//			fileVO.setFile_location (uploadFileVO.getServerlocation());
//			fileVO.setFile_path     (uploadFileVO.getServerPath());
//			fileVO.setFile_url      ("");
//			fileVO.setFile_origin   (uploadFileVO.getOriFileName());
//			fileVO.setFile_name     (uploadFileVO.getFileName());
//			fileVO.setFile_mime     (uploadFileVO.getMimeType());
//			fileVO.setFile_size     (uploadFileVO.getSize());
//			fileVO.setFile_format   (uploadFileVO.getExt());
//			fileVO.setIn_id         (doctor_id);
//			fileVO.setUp_id         (doctor_id);
//
//		}
//		return String.valueOf(s);
//	}


	/**
	 * web string replace
	 * @author yuritrap
	 * @since 2022.03.15
	 * @param s
	 * @return
	 */
	public static String ReplaceWeb(String s) {
		String rtn = "";
		//List<JsonVO> lvo = new ArrayList<JsonVO>();
		//${fn:replace(fn:replace(fn:replace(fn:replace(fn:replace(fn:replace(view.ar_html,"\'","&#39;"),"<","&lt;"),">","&gt;"),"\"","&quot;"),"\\","&#92;"),newLineChar,"<br/>")}

		String[] vReplace = {"\'", "<", ">", "\"", "\\\\", "\n"};
		String[] rReplace = {"&#39;","&lt;", "&gt;","&quot;","&#92;","<br />"};
		if (ToStr(s).equals("")) {
			return "";
		}
		rtn = s;
		for (int i = 0;i<vReplace.length;i++) {
			rtn = rtn.replaceAll(vReplace[i], rReplace[i]);
		}
		return rtn;
	}
	

	
	/**
	 * 숫자가 1자리면 앞에 "0"을붙여 2자리로 만들어준다.
	 * @author yuritrap
	 * @since 2022.06.07
	 * @param s
	 * @return
	 */
	public static String PlusZero(String s) { 
		  
		if (ToStr(s).equals("")) {
			return "00";
		}
		if (ToParseInt(s) >=0 && ToParseInt(s) < 10) {
			return String.format("0%s", ToParseInt(s));
		}
		return s; 
	}
	

	/**
	 * excel os context down
	 * @author yuritrap
	 * @since 2022.06.08
	 * @param request
	 * @return
	 */
	public static String getDownloadContentType(HttpServletRequest request){
		String browser = request.getHeader("User-Agent");
		String contentType = "application/download;charset=utf-8";
		switch(browser) {
			case "Firefox":
			case "Opera":
				contentType = "application/octet-stream; text/html; charset=UTF-8;";
				break;
			default: // MSIE, Trident, Chrome, ..
				contentType = "application/x-msdownload; text/html; charset=UTF-8;";
				break;
		}
	
		return contentType;
	}
	
	
	/**
	 * 폴더 생성 
	 * @author yuritrap
	 * @since 2022.06.13
	 * @param folder
	 */
	public static void mkdir(String folder) {
		File ff = new File(folder); 
		if (!ff.exists()) {
			ff.mkdir();
		}
	}

	/**
	 * jar 파일안의 Resource 안의 파일을 다른 폴더로 copy 한다. 
	 * @author yuritrap
	 * @since 2022.06.13
	 * @param destFile copy할 파일명
	 * @param srcFile  원본파일 (/static/sample/LMA_정산관리_매출세금계산서발급양식.xls)
	 * @return
	 * @throws Exception
	 */
	public static int ResourceCopyFile(String destFile, String srcFile) throws Exception {
		int rtn = 0;
	    try {
			Resource resource = new ClassPathResource(srcFile); //"/static/sample/LMA_정산관리_매출세금계산서발급양식.xls");
			InputStream is = resource.getInputStream();
			writeInputStreamToFile(destFile,is);
			rtn = 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	    return rtn;
	}
	

	/**
	 * inputStream 을 File 저장 
	 * @author yuritrap
	 * @since 2022.06.13
	 * @param fileName
	 * @param in
	 * @throws IOException
	 */
	public static void writeInputStreamToFile(String fileName, InputStream in) throws Exception { 
		OutputStream out = null; 
		int read = 0; 
		try {
			out = new FileOutputStream(fileName); 
			byte[] bytes = new byte[1024];      
			while ((read = in.read(bytes)) != -1) {        
				out.write(bytes, 0, read);      
				}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		} finally { 
			if (out != null) { 
				out.close(); 
			} 
			if (in != null){
				in.close();
			} 
		}
	}
	
	/**
	 * 이미지 size 조절 하고 이미지 upload
	 * @author yuritrap
	 * @since 2022.07.06
	 * @param srcfile    ==> upload file (c:/test/update/test.png)
	 * @param newWidth	 ==> 변경할 가로
	 * @param newHeight	 ==> 변경할 세로
	 * @param autoWidth  ==> true : 가로기준
	 * @param autoHeight ==> true : 세로기준
	 * @throws Exception
	 */
	public static void ImageSave(String srcfile, int newWidth, int newHeight, Boolean autoWidth , Boolean autoHeight ) throws Exception  {

		Image resizedImage = null;
		String ext = "";
		int width;
		int height;

		try {
			ext       = srcfile.substring(srcfile.lastIndexOf(".") + 1).toLowerCase();

			File file = new File(srcfile);  //리사이즈할 파일 경로
			BufferedImage img = null;
			if (ext.equals("bmp") || ext.equals("png") || ext.equals("gif")) {
				img = ImageIO.read(file);
			} else {
				// BMP가 아닌 경우 ImageIcon을 활용해서 Image 생성
				// 이렇게 하는 이유는 getScaledInstance를 통해 구한 이미지를
				// PixelGrabber.grabPixels로 리사이즈 할때
				// 빠르게 처리하기 위함이다.
				img = ImageIO.read(file);
			}

			// 이미지 용량이 1mb 이하인 경우 바로 저장
			if (Files.size(Paths.get(srcfile))/1024/1024 <= 1 ) {
				ImageIO.write(img, ext, file);
				return;
			}

			int imageWidth = img.getWidth(null);
			int imageHeight = img.getHeight(null);

			log.debug("사진의 가로길이 : {}", imageWidth); // 파일의 가로
			log.debug("사진의 세로길이 : {}", imageHeight); // 파일의 세로
			if (autoWidth) {
				double ratio = (double)newWidth/(double)imageWidth;
				width = (int)(imageWidth * ratio);
				height = (int)(imageHeight * ratio);
			} else if (autoHeight) {
				double ratio = (double)newHeight/(double)imageHeight;
				width = (int)(imageWidth * ratio);
				height = (int)(imageHeight * ratio);
			} else {
				width = newWidth;
				height = newHeight;
			}


			// 기존 파일 삭제
			if( file.exists() ){
				file.delete();
			}
			// 리사이즈 실행 메소드에 값을 넘겨준다.
			resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);

			// 새 이미지  저장하기
			int pixels[] = new int[width * height];
			PixelGrabber pg = new PixelGrabber(resizedImage, 0, 0, width, height, pixels, 0, width);
			try {
				pg.grabPixels();
			} catch (InterruptedException e) {
				throw new IOException(e.getMessage());
			}
			BufferedImage destImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			destImg.setRGB(0, 0, width, height, pixels, 0, width);

			ImageIO.write(destImg, ext, file);
		}
		catch(Exception ex) {
			throw ex;
		}
	}
}
