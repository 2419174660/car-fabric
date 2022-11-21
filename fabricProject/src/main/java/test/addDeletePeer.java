package test;

import client.FabricClient;
import config.Config;
import user.UserContext;
import util.Util;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;


public class addDeletePeer {
    public static void main(String[] args) {
        try {
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            //网络配置，用于连接当前网络并获取通道实例
            String networkConfigPath = "src/main/resources/network_resources/network_connection/connection.json";
            /*组织1-supplychannel***************************************************************************************/
            //通道配置，用于管理员在网络中创建通道
            String channelConfigPath = "src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //用户配置，指定用户证书/密钥路径
            String userConfigPath = "src/main/resources/network_resources/userid_properties/org1.admin1.properties";

            /*组织2-supplychannel***************************************************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org2.admin1.properties";

            /*组织3-supplychannel***************************************************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org3.admin1.properties";

            /*组织3-marketingchannel************************************************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/marketingchannel.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org3.admin1.properties";

            /*组织4-marketingchannel************************************************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/marketingchannel.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org4.admin1.properties";

            /*组织4-servicechannel**************************************************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/servicechannel.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org4.admin1.properties";

            /*组织5-servicechannel*************************************************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/servicechannel.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org5.admin1.properties";


            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);

            Peer org1_peer0 = fabclient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
            Peer org1_peer1 = fabclient.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
            Peer org2_peer0 = fabclient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
            Peer org2_peer1 = fabclient.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);
            Peer org3_peer0 = fabclient.getInstance().newPeer(Config.ORG3_PEER_0, Config.ORG3_PEER_0_URL);
            Peer org3_peer1 = fabclient.getInstance().newPeer(Config.ORG3_PEER_1, Config.ORG3_PEER_1_URL);
            Peer org4_peer0 = fabclient.getInstance().newPeer(Config.ORG4_PEER_0, Config.ORG4_PEER_0_URL);
            Peer org4_peer1 = fabclient.getInstance().newPeer(Config.ORG4_PEER_1, Config.ORG4_PEER_1_URL);
            Peer org5_peer0 = fabclient.getInstance().newPeer(Config.ORG5_PEER_0, Config.ORG5_PEER_0_URL);
            Peer org5_peer1 = fabclient.getInstance().newPeer(Config.ORG5_PEER_1, Config.ORG5_PEER_1_URL);

            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelConfigPath,userConfigPath);

            //节点加入通道
            channel.joinPeer(org1_peer0);
            channel.joinPeer(org1_peer1);
            fabclient.saveChannelAsConfig(channel);
            System.out.println(channel.getPeers());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
