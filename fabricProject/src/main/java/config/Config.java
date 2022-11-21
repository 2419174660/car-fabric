package config;

import network.PeerOrg;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.TransactionRequest;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class Config {
    public static final String user_properties_file_path = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/userid_properties";

    //各组织CA服务器默认管理员账户、密码（用于测试用户管理模块，后续删除）
    public static final String CA_1_ADMIN = "admin";
    public static final String CA_1_PASSWORD = "adminpw";
    public static final String CA_2_ADMIN = "admin";
    public static final String CA_2_PASSWORD = "adminpw";
    public static final String CA_3_ADMIN = "admin";
    public static final String CA_3_PASSWORD = "adminpw";
    public static final String CA_4_ADMIN = "admin";
    public static final String CA_4_PASSWORD = "adminpw";
    public static final String CA_5_ADMIN = "admin";
    public static final String CA_5_PASSWORD = "adminpw";

    //channel配置文件地址
    public static final String CHANNEL_1_CONFIG_PATH = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/generate_channel_tx/SupplyChannel.tx";
    public static final String CHANNEL_2_CONFIG_PATH ="/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/generate_channel_tx/MarketingChannel.tx";
    public static final String CHANNEL_3_CONFIG_PATH ="/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/generate_channel_tx/ServiceChannel.tx";

    //链码背书策略文件地址
    public static final String CHAINCODE_POLICY_PATH = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/config/chaincodeendorsementpolicy.yaml";

    //组织管理员证书、密钥文件地址（暂时）后续通过CA服务器生成并保存

    public static final String ORG1_USR_ADMIN_PK = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/priv_sk";
    public static final String ORG1_USR_ADMIN_CERT = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/admincerts/Admin@org1.example.com-cert.pem";
    public static final String ORG2_USR_ADMIN_PK = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/keystore/priv_sk";
    public static final String ORG2_USR_ADMIN_CERT = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/admincerts/Admin@org2.example.com-cert.pem";
    public static final String ORG3_USR_ADMIN_PK = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org3.example.com/users/Admin@org3.example.com/msp/keystore/priv_sk";
    public static final String ORG3_USR_ADMIN_CERT = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org3.example.com/users/Admin@org3.example.com/msp/admincerts/Admin@org3.example.com-cert.pem";
    public static final String ORG4_USR_ADMIN_PK = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org4.example.com/users/Admin@org4.example.com/msp/keystore/priv_sk";
    public static final String ORG4_USR_ADMIN_CERT = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org4.example.com/users/Admin@org4.example.com/msp/admincerts/Admin@org4.example.com-cert.pem";
    public static final String ORG5_USR_ADMIN_PK = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org5.example.com/users/Admin@org5.example.com/msp/keystore/priv_sk";
    public static final String ORG5_USR_ADMIN_CERT = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org5.example.com/users/Admin@org5.example.com/msp/admincerts/Admin@org5.example.com-cert.pem";

    public static final String ORG1_USR_PK = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/keystore/priv_sk";
    public static final String ORG1_USR_CERT = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/crypto-config/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/admincerts/User1@org1.example.com-cert.pem";
    //各组织CA服务器地址
    public static final String CA_ORG1_URL = "";
    public static final String CA_ORG2_URL = "";
    public static final String CA_ORG3_URL = "";

    //组织mspID和组织名
    public static final String ORG1_MSP = "Enterprise01";
    public static final String ORG1 = "Enterprise01";
    public static final String ORG2_MSP = "Enterprise02";
    public static final String ORG2 = "Enterprise02";
    public static final String ORG3_MSP = "Enterprise03";
    public static final String ORG3 = "Enterprise03";
    public static final String ORG4_MSP = "Enterprise04";
    public static final String ORG4 = "Enterprise04";
    public static final String ORG5_MSP = "Enterprise05";
    public static final String ORG5 = "Enterprise05";

    //节点名称与地址
    public static final String ORDERER_NAME = "orderer.example.com";
    public static final String ORDERER_URL = "grpc://localhost:7050";

    public static final String ADMIN_PEER_0 = "peer0.ChannelAdminOrg.example.com";
    public static final String ADMIN_PEER_1 = "peer1.ChannelAdminOrg.example.com";
    public static final String ADMIN_PEER_0_URL = "grpc://localhost:6051";
    public static final String ADMIN_PEER_1_URL = "grpc://localhost:6056";

    public static final String ORG1_PEER_0 = "peer0.org1.example.com";
    public static final String ORG1_PEER_1 = "peer1.org1.example.com";
    public static final String ORG1_PEER_0_URL = "grpc://localhost:7051";
    public static final String ORG1_PEER_1_URL = "grpc://localhost:7056";

    public static final String ORG2_PEER_0 = "peer0.org2.example.com";
    public static final String ORG2_PEER_1 = "peer1.org2.example.com";
    public static final String ORG2_PEER_0_URL = "grpc://localhost:8051";
    public static final String ORG2_PEER_1_URL = "grpc://localhost:8056";

    public static final String ORG3_PEER_0 = "peer0.org3.example.com";
    public static final String ORG3_PEER_1 = "peer1.org3.example.com";
    public static final String ORG3_PEER_0_URL = "grpc://localhost:9051";
    public static final String ORG3_PEER_1_URL = "grpc://localhost:9056";

    public static final String ORG4_PEER_0 = "peer0.org4.example.com";
    public static final String ORG4_PEER_1 = "peer1.org4.example.com";
    public static final String ORG4_PEER_0_URL = "grpc://localhost:10051";
    public static final String ORG4_PEER_1_URL = "grpc://localhost:10056";

    public static final String ORG5_PEER_0 = "peer0.org5.example.com";
    public static final String ORG5_PEER_1 = "peer1.org5.example.com";
    public static final String ORG5_PEER_0_URL = "grpc://localhost:11051";
    public static final String ORG5_PEER_1_URL = "grpc://localhost:11056";

}
