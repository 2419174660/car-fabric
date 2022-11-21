package servlets;

import client.FabricClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

@WebServlet(name = "getChannelMsgServlet", value = "/getChannelMsg")
public class getChannelMsgServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    /*
    获取某一通道内所有节点的信息
    * */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            String channelName = request.getParameter("channelName");
            String curUserConfPath = request.getParameter("curUserConfPath");
            System.out.println(channelName);
            System.out.println(curUserConfPath);

            UserContext userContext = Util.createUserContext(curUserConfPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.getChannelFromConfig(channelName);
            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(channel.getPeers()));

            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println(jsonArray);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.flush();
            out.close();
        }
    }
}
