package com.davidniu.utils;

import com.davidniu.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {

    public static void main(String[] args) throws IOException {
        HtmlParseUtil htmlParseUtil = new HtmlParseUtil();
        List<Content> list = htmlParseUtil.parseJD("java");
        for (Content content : list) {
            System.out.println(content);
        }
    }


    public List<Content> parseJD(String keyword) throws IOException {
        //获取请求 https://search.jd.com/Search?keyword=java
        //前提: 需要联网, 而且不能获取到AJAX!
        String url = "https://search.jd.com/Search?keyword=java";

        //设置超时时间 30S
        int timeOut = 30000;

        //解析网页 ==> Document就是浏览器的Document对象
        Document document = Jsoup.parse(new URL(url), timeOut);
        //所有你在JS中可以使用的方法, 这里都能用!
        Element element = document.getElementById("J_goodsList");
        
        // 添加空值检查，防止 NullPointerException
        if (element == null) {
            System.out.println("未找到ID为'J_goodsList'的元素，可能是以下原因：");
            System.out.println("1. 京东网站结构已更新");
            System.out.println("2. 页面内容通过JavaScript动态加载（Jsoup无法执行JS）");
            System.out.println("3. 请求被京东服务器拒绝或需要登录");
            System.out.println("\n页面标题: " + document.title());
            System.out.println("页面部分内容: " + document.body().text().substring(0, Math.min(500, document.body().text().length())));
            return null;
        }
//        System.out.println(element.html());
        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        ArrayList<Content> goodsList = new ArrayList<>();

        //获取元素中的内容, 这里的el就是每一个li标签了
        for (Element el : elements) {
            //关于这种图片特别多的网站, 所有的图片都是延迟加载的!
            //JD 放在了这个class data-lazy-img
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            Content content = new Content(img, price, title);
            goodsList.add(content);

            System.out.println("===================================================");
            System.out.println(img);
            System.out.println(price);
            System.out.println(title);
        }
        return goodsList;
    }
}
