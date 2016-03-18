package info.papdt.express.helper.port;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import info.papdt.express.helper.port.support.Express;

/**
 * @author Fython
 */
public class ExpressHelper {

	/** 单日 2000 次的API密钥，建议使用自己的API代替之，详情请搜索 爱查快递 */
	public static final String myid = "104262", mysecret = "2ac58b166085aefdc9c93a3a69010e87";
	public static final String ACTION_ADD_TRACK = "info.papdt.express.helper.ACTION_ADD_TRACK",
							EXTRA_ADD_EXPRESS_DATA = "data";

	/**
	 *
	 * 查询快递信息
	 *
	 * @param companyZh 快递公司中文关键词，如 “顺丰”
	 * @param number    快递单号
	 * @return          返回类型为 Express 的快递信息
	 *
	 */
	public static Express getExpressInfo(String companyZh, String number) {
		Express express = new Express(companyZh, number);
		if (companyZh.trim().length() <= 0 || number.trim().length() <= 0) {
			express.status = Express.STATUS_INPUT_ERROR;
			return express;
		}

		String companyCode = findCompanyCodeByZh(companyZh);
		if (companyCode == null) {
			express.status = Express.STATUS_COMPANY_UNSUPPORTED;
			return express;
		}

		String requestUrl = getRequestUrl(null, null, companyCode, number, null);
		String[] result = new String[1];
		try {
			if (httpGet(requestUrl, result)) {
				express.setData(result[0]);
				express.status = Express.STATUS_NORMAL;
			} else {
				express.status = Express.STATUS_SERVER_ERROR;
			}
		} catch (IOException e) {
			e.printStackTrace();
			express.status = Express.STATUS_UNKNOWN_ERROR;
		}

		return express;
	}

	/** 将得到的快递包裹添加到水表助手App中便于追踪 */
	public static boolean addExpressTrackToApp(Context context, Express express) {
		if (!isApplicationAvailable(context, "info.papdt.express.helper")) return false;

		Intent intent = new Intent(ACTION_ADD_TRACK);
		intent.putExtra(EXTRA_ADD_EXPRESS_DATA, express.toJSONObject().toString());
		context.sendBroadcast(intent);

		return true;
	}


