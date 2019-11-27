package day19;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test1 {
	@Test
	public void test1() throws Exception {
		// www.tedu.cn
		String body = Jsoup.connect("http://www.tedu.cn").execute().body();
		System.out.println(body);
	}
	
	@Test
	public void test2() throws Exception {
		String body = Jsoup
				.connect("https://item.jd.com/100009177372.html")
				.execute()
				.body();
		
		System.out.println(body);
	}
	
	@Test
	public void test3() throws Exception {
		String title = getTitle("https://item.jd.com/100009177372.html");
		System.out.println(title);
	}
	/*
	 * DOM��
	 *    /  ---------------------------- Document
	 *     |- <html>  ------------------- Element
	 *          |- <head>  -------------- Element
	 *          |- <body>  -------------- Element
	 *               |- <div>
	 *                   |- class="c1"  - Attribute
	 *               |- <div>
	 *                    |- <div>
	 *                    |- <p>
	 *               |- <div>
	 */
	private String getTitle(String url) throws Exception {
		//���html DOM���ĸ��ڵ�
		Document doc = Jsoup.connect(url).get();
		//doc.select("div.sku-name").get(0)
		Element e = doc.selectFirst("div.sku-name");//��cssѡ����ѡ��Ԫ��
		String title = e.text();//��Ԫ�ػ���ڲ��������ı�
		return title;
	}
	
	@Test
	public void test4() throws Exception {
		double price = getPrice("100009177372");
		System.out.println(price);
	}

	private double getPrice(String id) throws Exception {
		String url = "https://p.3.cn/prices/mgets?skuIds="+id;
		String userAgent = "Mozilla/5.0 (Windows NT 5.1; zh-CN) AppleWebKit/535.12 (KHTML, like Gecko) Chrome/22.0.1229.79 Safari/535.12";
		
		String json = 
			Jsoup.connect(url)
			     .userAgent(userAgent)//��ƭ������,�ͻ����Ǹ������
			     .ignoreContentType(true)//��jsoup��������ʱ,��Ҫ�����ݵ���html���д���
			     .execute()
			     .body();
		//System.out.println(json);
		/*
		 * [
		 *    {"a":2, "b":5, "p":6899}
		 * ]
		 */
		ObjectMapper m = new ObjectMapper();
		//TypeReferenceʵ��û���κ�����,ֻ�����������ڲ����﷨��ָ����������
		List<Map<String,String>> list =
		 m.readValue(json, new TypeReference<List<Map<String,String>>>() {});
		
		String p = list.get(0).get("p");
		return Double.parseDouble(p);
	}
	
	@Test
	public void test5() throws Exception {
		String desc = getDesc("100009177372");
		System.out.println(desc);
	}

	private String getDesc(String id) throws Exception {
		//�����ַ,�ͼ۸��ַ��ͬ
		String url = "http://d.3.cn/desc/"+id;
		String userAgent = "Mozilla/5.0 (Windows NT 5.1; zh-CN) AppleWebKit/535.12 (KHTML, like Gecko) Chrome/22.0.1229.79 Safari/535.12";
		
		String json = 
			Jsoup.connect(url).userAgent(userAgent).ignoreContentType(true)
			     .execute().body();
		/*
		 * showdesc({....,"content":"...."})
		 *          |                      |
		 *          9                     len()-1
		 */
		json = json.substring(9, json.length()-1);//��ȡ�м��json��ʽ����
		//��jackson api����json,��json�����Map����
		ObjectMapper m = new ObjectMapper();
		Map<String,String> map =
		 m.readValue(json, new TypeReference<Map<String,String>>() {});
		return map.get("content");
	}
	
	@Test
	public void test6() throws Exception {
		/* [
		 *    "http://....",
		 *    "http://....",
		 *    "http://...."
		 * ]
		 */
		List<String> list = getAllSort();
		for (String s : list) {
			System.out.println(s);
		}
		System.out.println(list.size());
	}

	private List<String> getAllSort() throws Exception {
		String url = "https://www.jd.com/allSort.aspx";
		Document doc = Jsoup.connect(url).get();
		Elements es = doc.select("dl.clearfix dd a");
		ArrayList<String> list = new ArrayList<String>();
		for (Element e : es) {
			String link = e.attr("href");
			if (link.startsWith("//list.jd.com")) {
				list.add("http:"+link);
			}
		}
		return list;
	}
	
	@Test
	public void test7() throws Exception {
		int maxPage = getMaxPage("https://list.jd.com/list.html?cat=1319,1526,7060");
		System.out.println(maxPage);
	}

	private int getMaxPage(String url) throws Exception {
		Document doc = Jsoup.connect(url).get();
		String p = doc.selectFirst("div.f-pager i").text();
		return Integer.parseInt(p);
	}
	
	/*
	 * https://list.jd.com/list.html?cat=1319,1526,7060&page=1
	 * https://list.jd.com/list.html?cat=1319,1526,7060&page=2
	 * https://list.jd.com/list.html?cat=1319,1526,7060&page=3
	 * https://list.jd.com/list.html?cat=1319,1526,7060&page=4
	 * ....
	 * https://list.jd.com/list.html?cat=1319,1526,7060&page=306
	 */
	@Test
	public void test8() throws Exception {
		List<String> list = getAllPageList("https://list.jd.com/list.html?cat=1319,1526,7060");
		for (String s : list) {
			System.out.println(s);
		}
	}

	private List<String> getAllPageList(String url) throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		// 306
		int max = getMaxPage(url);
		for (int i = 1; i <= max; i++) {
			list.add(url+"&page="+i);
		}
		return list;
	}
	
	//һ��ҳ����,������Ʒ�ĵ�ַ�б�
	@Test
	public void test9() throws Exception {
		List<String> list = getItemList("https://list.jd.com/list.html?cat=1319,1526,7060&page=136");
		for (String s : list) {
			System.out.println(s);
		}
	}

	private List<String> getItemList(String url) throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		Document doc = Jsoup.connect(url).get();
		Elements es = doc.select("div#plist div.p-img a");
		for (Element e : es) {
			String link = e.attr("href");
			list.add("http:"+link);
		}
		return list;
	}
	
	@Test
	public void test10() throws Exception {
		//�������з���
		List<String> list = getAllSort();
		for (String url : list) {
			handelFenLei(url);
		}
	}

	private void handelFenLei(String url) throws Exception {
		//����һ������,���������еķ�ҳ
		List<String> list = getAllPageList(url);
		for (String url2 : list) {
			handelPage(url2);
		}
	}

	private void handelPage(String url) throws Exception {
		//������һҳ�е�60����Ʒ
		List<String> list = getItemList(url);
		for (String url2 : list) {
			handelItem(url2);
		}
	}

	private void handelItem(String url) {
		// http://item.jd.com/34259816591.html
		//                   |           |
		try {
			int from = url.lastIndexOf("/")+1;
			int to = url.lastIndexOf(".");
			String id = url.substring(from, to);
			
			String title = getTitle(url);
			double price = getPrice(id);
			System.out.println(title);
			System.out.println(price);
			System.out.println("-------------------------");
		} catch (Exception e) {
			
		}
	}
}


















