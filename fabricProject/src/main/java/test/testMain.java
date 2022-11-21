package test;

import client.ChannelClient;
import client.FabricClient;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import user.UserContext;
import util.Util;

import java.util.Collection;

public class testMain {
    public static void main(String[] args) {
        try{
            /************************************配置文件路径*****************************************************/
            //网络配置，用于连接当前网络并获取通道实例
            //String networkConfigPath = "src/main/resources/network_resources/network_connection/connection.json";

            //通道配置，用于管理员在网络中创建通道
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/marketingchannel.properties";
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/servicechannel.properties";

            //用户配置，指定用户证书/密钥路径
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/channels.admin1.properties";//通道管理员账户
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org1.admin1.properties";
            //String userConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/userid_properties/org1.user1.properties";

            //链码配置
            //String chaincodeConfigPath = "src/main/resources/network_resources/chaincode_properties/supplychannelone.chaincode.properties";
            //String chaincodeConfigPath = "src/main/resources/network_resources/chaincode_properties/marketingchannel.chaincode.properties";
            //String chaincodeConfigPath = "src/main/resources/network_resources/chaincode_properties/servicechannel.chaincode.properties";
            /*************************************************************************************************/

            String userConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/userid_properties/org1.user1.properties";
            String networkConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";
            String channelConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/channel_properties/supplychannel.properties";

            String channelName = "supplychannelone";
            String chaincodeName = "supplymainone";

            //调用的函数名称
            String funcName = "getCarID";
            //调用合约的参数
            String[] arguments = {"L6T22222222221259"};

            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelName,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);

            Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(chaincodeName, funcName, arguments);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
