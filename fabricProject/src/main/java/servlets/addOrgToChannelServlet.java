package servlets;

import client.FabricClient;
import client.ChannelClient;
import config.Config;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.util.Collection;
import java.util.HashSet;

import java.io.PrintWriter;

@WebServlet(name = "addOrgToChannelServlet", value = "/addOrgToChannel")
public class addOrgToChannelServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();

            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();

            //获取数据
            String userConfigPath = request.getParameter("userpath");
            String orgJsonConfigPath = request.getParameter("orgjsonpath");
            
            String networkConfigPath = "/home/syunk/Desktop/fabricProject/src/main/resources/network_resources/network_connection/connection.json";
            /*组织1-supplychannel***************************************************************************************/
            //通道配置，用于管理员在网络中创建通道
            String channelConfigPath = "/home/syunk/Desktop/fabricProject/src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //用户配置，指定用户证书/密钥路径
            //String userConfigPath = "/home/syunk/Desktop/fabricProject/src/main/resources/network_resources/userid_properties/channels.admin1.properties";
            String userConfigPath1 = "/home/syunk/Desktop/fabricProject/src/main/resources/network_resources/userid_properties/org1.admin1.properties";
            String userConfigPath2 = "/home/syunk/Desktop/fabricProject/src/main/resources/network_resources/userid_properties/org2.admin1.properties";
            String userConfigPath3 = "/home/syunk/Desktop/fabricProject/src/main/resources/network_resources/userid_properties/org3.admin1.properties";
            String ordererConfigPath = "/home/syunk/Desktop/fabricProject/src/main/resources/network_resources/userid_properties/orderer.admin1.properties";

            //测试：已加入组织1 2 3的通道supplychannel，再加入组织6
            UserContext userContext = Util.createUserContext(userConfigPath);
            UserContext userContext1 = Util.createUserContext(userConfigPath1);
            UserContext userContext2 = Util.createUserContext(userConfigPath2);
            UserContext userContext3 = Util.createUserContext(userConfigPath3);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelConfigPath,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);
            //组织信息json路径
            Collection<UserContext> orgAdminList = new HashSet<>();
            orgAdminList.add(userContext);
            orgAdminList.add(userContext1);
            orgAdminList.add(userContext2);
            orgAdminList.add(userContext3);
            UserContext ordererAdmin = Util.createUserContext(ordererConfigPath);
            channelClient.addOrgToChannel(orgJsonConfigPath, orgAdminList, ordererAdmin);

            out.println("添加成功");
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