	/** 检查某个应用是否安装 */
	private static boolean isApplicationAvailable(Context context, String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(
							packageName,
							PackageManager.GET_UNINSTALLED_PACKAGES
					);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	/** HttpGet 方法 */
	private static boolean httpGet(String url, String[] result) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			result[0] = EntityUtils.toString(httpResponse.getEntity());
			return true;
		} else {
			return false;
		}
	}

	/** 获取 API 请求地址 */
	private static String getRequestUrl(String id, String secret, String com,
	                                   String number, String encode) {
		StringBuffer resultUrl = new StringBuffer();
		resultUrl.append("http://www.kuaidi100.com/query?type=" + com);
		resultUrl.append("&postid=" + number);
		resultUrl.append("&valicode=");
		return resultUrl.toString();
	}

	/** 通过关键词获取快递公司代码 */
	private static String findCompanyCodeByZh(String companyZh) {
		String companyCode = null;
		for (int i = 0; i < CompanyInfo.info.size(); i++){
			if (companyZh.toLowerCase(Locale.getDefault()).contains(CompanyInfo.info.get(i).get("name"))){
				companyCode = CompanyInfo.info.get(i).get("code");
				break;
			}
		}
		return companyCode;
	}

	/**
	 * 支持的快递公司信息
	 */
	public static class CompanyInfo {

		public static ArrayList<HashMap<String, String>> info;

		static {
			info = new ArrayList<>();
			addTo("aae","AAE");
			addTo("anjie","安捷");
			addTo("anxinda","安信达");
			addTo("aramex","aramex");
			addTo("balunzhi","巴伦支");
			addTo("baotongda","宝通达");
			addTo("benteng","奔腾");
			addTo("cces","cCES");
			addTo("changtong","长通");
			addTo("chengguang","程光");
			addTo("chengji","城际");
			addTo("chengshi100","城市100");
			addTo("chuanxi","传喜");
			addTo("chuanzhi","传志");
			addTo("chukouyi","出口易");
			addTo("citylink","cityLink");
			addTo("coe","东方");
			addTo("cszx","城市之星");
			addTo("datian","大田");
			addTo("dayang","大洋");
			addTo("debang","德邦");
			addTo("dechuang","德创");
			addTo("dhl","dHL");
			addTo("diantong","店通");
			addTo("dida","递达");
			addTo("dingdong","叮咚");
			addTo("disifang","递四方");
			addTo("dpex","dpex");
			addTo("dsu","d速");
			addTo("ees","百福东方");
			addTo("ems","ems");
			addTo("fanyu","凡宇");
			addTo("fardar","fardar");
			addTo("fedex","国际Fedex");
			addTo("fedexcn","fedex国内");
			addTo("feibang","飞邦");
			addTo("feibao","飞豹");
			addTo("feihang","飞航");
			addTo("feihu","飞狐");
			addTo("feite","飞特");
			addTo("feiyuan","飞远");
			addTo("fengda","丰达");
			addTo("fkd","飞康达");
			addTo("gdyz","广东邮政");
			addTo("gnxb","邮政国内小包");
			addTo("gongsuda","共速达");
			addTo("guotong","国通");
			addTo("haihong","海红");
			addTo("haimeng","海盟");
			addTo("haosheng","昊盛");
			addTo("hebeijianhua","建华");
			addTo("henglu","恒路");
			addTo("huacheng","华诚");
			addTo("huahan","华翰");
			addTo("huaqi","华企");
			addTo("huaxialong","华夏龙");
			addTo("huayu","华宇");
			addTo("huiqiang","汇强");
			addTo("huitong","汇通");
			addTo("hwhq","海外环球");
			addTo("jiaji","佳吉");
			addTo("jiayi","佳怡");
			addTo("jiayunmei","加运美");
			addTo("jinda","金大");
			addTo("jingdong","京东");
			addTo("jingguang","京广");
			addTo("jinyue","晋越");
			addTo("jixianda","急先达");
			addTo("jldt","嘉里大通");
			addTo("kangli","康力");
			addTo("kcs","顺鑫KCS");
			addTo("kuaijie","快捷");
			addTo("kuanrong","宽容");
			addTo("kuayue","跨越");
			addTo("lejiedi","乐捷递");
			addTo("lianhaotong","联昊通");
			addTo("lijisong","立即送");
			addTo("longbang","龙邦");
			addTo("minbang","民邦");
			addTo("mingliang","明亮");
			addTo("minsheng","闽盛");
			addTo("nell","尼尔");
			addTo("nengda","能达");
			addTo("ocs","oCS");
			addTo("pinganda","平安达");
			addTo("pingyou","平邮");
			addTo("pinsu","品速");
			addTo("quanchen","全晨");
			addTo("quanfeng","全峰");
			addTo("quanjitong","全际通");
			addTo("quanritong","全日通");
			addTo("quanyi","全一");
			addTo("rpx","rPX保时达");
			addTo("rufeng","如风达");
			addTo("saiaodi","赛澳");
			addTo("santai","三态");
			addTo("scs","伟邦SCS");
			addTo("shengan","圣安");
			addTo("shengbang","晟邦");
			addTo("shengfeng","盛丰");
			addTo("shenghui","盛辉");
			addTo("shentong","申通");
			addTo("shunfeng","顺丰");
			addTo("suchengzhaipei","速呈宅配");
			addTo("suijia","穗佳");
			addTo("sure","速尔");
			addTo("tiantian","天天");
			addTo("tnt","tNT");
			addTo("tongcheng","通成");
			addTo("tonghe","通和天下");
			addTo("ups","uPS");
			addTo("usps","uSPS");
			addTo("wanbo","万博");
			addTo("wanjia","万家");
			addTo("weitepai","微特派");
			addTo("xianglong","祥龙");
			addTo("xinbang","新邦");
			addTo("xinfeng","信丰");
			addTo("xingchengzhaipei","星程宅配");
			addTo("xiyoute","希优特");
			addTo("yad","源安达");
			addTo("yafeng","亚风");
			addTo("yibang","一邦");
			addTo("yinjie","银捷");
			addTo("yinsu","音素");
			addTo("yishunhang","亿顺航");
			addTo("yousu","优速");
			addTo("ytfh","一统飞鸿");
			addTo("yuancheng","远成");
			addTo("yuantong","圆通");
			addTo("yuanzhi","元智捷诚");
			addTo("yuefeng","越丰");
			addTo("yumeijie","誉美捷");
			addTo("yunda","韵达");
			addTo("yuntong","运通中港");
			addTo("yuxin","宇鑫");
			addTo("ywfex","源伟丰");
			addTo("zengyi","增益");
			addTo("zhaijisong","宅急送");
			addTo("zhengzhoujianhua","郑州建华");
			addTo("zhima","芝麻开门");
			addTo("zhongtian","济南中天");
			addTo("zhongtie","中铁");
			addTo("zhongtong","中通");
			addTo("zhongxinda","忠信达");
			addTo("zhongyou","中邮");
		}

		private static void addTo(String code, String name) {
			HashMap<String, String> data = new HashMap<>();
			data.put("code", code);
			data.put("name", name);
			info.add(data);
		}

	}

}
