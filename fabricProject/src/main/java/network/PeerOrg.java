package network;

import user.UserContext;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.*;

/***一个组织包含的节点集合***/
public class PeerOrg {
    final String name;
    final String mspid;
    private HFCAClient hfcaclient = null;//连接组织CA的客户端
    private UserContext admin = null;//组织管理员

    Map<String, UserContext> userMap = new HashMap<>();
    Map<String, Peer> peerMap = new HashMap<>();
    Set<Peer> peers = new HashSet<>();

    public PeerOrg(String name, String mspid) {
        this.name = name;//组织名
        this.mspid = mspid;
    }

    public String getName(){
        return this.name;
    }

    public String getMspid(){
        return this.mspid;
    }

    public void setAdmin(UserContext admin) {
        this.admin = admin;
    }

    public UserContext getAdmin() {
        return admin;
    }

    public void addUser(UserContext user){
        userMap.put(user.getName(), user);
    }

    public UserContext getUser(String name){
        return this.userMap.get(name);
    }

    public void addPeer(Peer peer){
        peerMap.put(peer.getName(),peer);
        peers.add(peer);
    }

    //需要用到peer的地方都从peerOrg来获取
    public Peer getPeer(String name){
        return peerMap.get(name);
    }

    //获得组织里所有的peer
    public Set<Peer> getOrgPeers(){
        return Collections.unmodifiableSet(peers);
    }

    public void setHfcaclient(HFCAClient hfcaclient) {
        this.hfcaclient = hfcaclient;
    }

    public HFCAClient getHfcaclient(){
        return this.hfcaclient;
    }
}
