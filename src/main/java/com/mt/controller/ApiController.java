package com.mt.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @create 2022-06-13 15:01
 */
@Api(description = "爬虫", value = "爬虫")
@RestController
@RequestMapping("/api")
public class ApiController {
    private String url1 = "https://data.weather.gov.hk/weatherAPI/opendata/weather.php?dataType=rhrread&lang=en";
    private String url2 = "https://data.weather.gov.hk/weatherAPI/opendata/weather.php?dataType=fnd&lang=en";
    private static final Map<String, String> placeToDistrict = new HashMap<String, String>();
    static {
        placeToDistrict.put("King's Park","Yau Tsim Mong");
        placeToDistrict.put("Hong Kong Observatory","Yau Tsim Mong");
        placeToDistrict.put("Wong Chuk Hang","Southern District");
        placeToDistrict.put("Ta Kwu Ling","North District");
        placeToDistrict.put("Lau Fau Shan","Yuen Long");
        placeToDistrict.put("Tai Po","Tai Po");
        placeToDistrict.put("Sha Tin","Sha Tin");
        placeToDistrict.put("Tuen Mun","Tuen Mun");
        placeToDistrict.put("Tseung Kwan O","Sai Kung");
        placeToDistrict.put("Sai Kung","Sai Kung");
        placeToDistrict.put("Chek Lap Kok","Islands District");
        placeToDistrict.put("Tsing Yi","Kwai Tsing");
        placeToDistrict.put("Shek Kong","Yuen Long");
        placeToDistrict.put("Tsuen Wan Ho Koon","Tsuen Wan");
        placeToDistrict.put("Tsuen Wan Shing Mun Valley","Tsuen Wan");
        placeToDistrict.put("Hong Kong Park","Central &amp; Western District");
        placeToDistrict.put("Shau Kei Wan","Eastern District");
        placeToDistrict.put("Kowloon City","Kowloon City");
        placeToDistrict.put("Happy Valley","Wan Chai");
        placeToDistrict.put("Wong Tai Sin","Wong Tai Sin");
        placeToDistrict.put("Stanley","Southern District");
        placeToDistrict.put("Kwun Tong","Kowloon City");
        placeToDistrict.put("Sham Shui Po","Sham Shui Po");
        placeToDistrict.put("Yuen Long Park","Yuen Long");
        placeToDistrict.put("Tai Mei Tuk","Tai Po");
    }



    @RequestMapping(value = "/homepage", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject getT(){
        String result = getData(url1);
        JSONObject total = JSONObject.parseObject(result);
        JSONObject res = new JSONObject();
        Object obj = total.get("uvindex");
        if(total.get("uvindex").toString().length() != 0) {
            res.put("uvindex", total.getJSONObject("uvindex"));
        }
        else {
            res.put("uvindex", null);
        }
        res.put("temperature", total.getJSONObject("temperature"));
        res.put("humidity", total.getJSONObject("humidity"));

        JSONArray temp = res.getJSONObject("temperature").getJSONArray("data");
        for(int i = 0; i < temp.size(); i++) {
            String place = temp.getJSONObject(i).getString("place");
            String district = placeToDistrict.get(place);
            JSONObject rainfall_info = findrfByDistrict(district, total);
            res.getJSONObject("temperature").getJSONArray("data")
                    .getJSONObject(i).put("rainfall",rainfall_info);
        }
        return res;
    }

    @RequestMapping(value = "/forecast", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject getF() {
        String result = getData(url2);
        JSONObject total = JSON.parseObject(result);
        JSONObject res = new JSONObject();
        res.put("weatherForecast",total.getJSONArray("weatherForecast"));
        return res;
    }

    private JSONObject findrfByDistrict(String district, JSONObject total) {
        JSONArray rainfall_array = total.getJSONObject("rainfall").getJSONArray("data");
        for(int i = 0; i < rainfall_array.size(); i++) {
            if(rainfall_array.getJSONObject(i).getString("place").equals(district)) {
                return rainfall_array.getJSONObject(i);
            }
        }
        return null;
    }

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    public String getData(String url) {
        BufferedReader in = null;
        HttpURLConnection conn = null;
        String result = "";
        try {
            //该部分必须在获取connection前调用
            trustAllHttpsCertificates();
            HostnameVerifier hv = new HostnameVerifier() {
                @Override
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            conn = (HttpURLConnection)new URL(url).openConnection();
            // 发送GET请求必须设置如下两行
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            // flush输出流的缓冲
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            //logger.error("发送 GET 请求出现异常！\t请求ID:"+id+"\n"+e.getMessage()+"\n");
        } finally {// 使用finally块来关闭输出流、输入流
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                //logger.error("关闭数据流出错了！\n"+ex.getMessage()+"\n");
            }
        }
        // 获得相应结果result,可以直接处理......
        return result;
    }

    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
}
