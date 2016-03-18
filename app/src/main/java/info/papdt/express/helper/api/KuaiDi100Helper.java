package info.papdt.express.helper.api;

import android.util.*;

import com.spreada.utils.chinese.ZHConverter;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import info.papdt.express.helper.support.*;
import org.json.*;
import java.util.*;

public class KuaiDi100Helper {

	// TODO: remove these id& secrets
	public static String myid = "104262", mysecret = "2ac58b166085aefdc9c93a3a69010e87";
	public static String xfid = "109066", xfsecret = "b1726be0ec9c6a1abe60e3d71ef72603";
	public static String smid = "", smsecret = "";
	public static String e0id = "110229", e0secret = "cedbdd77d5a5a737b09f42ec0ccbbbda";

	private static final String TAG = "KuaiDi100Helper";

	// TODO: deprecate id, secret and encode
	public static String getRequestUrl(String id, String secret, String com,
									  String number, String encode) {
		StringBuffer resultUrl = new StringBuffer();
		resultUrl.append("http://www.kuaidi100.com/query?type=" + com);
		resultUrl.append("&postid=" + number);
		resultUrl.append("&valicode=");
		Log.i(TAG, "Request URL:" + resultUrl);
		return resultUrl.toString();
	}

	public static String getDetectUrl(String number){
		return "http://www.kuaidi100.com/autonumber/autoComNum?text=" + number;
	}
	
