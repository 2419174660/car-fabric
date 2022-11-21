package user;

import java.io.Serializable;
import java.util.Set;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

public class UserContext implements User, Serializable {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected Set<String> roles;
    protected String account;
    protected String affiliation;
    protected Enrollment enrollment;
    protected String mspId;//可以获取用户所在peerorg的信息

    public void setName(String name) {
        this.name = name;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getAccount() {
        return account;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public String getMspId() {
        return mspId;
    }
}
