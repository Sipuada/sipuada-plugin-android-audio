package org.github.sipuada.plugin.android.audio;

import android.gov.nist.gnjvx.sdp.fields.SDPKeywords;

public class AndroidAudioSipuadaPluginConfig {

	private String mUsername = "anonymous";
	private String mLocalAddress = "127.0.0.1";
	private String mAddressType = SDPKeywords.IPV4;
	private String mNetworkType = SDPKeywords.IN;

	public AndroidAudioSipuadaPluginConfig() {}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String username) {
		mUsername = username;
	}
	
	public String getLocalAddress() {
		return mLocalAddress;
	}

	public void setLocalAddress(String localAddress) {
		mLocalAddress = localAddress;
	}

	public String getAddressType() {
		return mAddressType;
	}

	public void setAddressType(String addressType) {
		mAddressType = addressType;
	}

	public String getNetworkType() {
		return mNetworkType;
	}

	public void setNetworkType(String networkType) {
		mNetworkType = networkType;
	}

}
