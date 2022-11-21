package servlets;
import config.Config;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import util.Util;

import java.io.*;
import java.util.*;

@WebServlet(name = "getUsercontextServlet", value = "/userLogin")
public class getUsercontextServlet extends HttpServlet {
    /*
        读取文件夹中的user_propertites文件,返回用户配置信息列表
    * */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();

            String path = Config.user_properties_file_path;
            JSONArray jsonArray = new JSONArray();
            File file = new File(path);
            File[] tempList = file.listFiles();


            for (int i = 0; i < tempList.length; i++) {
                if (tempList[i].isFile()) {
                    String userConfigPath = tempList[i].toString();
                    Properties userConfig = new Properties();
                    InputStream userConfigStream = new BufferedInputStream(new FileInputStream(userConfigPath));
                    userConfig.load(userConfigStream);
                    //将用户属性打包发送到前端
                    String userName = userConfig.getProperty("userName");
                    String mspId = userConfig.getProperty("mspId");
                    String authority = userConfig.getProperty("authority");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userConfigPath",userConfigPath);
                    jsonObject.put("userName",userName);
                    jsonObject.put("mspId",mspId);
                    jsonObject.put("authority",authority);
                    jsonArray.add(jsonObject);
                }
            }

            System.out.println(jsonArray);

            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println(jsonArray);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
