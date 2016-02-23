package org.github.sipuada.plugin.android.audio;

import android.gov.nist.gnjvx.sdp.MediaDescriptionImpl;
import android.gov.nist.gnjvx.sdp.fields.*;
import android.javax.sdp.SdpConstants;
import android.javax.sdp.SdpException;
import android.javax.sdp.SdpFactory;
import android.javax.sdp.SessionDescription;
import org.github.sipuada.Constants.RequestMethod;
import org.github.sipuada.UserAgent;
import org.github.sipuada.plugins.SipuadaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class AndroidAudioSipuadaPlugin implements SipuadaPlugin {

	AndroidAudioSipuadaPluginConfig config;
	// Map<CallId, SessionID>
	Map<String, String> sessionsIds = new HashMap<>();

	public AndroidAudioSipuadaPlugin(String username, String localAddress) {
		config = new AndroidAudioSipuadaPluginConfig();
		config.setUsername(username);
		config.setLocalAddress(localAddress);
	}

	@Override
	public SessionDescription generateOffer(String callId, RequestMethod method) {
		try {
			/* This sdp start with:
			 * "v" (version) = 0
			 * "s" (session name) = -
			 * "t" (time) = 0
			 */
			SessionDescription sdp = SdpFactory.getInstance().createSessionDescription(config.getLocalAddress());

			// Origin ("o")
			// o=<user name> <sess-id> <sess-version> <net type> <addr type> <unicast-address> 
			OriginField originField = new OriginField();
			originField.setUsername(config.getUsername());
			String sessionId = Long.toString(System.currentTimeMillis() / 1000L);
			sessionsIds.put(callId,sessionId);
			originField.setSessionId(sessionId);
			originField.setSessVersion(0L);
			originField.setNetworkType(config.getNetworkType());
			originField.setAddressType(config.getAddressType());
			originField.setAddress(config.getLocalAddress());

			// Connection ("c")
			// c=<net type> <addr type> <connection-address>
			ConnectionField connectionField = new ConnectionField();
			connectionField.setNetworkType(config.getNetworkType());
			connectionField.setAddressType(config.getAddressType());
			connectionField.setAddress(config.getLocalAddress());

			// Media Descriptions ("m=")
			// m=<media> <port> <protocol> <fmt> ...
			// <media> is the media type.  Currently defined media are "audio","video", "text", "application", and "message"
			MediaField audioField = new MediaField();
			audioField.setMedia("audio");

			// TODO generate RTP port
			// audioField.setPort();

			audioField.setProtocol(SdpConstants.RTP_AVP);

			Vector<String> audioFormats = new Vector<>();
			audioFormats.add(Integer.toString(SdpConstants.PCMA));
			audioField.setFormats(audioFormats);

			Vector<Object> mediaDescriptions = new Vector<>();
			MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();

			AttributeField attributeField = new AttributeField();
			attributeField.setName(SdpConstants.RTPMAP);
			attributeField.setValue(SdpConstants.PCMA + " PCMA/8000");
			audioDescription.addAttribute(attributeField);

			AttributeField sendReceive = new AttributeField();
			sendReceive.setValue("sendrecv");
			audioDescription.addAttribute(sendReceive);

//			AttributeField rtcpAttribute = new AttributeField();
//			rtcpAttribute.setName("rtcp");
			// TODO generate RTCP port
			// rtcpAttribute.setValue();
//			audioDescription.addAttribute(rtcpAttribute);

			mediaDescriptions.add(audioField);
			mediaDescriptions.add(audioDescription);

			sdp.setOrigin(originField);
			sdp.setConnection(connectionField);
			sdp.setMediaDescriptions(mediaDescriptions);

			return sdp;

		} catch (SdpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void receiveAnswerToAcceptedOffer(String callId, SessionDescription answer) {
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	public SessionDescription generateAnswer(String callId, RequestMethod method, SessionDescription offer) {
		// TODO Auto-generated method stub
		try {
			/* This sdp start with:
			 * "v" (version) = 0
			 * "s" (session name) = -
			 * "t" (time) = 0
			 */
			SessionDescription sdp = SdpFactory.getInstance().createSessionDescription(config.getLocalAddress());

			SessionNameField sessionNameField = new SessionNameField();
			sessionNameField.setSessionName(offer.getSessionName().getValue());

			// Origin ("o")
			// o=<user name> <sess-id> <sess-version> <net type> <addr type> <unicast-address> 
			OriginField originField = new OriginField();
			originField.setUsername(config.getUsername());
			originField.setSessionId(offer.getOrigin().getSessionId());
			originField.setSessVersion(offer.getOrigin().getSessionVersion());
			originField.setNetworkType(config.getNetworkType());
			originField.setAddressType(config.getAddressType());
			originField.setAddress(config.getLocalAddress());

			// Connection ("c")
			// c=<net type> <addr type> <connection-address>
			ConnectionField connectionField = new ConnectionField();
			connectionField.setNetworkType(config.getNetworkType());
			connectionField.setAddressType(config.getAddressType());
			connectionField.setAddress(config.getLocalAddress());

			// Media Descriptions ("m=")
			// m=<media> <port> <protocol> <fmt> ...
			// <media> is the media type.  Currently defined media are "audio","video", "text", "application", and "message"
			MediaField audioField = new MediaField();
			audioField.setMedia("audio");

			// TODO generate RTP port
			// audioField.setPort();

			audioField.setProtocol(SdpConstants.RTP_AVP);

			// Check if the offer audio format its equal mine
			Vector offerMediaDescriptions = offer.getMediaDescriptions(false);
			if (offerMediaDescriptions != null) {
				Iterator itMediaDescriptions = offerMediaDescriptions.iterator();

				while (itMediaDescriptions.hasNext()) {
					MediaDescriptionImpl offerMediaDescription = (MediaDescriptionImpl) itMediaDescriptions.next();
					Vector offerFormats = offerMediaDescription.getMediaField().getFormats();

					if (offerFormats != null) {
						Iterator itFormats = offerFormats.iterator();
						while (itFormats.hasNext()) {
							Object offerFormat = itFormats.next();
							if (offerFormat != null) {
								if (!(Integer.parseInt(((String) offerFormat)) == SdpConstants.PCMA));
								return null;
							}
						}
					}
				}
			}

			Vector<String> audioFormats = new Vector<>();
			audioFormats.add(Integer.toString(SdpConstants.PCMA));
			audioField.setFormats(audioFormats);

			Vector<Object> mediaDescriptions = new Vector<>();
			MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();

			AttributeField attributeField = new AttributeField();
			attributeField.setName(SdpConstants.RTPMAP);
			attributeField.setValue(SdpConstants.PCMA + " PCMA/8000");
			audioDescription.addAttribute(attributeField);

			AttributeField sendReceive = new AttributeField();
			sendReceive.setValue("sendrecv");
			audioDescription.addAttribute(sendReceive);

//			AttributeField rtcpAttribute = new AttributeField();
//			rtcpAttribute.setName("rtcp");
			// TODO generate RTCP port
			// rtcpAttribute.setValue();
//			audioDescription.addAttribute(rtcpAttribute);

			mediaDescriptions.add(audioField);
			mediaDescriptions.add(audioDescription);

			sdp.setOrigin(originField);
			sdp.setConnection(connectionField);
			sdp.setMediaDescriptions(mediaDescriptions);

			return sdp;

		} catch (SdpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean performSessionSetup(String callId, UserAgent userAgent) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean performSessionTermination(String callId) {
		// TODO Auto-generated method stub
		return false;
	}

}
