package hzhl.net.hlcall.entity;

import org.linphone.core.AVPFMode;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.ErrorInfo;
import org.linphone.core.NatPolicy;
import org.linphone.core.ProxyConfig;
import org.linphone.core.Reason;
import org.linphone.core.RegistrationState;

import java.io.Serializable;

public class ProxyConfigEntity implements ProxyConfig,Serializable {
    private static final long serialVersionUID = 8111169637077096815L;
    private boolean isChoose;

    public boolean getIsChoose() {
        return isChoose;
    }

    public void setIsChoose(boolean choose) {
        isChoose = choose;
    }

    @Override
    public boolean avpfEnabled() {
        return false;
    }

    @Override
    public AVPFMode getAvpfMode() {
        return null;
    }

    @Override
    public void setAvpfMode(AVPFMode avpfMode) {

    }

    @Override
    public int getAvpfRrInterval() {
        return 0;
    }

    @Override
    public void setAvpfRrInterval(int i) {

    }

    @Override
    public String getConferenceFactoryUri() {
        return null;
    }

    @Override
    public void setConferenceFactoryUri(String s) {

    }

    @Override
    public Address getContact() {
        return null;
    }

    @Override
    public String getContactParameters() {
        return null;
    }

    @Override
    public void setContactParameters(String s) {

    }

    @Override
    public String getContactUriParameters() {
        return null;
    }

    @Override
    public void setContactUriParameters(String s) {

    }

    @Override
    public Core getCore() {
        return null;
    }

    @Override
    public ProxyConfig getDependency() {
        return null;
    }

    @Override
    public void setDependency(ProxyConfig proxyConfig) {

    }

    @Override
    public boolean getDialEscapePlus() {
        return false;
    }

    @Override
    public void setDialEscapePlus(boolean b) {

    }

    @Override
    public String getDialPrefix() {
        return null;
    }

    @Override
    public void setDialPrefix(String s) {

    }

    @Override
    public String getDomain() {
        return null;
    }

    @Override
    public Reason getError() {
        return null;
    }

    @Override
    public ErrorInfo getErrorInfo() {
        return null;
    }

    @Override
    public int getExpires() {
        return 0;
    }

    @Override
    public void setExpires(int i) {

    }

    @Override
    public Address getIdentityAddress() {
        return null;
    }

    @Override
    public int setIdentityAddress(Address address) {
        return 0;
    }


    @Override
    public String getIdkey() {
        return null;
    }

    @Override
    public void setIdkey(String s) {

    }

    @Override
    public boolean isPushNotificationAllowed() {
        return false;
    }

    @Override
    public NatPolicy getNatPolicy() {
        return null;
    }

    @Override
    public void setNatPolicy(NatPolicy natPolicy) {

    }

    @Override
    public int getPrivacy() {
        return 0;
    }

    @Override
    public void setPrivacy(int i) {

    }

    @Override
    public boolean publishEnabled() {
        return false;
    }

    @Override
    public void enablePublish(boolean b) {

    }

    @Override
    public int getPublishExpires() {
        return 0;
    }

    @Override
    public void setPublishExpires(int i) {

    }

    @Override
    public void setPushNotificationAllowed(boolean b) {

    }

    @Override
    public String getQualityReportingCollector() {
        return null;
    }

    @Override
    public void setQualityReportingCollector(String s) {

    }

    @Override
    public boolean qualityReportingEnabled() {
        return false;
    }

    @Override
    public void enableQualityReporting(boolean b) {

    }

    @Override
    public int getQualityReportingInterval() {
        return 0;
    }

    @Override
    public void setQualityReportingInterval(int i) {

    }

    @Override
    public String getRealm() {
        return null;
    }

    @Override
    public void setRealm(String s) {

    }

    @Override
    public String getRefKey() {
        return null;
    }

    @Override
    public void setRefKey(String s) {

    }

    @Override
    public boolean registerEnabled() {
        return false;
    }

    @Override
    public void enableRegister(boolean b) {

    }

    @Override
    public String getRoute() {
        return null;
    }

    @Override
    public int setRoute(String s) {
        return 0;
    }


    @Override
    public String[] getRoutes() {
        return new String[0];
    }

    @Override
    public int setRoutes(String[] strings) {
        return 0;
    }


    @Override
    public String getServerAddr() {
        return null;
    }

    @Override
    public int setServerAddr(String s) {
        return 0;
    }


    @Override
    public RegistrationState getState() {
        return null;
    }

    @Override
    public String getTransport() {
        return null;
    }

    @Override
    public int getUnreadChatMessageCount() {
        return 0;
    }

    @Override
    public int done() {
        return 0;
    }


    @Override
    public void edit() {

    }

    @Override
    public AuthInfo findAuthInfo() {
        return null;
    }

    @Override
    public String getCustomHeader(String s) {
        return null;
    }

    @Override
    public boolean isPhoneNumber(String s) {
        return false;
    }

    @Override
    public String normalizePhoneNumber(String s) {
        return null;
    }

    @Override
    public Address normalizeSipUri(String s) {
        return null;
    }

    @Override
    public void pauseRegister() {

    }

    @Override
    public void refreshRegister() {

    }

    @Override
    public void setCustomHeader(String s, String s1) {

    }

    @Override
    public void setUserData(Object o) {

    }

    @Override
    public Object getUserData() {
        return null;
    }
}
