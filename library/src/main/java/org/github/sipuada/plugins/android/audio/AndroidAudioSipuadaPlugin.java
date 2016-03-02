package org.github.sipuada.plugins.android.audio;

import android.content.Context;
import android.gov.nist.gnjvx.sdp.MediaDescriptionImpl;
import android.gov.nist.gnjvx.sdp.fields.*;
import android.javax.sdp.*;
import android.net.rtp.AudioCodec;
import android.util.Pair;
import org.github.sipuada.Constants.RequestMethod;
import org.github.sipuada.UserAgent;
import org.github.sipuada.plugins.android.audio.utils.SipuadaLog;
import org.github.sipuada.plugins.SipuadaPlugin;

import java.util.*;

public class AndroidAudioSipuadaPlugin implements SipuadaPlugin {

	class Record {
		Map<String, SessionDescription> storage = new HashMap<>();
		public Record(SessionDescription offer) {
			storage.put("offer", offer);
		}
		public Record(SessionDescription offer, SessionDescription answer) {
			storage.put("offer", offer);
			storage.put("answer", answer);
		}
		public SessionDescription getOffer() {
			return storage.get("offer");
		}

		public SessionDescription getAnswer() {
			return storage.get("answer");
		}

		public void setAnswer(SessionDescription answer) {
			storage.put("answer", answer);
		}
	}

	public enum CallRole {
		CALLEE,
		CALLER
	}

	private final Map<String, CallRole> roles = new HashMap<>();
	private final Map<String, Record> records = new HashMap<>();
	private SipuadaAudioManager mSipuadaAudioManager;
	private AudioCodec[] myCodecs;
	private ArrayList<Pair<AudioCodec,Integer>> matchCodecs;
	AndroidAudioSipuadaPluginConfig config;
	Map<String, String> sessionsIds = new HashMap<>();

	public AndroidAudioSipuadaPlugin(String username, String localAddress, Context context) {

		mSipuadaAudioManager = new SipuadaAudioManager(context, localAddress);
		mSipuadaAudioManager.setupAudioStream();
		myCodecs = mSipuadaAudioManager.getCodecs();

		config = new AndroidAudioSipuadaPluginConfig();
		config.setUsername(username);
		config.setLocalAddress(localAddress);
	}

	@Override
	public SessionDescription generateOffer(String callId, RequestMethod method) {
		roles.put(callId, CallRole.CALLER);
		try {
			/* This offer start with:
			 * "v" (version) = 0
			 * "s" (session name) = -
			 * "t" (time) = 0
			 */
			SessionDescription offer = SdpFactory.getInstance().createSessionDescription(config.getLocalAddress());

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
			Vector<Object> mediaDescriptions = new Vector<>();

			for (AudioCodec codec : myCodecs) {
				MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();
				AttributeField attributeField = new AttributeField();
				attributeField.setName(SdpConstants.RTPMAP);

				MediaField audioField = new MediaField();
				audioField.setMedia("audio");
				audioField.setPort(mSipuadaAudioManager.getAudioStreamPort());
				audioField.setProtocol(SdpConstants.RTP_AVP);

				Vector<String> audioFormats = new Vector<>();
				audioFormats.add(Integer.toString(codec.type));
				audioField.setFormats(audioFormats);

				attributeField.setValue(Integer.toString(codec.type) + " "+ codec.rtpmap);
				audioDescription.addAttribute(attributeField);

				AttributeField sendReceive = new AttributeField();
				sendReceive.setValue("sendrecv");
				audioDescription.addAttribute(sendReceive);

				AttributeField rtcpAttribute = new AttributeField();
				rtcpAttribute.setName("rtcp");
				rtcpAttribute.setValue(Integer.toString(mSipuadaAudioManager.getAudioStreamPort()));
				audioDescription.addAttribute(rtcpAttribute);

				mediaDescriptions.add(audioField);
				mediaDescriptions.add(audioDescription);
			}

			offer.setOrigin(originField);
			offer.setConnection(connectionField);
			offer.setMediaDescriptions(mediaDescriptions);

			records.put(callId, new Record(offer));
			return offer;

		} catch (SdpException e) {
			SipuadaLog.error("Failed to create offer session description", e);
		}

		return null;
	}

