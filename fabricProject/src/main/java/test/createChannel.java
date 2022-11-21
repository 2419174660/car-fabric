package test;

import client.FabricClient;
import config.Config;
import user.UserContext;
import util.Util;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.security.CryptoSuite;


public class createChannel {
    public static void main(String[] args) {
        try {
            /************************************配置文件路径*****************************************************/
            //通道配置，用于管理员在网络中创建通道
            String supplyConfigPath = "src/main/resources/network_resources/channel_properties/supplychannel.properties";
            String marketingConfigPath = "src/main/resources/network_resources/channel_properties/marketingchannel.properties";
            String serviceConfigPath = "src/main/resources/network_resources/channel_properties/servicechannel.properties";

            //用户配置，指定用户证书/密钥路径
            String userConfigPath = "src/main/resources/network_resources/userid_properties/channels.admin1.properties";//通道管理员账户
            /*************************************************************************************************/

            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();

            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            //通道管理员创建三个通道
            Channel channel1 = fabclient.constructChannel(supplyConfigPath);
            Channel channel2 = fabclient.constructChannel(marketingConfigPath);
            Channel channel3 = fabclient.constructChannel(serviceConfigPath);

            //将管理员组织节点加入通道中
            channel1.joinPeer(fabclient.getInstance().newPeer(Config.ADMIN_PEER_0,Config.ADMIN_PEER_0_URL));
            channel1.joinPeer(fabclient.getInstance().newPeer(Config.ADMIN_PEER_1,Config.ADMIN_PEER_1_URL));
            channel2.joinPeer(fabclient.getInstance().newPeer(Config.ADMIN_PEER_0,Config.ADMIN_PEER_0_URL));
            channel2.joinPeer(fabclient.getInstance().newPeer(Config.ADMIN_PEER_1,Config.ADMIN_PEER_1_URL));
            channel3.joinPeer(fabclient.getInstance().newPeer(Config.ADMIN_PEER_0,Config.ADMIN_PEER_0_URL));
            channel3.joinPeer(fabclient.getInstance().newPeer(Config.ADMIN_PEER_1,Config.ADMIN_PEER_1_URL));


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
