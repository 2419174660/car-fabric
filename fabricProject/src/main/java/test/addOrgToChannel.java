package test;

import client.ChannelClient;
import client.FabricClient;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.util.Collection;
import java.util.HashSet;

public class addOrgToChannel {
    public static void main(String[] args) {
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            //网络配置，用于连接当前网络并获取通道实例
            String networkConfigPath = "src/main/resources/network_resources/network_connection/connection.json";
            /*组织1-supplychannel***************************************************************************************/
            //通道配置，用于管理员在网络中创建通道
            String channelConfigPath = "src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //用户配置，指定用户证书/密钥路径
            String userConfigPath = "src/main/resources/network_resources/userid_properties/channels.admin1.properties";
            String userConfigPath1 = "src/main/resources/network_resources/userid_properties/org1.admin1.properties";
            String userConfigPath2 = "src/main/resources/network_resources/userid_properties/org2.admin1.properties";
            String userConfigPath3 = "src/main/resources/network_resources/userid_properties/org3.admin1.properties";
            String ordererConfigPath = "src/main/resources/network_resources/userid_properties/orderer.admin1.properties";

            //测试：已加入组织1 2 3的通道supplychannel，再加入组织6
            UserContext userContext = Util.createUserContext(userConfigPath);
            UserContext userContext1 = Util.createUserContext(userConfigPath1);
            UserContext userContext2 = Util.createUserContext(userConfigPath2);
            UserContext userContext3 = Util.createUserContext(userConfigPath3);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelConfigPath,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);
            //组织信息json路径
            String orgConfigPath = "";
            Collection<UserContext> orgAdminList = new HashSet<>();
            orgAdminList.add(userContext);
            orgAdminList.add(userContext1);
            orgAdminList.add(userContext2);
            orgAdminList.add(userContext3);
            UserContext ordererAdmin = Util.createUserContext(ordererConfigPath);
            channelClient.addOrgToChannel(orgConfigPath, orgAdminList, ordererAdmin);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