	@Override
	public void receiveAnswerToAcceptedOffer(String callId, SessionDescription answer) {
		Record record = records.get(callId);
		record.setAnswer(answer);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public SessionDescription generateAnswer(String callId, RequestMethod method, SessionDescription offer) {
		roles.put(callId, CallRole.CALLEE);
		try {
			/* This answer start with:
			 * "v" (version) = 0
			 * "s" (session name) = -
			 * "t" (time) = 0
			 */
			SessionDescription answer = SdpFactory.getInstance().createSessionDescription(config.getLocalAddress());

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

			// Check codecs
			matchCodecs = new ArrayList<>();
			// Check if the offer audio format its equal mine
			Vector offerMediaDescriptions = offer.getMediaDescriptions(false);

			if (offerMediaDescriptions != null) {
				for (Object offerMediaDescription : offerMediaDescriptions) {
					int port = ((MediaDescription)offerMediaDescription).getMedia().getMediaPort();
					Vector offerFormats = ((MediaDescriptionImpl)offerMediaDescription).getMediaField().getFormats();

					if (offerFormats != null) {
						for (Object offerFormat : offerFormats) {
							SipuadaLog.verbose("myCodecs size: " + myCodecs.length);
							for (AudioCodec codec : myCodecs) {
								if (offerFormat.equals(Integer.toString(codec.type))) {
									SipuadaLog.verbose("Codec: " + codec.rtpmap);
									SipuadaLog.verbose("Port: " + port);
									matchCodecs.add(new Pair<>(codec,port));
								}
							}
						}
					} else {
						return null;
					}
				}
			} else {
				return null;
			}

			if (matchCodecs.isEmpty()) {
				return null;
			}

			// Media Descriptions ("m=")
			// m=<media> <port> <protocol> <fmt> ...
			// <media> is the media type.  Currently defined media are "audio","video", "text", "application", and "message"
			Vector<Object> mediaDescriptions = new Vector<>();

			for (Pair<AudioCodec,Integer> codecAndPort : matchCodecs) {
				AudioCodec codec = codecAndPort.first;
				MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();
				AttributeField attributeField = new AttributeField();
				attributeField.setName(SdpConstants.RTPMAP);

				MediaField audioField = new MediaField();
				audioField.setMedia("audio");
				audioField.setPort(mSipuadaAudioManager.getAudioStreamPort());
				audioField.setProtocol(SdpConstants.RTP_AVP);

				Vector<String> audioFormats = new Vector<>();
				audioFormats.add(Integer.toString(codec.type));
				audioField.setFormats(audioFormats);

				attributeField.setValue(Integer.toString(codec.type) + " "+ codec.rtpmap);
				audioDescription.addAttribute(attributeField);

				AttributeField sendReceive = new AttributeField();
				sendReceive.setValue("sendrecv");
				audioDescription.addAttribute(sendReceive);

				AttributeField rtcpAttribute = new AttributeField();
				rtcpAttribute.setName("rtcp");
				rtcpAttribute.setValue(Integer.toString(mSipuadaAudioManager.getAudioStreamPort()));
				audioDescription.addAttribute(rtcpAttribute);

				mediaDescriptions.add(audioField);
				mediaDescriptions.add(audioDescription);
			}

			answer.setOrigin(originField);
			answer.setConnection(connectionField);
			answer.setMediaDescriptions(mediaDescriptions);

			records.put(callId, new Record(offer, answer));
			return answer;

		} catch (SdpException e) {
			SipuadaLog.error("Failed to create answer session description", e);
		}
		return null;
	}

	@Override
	public boolean performSessionSetup(String callId, UserAgent userAgent) {
		Record record = records.get(callId);
		SessionDescription offer = record.getOffer();
		SessionDescription answer = record.getAnswer();
		int audioRtpPort = 5060;

		switch (roles.get(callId)) {
			case CALLEE:
				try {
					mSipuadaAudioManager.startStreaming(matchCodecs.get(0).second,offer.getConnection().getAddress(), matchCodecs.get(0).first);
				} catch (SdpParseException e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to start audio streaming.", e);
				}
				return true;
			case CALLER:
				try {
					@SuppressWarnings("unchecked")
					Vector<MediaDescription> answerMediasDescription = answer.getMediaDescriptions(false);

					for (MediaDescription item : answerMediasDescription) {
						if (item.getMedia().getMediaType().contains("audio")) {
							audioRtpPort = item.getMedia().getMediaPort();
						}
					}
				} catch (SdpException e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to extract media from answer.", e);
				}
				try {
					mSipuadaAudioManager.startStreaming(audioRtpPort,answer.getConnection().getAddress(), myCodecs[0]);
				} catch (SdpParseException e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to start audio streaming.", e);
				}
				return true;
		}
		return false;
	}

	@Override
	public boolean performSessionTermination(String callId) {
		mSipuadaAudioManager.stopStreaming();
		records.remove(callId);
		return false;
	}

}