	public static ExpressResult buildDataFromResultStr(String jsonStr) {
		ExpressResult result = new ExpressResult();
		try {
			JSONObject person = new JSONObject(jsonStr);
			JSONArray array = person.getJSONArray("data");

			String json2;
			Map<String, String> map;

			result.status = person.getInt("state");
			result.errCode = person.getInt("status");
			result.message = person.getString("message");
			result.mailNo = person.getString("nu");
			result.expSpellName = person.getString("com");
			result.expTextName = CompanyInfo.info.get((CompanyInfo.findCompanyByCode(person.getString("com")))).name;

			for (int i = array.length() - 1; i >= 0; i--) {
				map = new HashMap<>();
				json2 = array.get(i).toString();
				JSONTokener jsonParser2 = new JSONTokener(json2);
				JSONObject person2 = (JSONObject) jsonParser2.nextValue();
				map.put("time", person2.getString("time"));
				map.put("context", person2.getString("context"));
				if (!person2.getString("context").contains("官网")) {
					result.data.add(map);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			result.status = 0;
			result.message = "JSON String token error!";
			return result;
		} catch (NullPointerException e) {
			e.printStackTrace();
			result.status = 0;
			result.message = "Unknown error!";
		}
		return result;
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
			info.add(new Company("顺丰速递（中文结果）", "shunfeng", "95338", "http://www.sf-express.com/"));
			info.add(new Company("中通速递", "zhongtong", "95311", "http://www.zto.cn/"));
			info.add(new Company("圆通速递", "yuantong", "95554", "http://www.ytoexpress.com/"));
			info.add(new Company("申通", "shentong", "95543", "http://www.sto.cn/"));
			info.add(new Company("韵达快运", "yunda", "95546", "http://www.yundaex.com/"));
			info.add(new Company("百世汇通", "huitongkuaidi", "4009 565656", "http://www.800bestex.com/"));
			info.add(new Company("EMS(中文结果)", "ems", "11183", "http://www.ems.com.cn/"));

			info.add(new Company("澳大利亚邮政(英文结果）", "auspost", null, null));
			info.add(new Company("AAE", "aae", null, null));
			info.add(new Company("安信达", "anxindakuaixi", null, null));
			info.add(new Company("百福东方", "baifudongfang", null, null));
			info.add(new Company("BHT", "bht", null, null));
			info.add(new Company("包裹/平邮/挂号信", "youzhengguonei", null, null));
			info.add(new Company("邦送物流", "bangsongwuliu", null, null));
			info.add(new Company("希伊艾斯（CCES）", "cces", null, null));
			info.add(new Company("中国东方（COE）", "coe", null, null));
			info.add(new Company("传喜物流", "chuanxiwuliu ", null, null));
			info.add(new Company("加拿大邮政Canada Post（英文结果）", "canpost", null, null));
			info.add(new Company("加拿大邮政Canada Post(德文结果）", "canpostfr", null, null));
			info.add(new Company("大田物流", "datianwuliu", null, null));
			info.add(new Company("德邦物流", "debangwuliu", null, null));
			info.add(new Company("DPEX", "dpex", null, null));
			info.add(new Company("DHL-中国件-中文结果", "dhl", null, null));
			info.add(new Company("DHL-国际件-英文结果", "dhlen", null, null));
			info.add(new Company("DHL-德国件-德文结果（德国国内派、收的件）", "dhlde", null, null));
			info.add(new Company("D速快递", "dsukuaidi", null, null));
			info.add(new Company("递四方", "disifang", null, null));
			info.add(new Company("E邮宝", "ems", null, null));
			info.add(new Company("EMS（英文结果）", "emsen", null, null));
			info.add(new Company("EMS-（中国-国际）件-中文结果", "emsguoji", null, null));
			info.add(new Company("EMS-（中国-国际）件-英文结果", "emsinten", null, null));
			info.add(new Company("Fedex-国际件-英文结果", "fedex", null, null));
			info.add(new Company("Fedex-国际件-中文结果", "fedexcn", null, null));
			info.add(new Company("Fedex-美国件-英文结果", "fedexus", null, null));
			info.add(new Company("飞康达物流", "feikangda", null, null));
			info.add(new Company("飞快达", "feikuaida", null, null));
			info.add(new Company("凡客如风达", "rufengda", null, null));
			info.add(new Company("风行天下", "fengxingtianxia", null, null));
			info.add(new Company("飞豹快递", "feibaokuaidi", null, null));
			info.add(new Company("港中能达", "ganzhongnengda", null, null));
			info.add(new Company("国通快递", "guotongkuaidi", null, null));
			info.add(new Company("广东邮政", "guangdongyouzhengwuliu", null, null));
			info.add(new Company("挂号信", "youzhengguonei", null, null));
			info.add(new Company("国内邮件", "youzhengguonei", null, null));
			info.add(new Company("国际邮件", "youzhengguoji", null, null));
			info.add(new Company("GLS", "gls", null, null));
			info.add(new Company("共速达", "gongsuda", null, null));
			info.add(new Company("汇通快运", "huitongkuaidi", null, null));
			info.add(new Company("汇强快递", "huiqiangkuaidi", null, null));
			info.add(new Company("华宇物流", "tiandihuayu", null, null));
			info.add(new Company("恒路物流", "hengluwuliu", null, null));
			info.add(new Company("华夏龙", "huaxialongwuliu", null, null));
			info.add(new Company("海航天天", "tiantian", null, null));
			info.add(new Company("海外环球", "haiwaihuanqiu", null, null));
			info.add(new Company("河北建华", "hebeijianhua", null, null));
			info.add(new Company("海盟速递", "haimengsudi", null, null));
			info.add(new Company("华企快运", "huaqikuaiyun", null, null));
			info.add(new Company("山东海红", "haihongwangsong", null, null));
			info.add(new Company("佳吉物流", "jiajiwuliu", null, null));
			info.add(new Company("佳怡物流", "jiayiwuliu", null, null));
			info.add(new Company("加运美", "jiayunmeiwuliu", null, null));
			info.add(new Company("京广速递", "jinguangsudikuaijian", null, null));
			info.add(new Company("急先达", "jixianda", null, null));
			info.add(new Company("晋越快递", "jinyuekuaidi", null, null));
			info.add(new Company("捷特快递", "jietekuaidi", null, null));
			info.add(new Company("金大物流", "jindawuliu", null, null));
			info.add(new Company("嘉里大通", "jialidatong", null, null));
			info.add(new Company("快捷速递", "kuaijiesudi", null, null));
			info.add(new Company("康力物流", "kangliwuliu", null, null));
			info.add(new Company("跨越物流", "kuayue", null, null));
			info.add(new Company("联昊通", "lianhaowuliu", null, null));
			info.add(new Company("龙邦物流", "longbanwuliu", null, null));
			info.add(new Company("蓝镖快递", "lanbiaokuaidi", null, null));
			info.add(new Company("乐捷递", "lejiedi", null, null));
			info.add(new Company("联邦快递（Fedex-中国-中文结果）", "lianbangkuaidi", null, null));
			info.add(new Company("联邦快递(Fedex-中国-英文结果）", "lianbangkuaidien", null, null));
			info.add(new Company("立即送", "lijisong", null, null));
			info.add(new Company("隆浪快递", "longlangkuaidi", null, null));
			info.add(new Company("门对门", "menduimen", null, null));
			info.add(new Company("美国快递", "meiguokuaidi", null, null));
			info.add(new Company("明亮物流", "mingliangwuliu", null, null));
			info.add(new Company("OCS", "ocs", null, null));
			info.add(new Company("onTrac", "ontrac", null, null));
			info.add(new Company("全晨快递", "quanchenkuaidi", null, null));
			info.add(new Company("全际通", "quanjitong", null, null));
			info.add(new Company("全日通", "quanritongkuaidi", null, null));
			info.add(new Company("全一快递", "quanyikuaidi", null, null));
			info.add(new Company("全峰快递", "quanfengkuaidi", null, null));
			info.add(new Company("七天连锁", "sevendays", null, null));
			info.add(new Company("如风达快递", "rufengda", null, null));
			info.add(new Company("顺丰（英文结果）", "shunfengen", null, null));
			info.add(new Company("三态速递", "santaisudi", null, null));
			info.add(new Company("盛辉物流", "shenghuiwuliu", null, null));
			info.add(new Company("速尔物流", "suer", null, null));
			info.add(new Company("盛丰物流", "shengfengwuliu", null, null));
			info.add(new Company("上大物流", "shangda", null, null));
			info.add(new Company("三态速递", "santaisudi", null, null));
			info.add(new Company("山东海红", "haihongwangsong", null, null));
			info.add(new Company("赛澳递", "saiaodi", null, null));
			info.add(new Company("山东海红", "haihongwangsong", null, null));
			info.add(new Company("山西红马甲", "sxhongmajia", null, null));
			info.add(new Company("圣安物流", "shenganwuliu", null, null));
			info.add(new Company("穗佳物流", "suijiawuliu", null, null));
			info.add(new Company("天地华宇", "tiandihuayu", null, null));
			info.add(new Company("天天快递", "tiantian", null, null));
			info.add(new Company("TNT（中文结果）", "tnt", null, null));
			info.add(new Company("TNT（英文结果）", "tnten", null, null));
			info.add(new Company("通和天下", "tonghetianxia", null, null));
			info.add(new Company("UPS（中文结果）", "ups", null, null));
			info.add(new Company("UPS（英文结果）", "upsen", null, null));
			info.add(new Company("优速物流", "youshuwuliu", null, null));
			info.add(new Company("USPS（中英文）", "usps", null, null));
			info.add(new Company("万家物流", "wanjiawuliu", null, null));
			info.add(new Company("万象物流", "wanxiangwuliu", null, null));
			info.add(new Company("微特派", "weitepai", null, null));
			info.add(new Company("新邦物流", "xinbangwuliu", null, null));
			info.add(new Company("信丰物流", "xinfengwuliu", null, null));
			info.add(new Company("星晨急便（暂不支持，该公司已不存在）", "xingchengjibian", null, null));
			info.add(new Company("鑫飞鸿（暂不支持，该公司已不存在）", "xinhongyukuaidi", null, null));
			info.add(new Company("希伊艾斯(CCES)（暂不支持，该公司已不存在）", "cces", null, null));
			info.add(new Company("新邦物流", "xinbangwuliu", null, null));
			info.add(new Company("新蛋奥硕物流", "neweggozzo", null, null));
			info.add(new Company("香港邮政", "hkpost", null, null));
			info.add(new Company("运通快递", "yuntongkuaidi", null, null));
			info.add(new Company("邮政小包（国内），邮政包裹（国内）、邮政国内给据（国内）", "youzhengguonei", null, null));
			info.add(new Company("邮政小包（国际），邮政包裹（国际）、邮政国内给据（国际）", "youzhengguoji", null, null));
			info.add(new Company("远成物流", "yuanchengwuliu", null, null));
			info.add(new Company("亚风速递", "yafengsudi", null, null));
			info.add(new Company("一邦速递", "yibangwuliu", null, null));
			info.add(new Company("优速物流", "youshuwuliu", null, null));
			info.add(new Company("源伟丰快递", "yuanweifeng", null, null));
			info.add(new Company("元智捷诚", "yuanzhijiecheng", null, null));
			info.add(new Company("越丰物流", "yuefengwuliu", null, null));
			info.add(new Company("源安达", "yuananda", null, null));
			info.add(new Company("原飞航", "yuanfeihangwuliu", null, null));
			info.add(new Company("忠信达快递", "zhongxinda", null, null));
			info.add(new Company("芝麻开门", "zhimakaimen", null, null));
			info.add(new Company("银捷速递", "yinjiesudi", null, null));
			info.add(new Company("一统飞鸿", "yitongfeihong", null, null));
			info.add(new Company("宅急送", "zhaijisong", null, null));
			info.add(new Company("中邮物流", "zhongyouwuliu", null, null));
			info.add(new Company("忠信达", "zhongxinda", null, null));
			info.add(new Company("中速快件", "zhongsukuaidi", null, null));
			info.add(new Company("芝麻开门", "zhimakaimen", null, null));
			info.add(new Company("郑州建华", "zhengzhoujianhua", null, null));
			info.add(new Company("中天万运", "zhongtianwanyun", null, null));
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
