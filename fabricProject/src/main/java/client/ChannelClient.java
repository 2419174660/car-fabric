package client;

import com.alibaba.fastjson.JSONObject;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import user.UserContext;
import util.Util;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

//ChannelClient是与channel绑定的fabricClient，方便进行与channel相关的操作
public class ChannelClient {
    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";

    private FabricClient fabricClient;
    private Channel channel;

    public ChannelClient(FabricClient fabricclient,Channel channel){
        fabricClient = fabricclient;
        this.channel = channel;
    }

    //向通道加入节点（仅能由本组织管理员将本组织节点加入通道）
    public Channel joinPeerToChannel(String peerName,String peerUrl) throws InvalidArgumentException, ProposalException {
        channel.joinPeer(fabricClient.getInstance().newPeer(peerName, peerUrl));
        return channel;
    }

    //通道管理员删除节点
    public Channel removePeerFromChannel(String peerName) throws InvalidArgumentException {
        Collection<Peer> peerCollection = channel.getPeers();
        Iterator<Peer> it = peerCollection.iterator();
        while (it.hasNext()){
            Peer p = it.next();
            if(p.getName().equals(peerName)){
                channel.removePeer(p);
                return channel;
            }
        }
        Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,"no peer names "+peerName+", remove failed.");
        return channel;
    }

    //管理员向channel添加节点集合
    public Channel addPeersToChannel(Collection<Peer> peers) throws InvalidArgumentException, ProposalException, IOException, ClassNotFoundException {
        if(channel.getName() == null){
            Logger.getLogger(FabricClient.class.getName()).log(Level.INFO, "can not add peer to an unexsits channel ");
        }else{
            Collection<Peer> channelPeers = channel.getPeers();
            Iterator it = peers.iterator();
            while(it.hasNext()){
                Peer p = (Peer)it.next();
                //判断是否已经加入了通道
                if(!channelPeers.contains(p)){
                    channel.joinPeer(p);
                }
            }
        }
        return channel;
    }

    //管理员向channel删除节点集合
    public Channel removePeersFromChannel(Collection<String> peerNames) throws InvalidArgumentException, IOException {
        if(channel.getName() == null){
            Logger.getLogger(FabricClient.class.getName()).log(Level.INFO, "can not add peer to an unexsits channel ");
        }else{
            Collection<Peer> channelPeers = channel.getPeers();
            Iterator it = channelPeers.iterator();
            while(it.hasNext()){
                Peer p = (Peer) it.next();
                if(peerNames.contains(p.getName())){
                    channel.removePeer(p);
                }
            }
        }
        return channel;
    }


    //安装链码(安装者必须为组织的admin且只能安装本组织节点)
    public Collection<ProposalResponse> deployChainCode(Collection<Peer> peers, String chaincodeConfigPath)
            throws InvalidArgumentException, ProposalException, ChaincodeEndorsementPolicyParseException, IOException {
        try{
            Properties chaincodeConfig = new Properties();
            InputStream chaincodeConfigStream = new BufferedInputStream(new FileInputStream(chaincodeConfigPath));
            System.out.println("chaincodeConfigPath: "+chaincodeConfigPath);
            chaincodeConfig.load(chaincodeConfigStream);
            String chaincodeName = chaincodeConfig.getProperty("chaincodeName");
            String chaincodeSource = chaincodeConfig.getProperty("chaincodeSource");
            String chaincodePath = chaincodeConfig.getProperty("chaincodePath");
            System.out.println("chaincodePath: "+chaincodePath);
            String version = chaincodeConfig.getProperty("version");
            String policyPath = chaincodeConfig.getProperty("policyPath");

            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();
            InstallProposalRequest request = fabricClient.getInstance().newInstallProposalRequest();
            System.out.println("chaincodeName: "+chaincodeName);
            ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(version)
                    .setPath(chaincodePath);
            ChaincodeID chaincodeID = chaincodeIDBuilder.build();
            request.setChaincodeID(chaincodeID);
            request.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            request.setUserContext(fabricClient.getInstance().getUserContext());
            request.setChaincodeSourceLocation(new File(chaincodeSource));
            request.setChaincodeVersion(version);
            //设置链码的背书策略
            if (policyPath != null) {
                ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
                chaincodeEndorsementPolicy.fromYamlFile(new File(policyPath));
                request.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
            }
            Collection<ProposalResponse> responses = fabricClient.getInstance().sendInstallProposal(request, peers);
            //收集安装成功的response（安装者权限不够将安装失败）
            for (ProposalResponse res : responses) {
                if (res.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(res);
                } else {
                    failed.add(res);
                }
            }
            channel.sendTransaction(successful);

            return responses;
        }catch(Exception ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    //初始化chaincode
    public Collection<ProposalResponse> instantiateChainCode(Collection<Peer> peers,String chaincodeConfigPath)
            throws InvalidArgumentException, ProposalException, ChaincodeEndorsementPolicyParseException, IOException {
        try{
            Properties chaincodeConfig = new Properties();
            InputStream chaincodeConfigStream = new BufferedInputStream(new FileInputStream(chaincodeConfigPath));
            chaincodeConfig.load(chaincodeConfigStream);
            String chaincodeName = chaincodeConfig.getProperty("chaincodeName");
            String chaincodePath = chaincodeConfig.getProperty("chaincodePath");
            String version = chaincodeConfig.getProperty("version");
            String policyPath = chaincodeConfig.getProperty("policyPath");
            String functionName = chaincodeConfig.getProperty("initFunc");
            String stringArgs = chaincodeConfig.getProperty("initArgs");
            //将string转化为string[]
            String[] functionArgs = {};
            if(!stringArgs.equals("{}")){
                stringArgs = stringArgs.substring(1,stringArgs.length()-1);
                functionArgs = stringArgs.split(",");
            }

            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();
            InstantiateProposalRequest instantiateProposalRequest = fabricClient.getInstance().newInstantiationProposalRequest();
            instantiateProposalRequest.setProposalWaitTime(180000);
            ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(version).setPath(chaincodePath);
            ChaincodeID ccid = chaincodeIDBuilder.build();

            instantiateProposalRequest.setChaincodeID(ccid);
            instantiateProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            instantiateProposalRequest.setFcn(functionName);
            instantiateProposalRequest.setArgs(functionArgs);
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
            tm.put("result", ":)".getBytes(UTF_8));
            instantiateProposalRequest.setTransientMap(tm);
            //设置链码的背书策略
            if (policyPath != null) {
                ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
                chaincodeEndorsementPolicy.fromYamlFile(new File(policyPath));
                instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
            }
            Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateProposalRequest, peers);
            //收集初始化成功的response
            for (ProposalResponse res : responses) {
                if (res.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(res);
                } else {
                    failed.add(res);
                }
            }

            channel.sendTransaction(successful);
            return successful;

        }catch (Exception ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    //chaincode执行查询操作
    public Collection<ProposalResponse> queryByChainCode(String chaincodeName, String functionName, String[] args)
            throws InvalidArgumentException, ProposalException {

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        QueryByChaincodeRequest request = this.fabricClient.getInstance().newQueryProposalRequest();
        ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincodeName).build();
        request.setChaincodeID(ccid);
        request.setFcn(functionName);
        if (args != null)
            request.setArgs(args);

        System.out.println("this.channel:"+this.channel);
        System.out.println("request:"+request);
        Collection<ProposalResponse> responses = this.channel.queryByChaincode(request);
        for (ProposalResponse res : responses) {
            if (res.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(res);
            } else {
                failed.add(res);
            }
        }

        return successful;
    }

    //发送交易请求
    public Collection<ProposalResponse> sendTransactionProposal(String chaincodeName,String functionName,String[] functionArgs)
            throws ProposalException, InvalidArgumentException {
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        TransactionProposalRequest request = fabricClient.getInstance().newTransactionProposalRequest();
        ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincodeName).build();
        request.setChaincodeID(ccid);
        request.setFcn(functionName);
        request.setArgs(functionArgs);
        request.setProposalWaitTime(1000);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));
        tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
        request.setTransientMap(tm2);

        Collection<ProposalResponse> response = channel.sendTransactionProposal(request, channel.getPeers());
        for (ProposalResponse pres : response){
            if (pres.getStatus() == ProposalResponse.Status.SUCCESS){
                successful.add(pres);
            } else {
                failed.add(pres);
            }
        }
        if(!successful.equals(null)){
            channel.sendTransaction(successful);
            for (ProposalResponse pres : successful) {
                String stringResponse = new String(pres.getChaincodeActionResponsePayload());
                Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
                        "Transaction proposal on channel " + channel.getName() + " " + pres.getMessage() + " "
                                + pres.getStatus() + " with transaction id:" + pres.getTransactionID());
                Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,stringResponse);
            }
        }


        return successful;
    }

    //从peer上获取具体交易的信息
    public TransactionInfo queryByTransactionId(String txnId) throws ProposalException, InvalidArgumentException {

        Collection<Peer> peers = channel.getPeers();
        for (Peer peer : peers) {
            TransactionInfo info = channel.queryTransactionByID(peer, txnId);
            return info;
        }
        return null;
    }

    ////(管理员组织特权，暂未写权限判断)将组织动态加入通道
    public Channel addOrgToChannel(String orgConfigPath, Collection<UserContext> orgAdminList, UserContext ordererAdmin) throws InvalidArgumentException, TransactionException, IOException {
        // 获取通道配置字节码
        byte[] channelConfig = channel.getChannelConfigurationBytes();

        // 将通道字节码转换为json
        JSONObject channelConfigJson = Util.getDecodeData(channelConfig);

        // 读取新组织的json内容
        String newOrgJson = Util.getDataFromFile(orgConfigPath);
        JSONObject newOrgJsonObject = JSONObject.parseObject(newOrgJson);

        // 将新组织的内容和通道当前的内容合并
        channelConfigJson.getJSONObject("channel_group").getJSONObject("groups").getJSONObject("Application").getJSONObject("groups").put("Org3MSP", newOrgJsonObject);

        // 将合并之后的通道内容转为字节码
        byte[] newConfigBytes = Util.getEncodeData(channelConfigJson.toJSONString());

        // 使用原始通道字节码和新配置的通道字节码，进行对比计算，获得差异字节码
        byte[] updateBytes = Util.updateFromConfigs(channelConfig, newConfigBytes, channel.getName());

        // 使用转码的字节，构建更新通道的对象
        UpdateChannelConfiguration updateChannelConfiguration = new UpdateChannelConfiguration(updateBytes);

        //构造通道内所有组织管理员签名数组
        Iterator<UserContext> it = orgAdminList.iterator();
        int i = 0;
        byte[][] orgSigns = new byte[orgAdminList.size()+1][];
        while(it.hasNext()){
            byte[] sign = fabricClient.getInstance().getUpdateChannelConfigurationSignature(updateChannelConfiguration, it.next());
            orgSigns[i] = sign;
            i++;
        }

        // 发送给区块链，进行配置修改
        fabricClient.getInstance().setUserContext(ordererAdmin);
        channel.updateChannelConfiguration(updateChannelConfiguration, orgSigns);

        return channel;
        
    }

}
