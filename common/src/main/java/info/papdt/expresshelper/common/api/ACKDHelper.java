package info.papdt.expresshelper.common.api;

import android.util.Log;

import com.spreada.utils.chinese.ZHConverter;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;

import info.papdt.expresshelper.common.api.secret.ACKD;

public class ACKDHelper {
	
	public static String myid = "104262", mysecret = "2ac58b166085aefdc9c93a3a69010e87";
	public static String xfid = "109066", xfsecret = "b1726be0ec9c6a1abe60e3d71ef72603";
	public static String smid = ACKD.SKINME_API_ID, smsecret = ACKD.SKINME_API_SECRET;
	public static String e0id = "110229", e0secret = "cedbdd77d5a5a737b09f42ec0ccbbbda";
	// public static String smid = "", smsecret = "";

	private static final String TAG = "ACKDHelper";

	public static String getRequestUrl(String id, String secret, String com,
									  String number, String encode) {
		StringBuffer resultUrl = new StringBuffer();
		resultUrl.append("http://api.ickd.cn/?id=" + (id != null ? id : xfid));
		resultUrl.append("&secret=" + (secret != null ? secret : xfsecret));
		resultUrl.append("&com=" + com);
		resultUrl.append("&nu=" + number);
		resultUrl.append("&encode=" + (encode != null ? encode : "gbk")
						 + "&ord=asc");
		Log.i(TAG, "Request URL:" + resultUrl);
		return resultUrl.toString();
	}

	public static ArrayList<CompanyInfo.Company> searchCompany(String keyword) {
		keyword = ZHConverter.convert(keyword, ZHConverter.SIMPLIFIED);
		ArrayList<CompanyInfo.Company> src = new ArrayList<>();
		if (keyword != null && keyword.trim().length() > 0) {
			for (int i = 0; i < CompanyInfo.info.size(); i++) {
				if (!CompanyInfo.names [i].contains(keyword) && !CompanyInfo.pinyin [i].contains(keyword)) {
					continue;
				}

				src.add(CompanyInfo.info.get(i));
			}
		} else {
			return CompanyInfo.info;
		}
		return src;
	}

	public static class CompanyInfo {

		public static ArrayList<Company> info;
		public static String[] names, pinyin;

		public static class Company {
			
			public String name, code, phone, website;
			
			public Company(String name, String code, String phone, String website) {
				this.name = name;
				this.code = code;
				this.phone = phone;
				this.website = website;
			}
			
		}

		public static int findCompanyByCode(String code) {
			for (int i = 0; i < info.size(); i++) {
				if (info.get(i).code.equals(code)) {
					return i;
				}
			}
			return -1;
		}
		
