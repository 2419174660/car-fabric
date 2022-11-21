package client;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.sql.Array;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONObject;
import net.sf.json.JSONArray;

import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import user.UserContext;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import util.Util;


public class FabricClient {
    private HFClient instance;

    public HFClient getInstance() {
        return instance;
    }

    public FabricClient(UserContext context) throws InvalidArgumentException, CryptoException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        instance = HFClient.createNewInstance();
        instance.setCryptoSuite(cryptoSuite);
        instance.setUserContext(context);
    }

    public ChannelClient createChannelClient(Channel channel){
        ChannelClient channelClient = new ChannelClient(this, channel);
        return channelClient;
    }


    //新建通道（系统链码会自动检查是否为admin身份）(管理员组织成员特权，暂未写权限判断)
    public Channel constructChannel(String channelConfigPath) throws InvalidArgumentException, TransactionException, ProposalException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, CertificateException, InvalidKeyException {
        //从配置文件读取通道属性
        Properties channelConfig = new Properties();
        InputStream channelConfigStream = new BufferedInputStream(new FileInputStream(channelConfigPath));
        channelConfig.load(channelConfigStream);
        String channelName = channelConfig.getProperty("channelName");
        //判断该channel是否已经存在
        Channel ch = getChannelFromConfig(channelName);
        if(ch == null){
            String channelTxPath =  channelConfig.getProperty("channelTxPath");
            String ordererName = channelConfig.getProperty("ordererName");
            String ordererUrl = channelConfig.getProperty("ordererUrl");
            ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(channelTxPath));
            byte[] channelConfigurationSignatures = instance.getChannelConfigurationSignature(channelConfiguration, instance.getUserContext());
            Channel channel = instance.newChannel(channelName,instance.newOrderer(ordererName,ordererUrl),
                    channelConfiguration,channelConfigurationSignatures);
            channel.initialize();
            //存储通道
            saveChannelAsConfig(channel);
            return channel;
        }else{
            Logger.getLogger(FabricClient.class.getName()).log(Level.INFO,
                    "channel already exsist!");
            return null;
        }
    }

    //通过gateway获取到当前网络
    //channelConfigPath：通道配置信息路径；networkConfigPath：网络配置信息路径（每个组织的配置信息文件不一样）
    public Channel connectToNetwork(String networkConfigPath, String channelName, String userConfigPath) throws IOException, CertificateException, InvalidKeyException {
        //从通道配置文件读取通道名称
        //Properties channelConfig = new Properties();
        //InputStream channelConfigStream = new BufferedInputStream(new FileInputStream(channelConfigPath));
        //channelConfig.load(channelConfigStream);
        //String channelName = channelConfig.getProperty("channelName");

        //用户配置信息
        Properties userConfig = new Properties();
        InputStream userConfigStream = new BufferedInputStream(new FileInputStream(userConfigPath));
        userConfig.load(userConfigStream);

        //使用用户证书/密钥初始化一个网关wallet账户用于连接网络
        Wallet wallet = Wallets.newInMemoryWallet();
        X509Certificate certificate = Util.readX509Certificate(Paths.get(userConfig.getProperty("userCertPath")));
        PrivateKey privateKey = Util.getPrivateKey(Paths.get(userConfig.getProperty("userPrivkeyPath")));
        wallet.put(userConfig.getProperty("userName"), Identities.newX509Identity(userConfig.getProperty("mspId"),certificate,privateKey));
        //获取fabric网络连接对象
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, userConfig.getProperty("userName"))
                .networkConfig(Paths.get(networkConfigPath));
        //连接网关
        Gateway gateway = builder.connect();
        //获取通道
        Network network = gateway.getNetwork(channelName);
        return network.getChannel();
    }

    //将调用链码查询返回的response进行解析，获得每个节点返回的json结果
    public Map<Peer,JSONArray> getResponsePayload(Collection<ProposalResponse> response) throws InvalidArgumentException {
        Map<Peer,JSONArray> result = new HashMap<>();
        for (ProposalResponse res : response) {
            String str = new String(res.getChaincodeActionResponsePayload());
            JSONArray jsonArray = JSONArray.fromObject(str);
            result.put(res.getPeer(),jsonArray);
            System.out.println("Peer name: "+res.getPeer().getName());
            System.out.println("get return: "+jsonArray);
        }
        return result;
    }


    //将channel存储为本地的block文件
    public String saveChannelAsConfig(Channel channel) throws InvalidArgumentException, IOException {
        String channelName = channel.getName();
        String fileName = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/channel_resources/"+channelName+".block";
        File file = new File(fileName);
        if(file.exists()){//删除文件，新建文件
            file.delete();
        }
        channel.serializeChannel(new File(fileName));
        return fileName;
    }

    //从本地block文件中读取channel
    public Channel getChannelFromConfig(String channelName) throws InvalidArgumentException, IOException, ClassNotFoundException {
        String fileName = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/channel_resources/"+channelName+".block";
        File file = new File(fileName);
        if(file.exists()){
            Channel channel = instance.deSerializeChannel(file);
            return channel;
        }else{
            Logger.getLogger(FabricClient.class.getName()).log(Level.INFO, "channel: "+channelName+" not exist!");
            return null;
        }
    }



}
