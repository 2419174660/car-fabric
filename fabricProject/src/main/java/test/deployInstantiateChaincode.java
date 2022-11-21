package test;

import client.ChannelClient;
import client.FabricClient;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ProposalResponse;
import user.UserContext;
import util.Util;

import java.util.Collection;

public class deployInstantiateChaincode {
    public static void main(String[] args) {
        try {
            String[] arguments = {};
            //网络配置，用于连接当前网络并获取通道实例
            String networkConfigPath = "src/main/resources/network_resources/network_connection/connection.json";
            /*****************************************supplychannel***********************************************/
            //通道配置，用于管理员在网络中创建通道
            String channelConfigPath = "src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //链码配置
            String chaincodeConfigPath = "src/main/resources/network_resources/chaincode_properties/supplychannelone.chaincode.properties";

            /*****************************************marketingchannel***********************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/marketingchannel.properties";
            //String chaincodeConfigPath = "src/main/resources/network_resources/chaincode_properties/marketingchannel.chaincode.properties";

            /*****************************************servicechannel***********************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/servicechannel.properties";
            //String chaincodeConfigPath = "src/main/resources/network_resources/chaincode_properties/servicechannel.chaincode.properties";


            //用户配置，指定用户证书/密钥路径
            String userConfigPath = "src/main/resources/network_resources/userid_properties/org1.admin1.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org2.admin1.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org3.admin1.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org4.admin1.properties";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org5.admin1.properties";


            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelConfigPath,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);
            //安装链码
            channelClient.deployChainCode(channel.getPeers(),chaincodeConfigPath);
            //初始化链码
            Collection<ProposalResponse> response = channelClient.instantiateChainCode(channel.getPeers(), chaincodeConfigPath);
            System.out.println(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