		static {
			info = new ArrayList<>();
			info.add(new Company("顺丰快递","shunfeng", "95338", null));
			info.add(new Company("申通快递","shentong", "400-889-5543", null));
			info.add(new Company("中通快递","zhongtong", "400-827-0270", null));
			info.add(new Company("圆通快递","yuantong", "95554", null));
			info.add(new Company("韵达快递","yunda", "400-821-6789", null));
			info.add(new Company("天天快递","tiantian", "400-188-8888", null));
			info.add(new Company("德邦物流","debang", "95353", null));
			info.add(new Company("UPS快递","ups", "400-820-8388", null));
			info.add(new Company("EMS快递","ems", "11183", null));
			info.add(new Company("AAE快递","aae", "400-610-0400", null));
			info.add(new Company("安捷快递","anjie", "0531-58705656", null));
			info.add(new Company("安能物流","anneng", "400-104-0088", null));
			info.add(new Company("安迅物流","anxun", "010-59288730", null));
			info.add(new Company("奥硕物流","aoshuo", null, null));
			info.add(new Company("Aramex国际快递","aramex", null, null));
			info.add(new Company("百千诚国际物流","baiqian", null, null));
			info.add(new Company("巴伦支","balunzhi", null, null));
			info.add(new Company("宝通达","baotongda", null, null));
			info.add(new Company("成都奔腾国际快递","benteng", null, null));
			info.add(new Company("长通物流","changtong", null, null));
			info.add(new Company("程光快递","chengguang", null, null));
			info.add(new Company("城际快递","chengji", null, null));
			info.add(new Company("城市100","chengshi100", "400-820-0088", null));
			info.add(new Company("传喜快递","chuanxi", null, null));
			info.add(new Company("传志快递","chuanzhi", null, null));
			info.add(new Company("出口易物流","chukouyi", null, null));
			info.add(new Company("CityLinkExpress","citylink", null, null));
			info.add(new Company("东方快递","coe", null, null));
			info.add(new Company("中国远洋运输","coscon", null, null));
			info.add(new Company("城市之星","cszx", null, null));
			info.add(new Company("大达物流","dada", null, null));
			info.add(new Company("大金物流","dajin", null, null));
			info.add(new Company("大田物流","datian", null, null));
			info.add(new Company("大洋物流快递","dayang", null, null));
			info.add(new Company("DHL快递","dhl", "800-810-8000", null));
			info.add(new Company("店通快递","diantong", null, null));
			info.add(new Company("递四方速递","disifang", null, null));
			info.add(new Company("DPEX快递","dpex", null, null));
			info.add(new Company("D速快递","dsu", null, null));
			info.add(new Company("百福东方物流","ees", null, null));
			info.add(new Company("E邮宝","eyoubao", null, null));
			info.add(new Company("凡宇快递","fanyu", null, null));
			info.add(new Company("Fardar","fardar", null, null));
			info.add(new Company("国际Fedex","fedex", "400.886.1888", null));
			info.add(new Company("Fedex国内","fedexcn", "400-889-1888", null));
			info.add(new Company("飞豹快递","feibao", null, null));
			info.add(new Company("原飞航物流","feihang", null, null));
			info.add(new Company("飞特物流","feite", null, null));
			info.add(new Company("飞洋快递","feiyang", null, null));
			info.add(new Company("飞远物流","feiyuan", null, null));
			info.add(new Company("丰达快递","fengda", null, null));
			info.add(new Company("港快速递","gangkuai", null, null));
			info.add(new Company("高铁快递","gaotie", null, null));
			info.add(new Company("广东邮政物流","gdyz", null, null));
			info.add(new Company("邮政小包","gnxb", null, null));
			info.add(new Company("共速达物流","gongsuda", null, null));
			info.add(new Company("冠达快递","guanda", null, null));
			info.add(new Company("国通快递","guotong", null, null));
			info.add(new Company("山东海红快递","haihong", null, null));
			info.add(new Company("好来运快递","haolaiyun", null, null));
			info.add(new Company("昊盛物流","haosheng", null, null));
			info.add(new Company("河北建华快递","hebeijianhua", null, null));
			info.add(new Company("恒路物流","henglu", null, null));
			info.add(new Company("恒宇运通","hengyu", null, null));
			info.add(new Company("香港邮政","hkpost", null, null));
			info.add(new Company("华诚物流","huacheng", null, null));
			info.add(new Company("华翰物流","huahan", null, null));
			info.add(new Company("华航快递","huahang", null, null));
			info.add(new Company("黄马甲快递","huangmajia", null, null));
			info.add(new Company("华企快递","huaqi", null, null));
			info.add(new Company("华宇物流","huayu", null, null));
			info.add(new Company("汇通快递","huitong", null, null));
			info.add(new Company("户通物流","hutong", null, null));
			info.add(new Company("海外环球快递","hwhq", null, null));
			info.add(new Company("国际邮政快递","intmail", null, null));
			info.add(new Company("佳惠尔快递","jiahuier", null, null));
			info.add(new Company("佳吉快运","jiaji", null, null));
			info.add(new Company("佳怡物流","jiayi", null, null));
			info.add(new Company("佳宇物流","jiayu", null, null));
			info.add(new Company("加运美快递","jiayunmei", null, null));
			info.add(new Company("捷特快递","jiete", null, null));
			info.add(new Company("金大物流","jinda", null, null));
			info.add(new Company("京东快递","jingdong", null, null));
			info.add(new Company("京广快递","jingguang", null, null));
			info.add(new Company("晋越快递","jinyue", null, null));
			info.add(new Company("久易快递","jiuyi", null, null));
			info.add(new Company("急先达物流","jixianda", null, null));
			info.add(new Company("嘉里大通物流","jldt", null, null));
			info.add(new Company("日本邮政","jppost", null, null));
			info.add(new Company("康力物流","kangli", null, null));
			info.add(new Company("顺鑫快递","kcs", null, null));
			info.add(new Company("快捷快递","kuaijie", null, null));
			info.add(new Company("快淘速递","kuaitao", null, null));
			info.add(new Company("快优达速递","kuaiyouda", null, null));
			info.add(new Company("宽容物流","kuanrong", null, null));
			info.add(new Company("跨越快递","kuayue", null, null));
			info.add(new Company("蓝弧快递","lanhu", null, null));
			info.add(new Company("乐捷递快递","lejiedi", null, null));
			info.add(new Company("联昊通快递","lianhaotong", null, null));
			info.add(new Company("成都立即送快递","lijisong", null, null));
			info.add(new Company("上海林道货运","lindao", null, null));
			info.add(new Company("龙邦快递","longbang", null, null));
			info.add(new Company("门对门快递","menduimen", null, null));
			info.add(new Company("蒙速快递","mengsu", null, null));
			info.add(new Company("民邦快递","minbang", null, null));
			info.add(new Company("明亮物流","mingliang", null, null));
			info.add(new Company("闽盛快递","minsheng", null, null));
			info.add(new Company("南北快递","nanbei", null, null));
			info.add(new Company("尼尔快递","nell", null, null));
			info.add(new Company("能达快递","nengda", null, null));
			info.add(new Company("新顺丰快递","nsf", null, null));
			info.add(new Company("OCS快递","ocs", null, null));
			info.add(new Company("陪行物流","peixing", null, null));
			info.add(new Company("平安达","pinganda", null, null));
			info.add(new Company("中国邮政","pingyou", "11185", null));
			info.add(new Company("贝邮宝","ppbyb", "4008-206-207", null));
			info.add(new Company("全晨快递","quanchen", null, null));
			info.add(new Company("全峰快递","quanfeng", null, null));
			info.add(new Company("全日通快递","quanritong", null, null));
			info.add(new Company("全一快递","quanyi", null, null));
			info.add(new Company("日日顺物流","ririshun", null, null));
			info.add(new Company("日昱物流","riyu", null, null));
			info.add(new Company("RPX保时达","rpx", null, null));
			info.add(new Company("如风达快递","rufeng", null, null));
			info.add(new Company("瑞丰速递","ruifeng", null, null));
			info.add(new Company("赛澳递","saiaodi", null, null));
			info.add(new Company("三态速递","santai", null, null));
			info.add(new Company("伟邦快递","scs", null, null));
			info.add(new Company("圣安物流","shengan", null, null));
			info.add(new Company("晟邦物流","shengbang", null, null));
			info.add(new Company("盛丰物流","shengfeng", null, null));
			info.add(new Company("盛辉物流","shenghui", null, null));
			info.add(new Company("世运快递","shiyun", null, null));
			info.add(new Company("思迈快递","simai", null, null));
			info.add(new Company("新加坡邮政","singpost", null, null));
			info.add(new Company("速呈宅配","suchengzhaipei", null, null));
			info.add(new Company("穗佳物流","suijia", null, null));
			info.add(new Company("速尔快递","sure", null, null));
			info.add(new Company("速腾快递","suteng", null, null));
			info.add(new Company("速通物流","sutong", null, null));
			info.add(new Company("TNT快递","tnt", null, null));
			info.add(new Company("高考录取通知书","tongzhishu", null, null));
			info.add(new Company("合众速递","ucs", null, null));
			info.add(new Company("USPS快递","usps", null, null));
			info.add(new Company("万博快递","wanbo", null, null));
			info.add(new Company("万家物流","wanjia", null, null));
			info.add(new Company("万象物流","wanxiang", null, null));
			info.add(new Company("微特派","weitepai", null, null));
			info.add(new Company("五环速递","wuhuan", null, null));
			info.add(new Company("祥龙运通快递","xianglong", null, null));
			info.add(new Company("新邦物流","xinbang", null, null));
			info.add(new Company("信丰快递","xinfeng", null, null));
			info.add(new Company("星程宅配快递","xingchengzhaipei", null, null));
			info.add(new Company("希优特快递","xiyoute", null, null));
			info.add(new Company("源安达快递","yad", null, null));
			info.add(new Company("亚风快递","yafeng", null, null));
			info.add(new Company("燕文物流","yanwen", null, null));
			info.add(new Company("一邦快递","yibang", null, null));
			info.add(new Company("银捷快递","yinjie", null, null));
			info.add(new Company("亿顺航快递","yishunhang", null, null));
			info.add(new Company("优速快递","yousu", null, null));
			info.add(new Company("北京一统飞鸿快递","ytfh", null, null));
			info.add(new Company("远成物流","yuancheng", null, null));
			info.add(new Company("越丰快递","yuefeng", null, null));
			info.add(new Company("宇宏物流","yuhong", null, null));
			info.add(new Company("誉美捷快递","yumeijie", null, null));
			info.add(new Company("运通中港快递","yuntong", null, null));
			info.add(new Company("增益快递","zengyi", null, null));
			info.add(new Company("宅急送快递","zhaijisong", "400-6789-000", null));
			info.add(new Company("郑州建华快递","zhengzhoujianhua", null, null));
			info.add(new Company("芝麻开门快递","zhima", null, null));
			info.add(new Company("济南中天万运","zhongtian", null, null));
			info.add(new Company("中铁快运","zhongtie", null, null));
			info.add(new Company("忠信达快递","zhongxinda", null, null));
			info.add(new Company("中邮物流","zhongyou", null, null));
			info.add(new Company("纵行物流","zongxing", null, null));
			info.add(new Company("中铁物流","ztwl", null, null));
			info.add(new Company("佐川急便","zuochuan", null, null));
			names = new String[info.size()];
			pinyin = new String[info.size()];

			HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
			format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

			for (int i = 0; i < info.size(); i++) {
				names [i] = info.get(i).name;

				StringBuffer sb = new StringBuffer();
				for (int j = 0; j < names [i].length(); j++) {
					try {
						String[] s = PinyinHelper.toHanyuPinyinStringArray(names[i].toCharArray() [j], format);
						if (s == null) continue;
						sb.append(s[0].toCharArray() [0]);
					} catch (BadHanyuPinyinOutputFormatCombination e) {
						e.printStackTrace();
					}
				}

				pinyin [i] = sb.toString();
			}
		}

	}

}
