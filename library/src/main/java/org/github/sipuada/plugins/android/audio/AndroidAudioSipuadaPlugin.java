package org.github.sipuada.plugins.android.audio;

import android.content.Context;
import android.gov.nist.gnjvx.sdp.MediaDescriptionImpl;
import android.gov.nist.gnjvx.sdp.fields.*;
import android.javax.sdp.*;
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
	private String mUsername;
	private String mAddressType = SDPKeywords.IPV4;
	private String mNetworkType = SDPKeywords.IN;
	private AudioManager mSipuadaAudioManager;
	private SipuadaAudioCodec[] myCodecs;
	private ArrayList<Pair<SipuadaAudioCodec,Integer>> matchCodecs;
	Map<String, String> sessionsIds = new HashMap<>();

	public AndroidAudioSipuadaPlugin(String username, Context context) {

		mSipuadaAudioManager = new AudioManager(context);
		myCodecs = mSipuadaAudioManager.getCodecs();
		mUsername = username;
	}

	@Override
	public SessionDescription generateOffer(String callId, RequestMethod method, String localAddress) {
		roles.put(callId, CallRole.CALLER);
		try {
			/* This offer start with:
			 * "v" (version) = 0
			 * "s" (session name) = -
			 * "t" (time) = 0
			 */
			SessionDescription offer = SdpFactory.getInstance().createSessionDescription(localAddress);

			// Origin ("o")
			// o=<user name> <sess-id> <sess-version> <net type> <addr type> <unicast-address> 
			OriginField originField = new OriginField();
			originField.setUsername(mUsername);
			String sessionId = Long.toString(System.currentTimeMillis() / 1000L);
			sessionsIds.put(callId,sessionId);
			originField.setSessionId(sessionId);
			originField.setSessVersion(0L);
			originField.setNetworkType(mNetworkType);
			originField.setAddressType(mAddressType);
			originField.setAddress(localAddress);

			// Connection ("c")
			// c=<net type> <addr type> <connection-address>
			ConnectionField connectionField = new ConnectionField();
			connectionField.setNetworkType(mNetworkType);
			connectionField.setAddressType(mAddressType);
			connectionField.setAddress(localAddress);

			// Media Descriptions ("m=")
			// m=<media> <port> <protocol> <fmt> ...
			// <media> is the media type.  Currently defined media are "audio","video", "text", "application", and "message"
			Vector<Object> mediaDescriptions = new Vector<>();

			for (SipuadaAudioCodec codec : myCodecs) {
				MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();

				MediaField audioField = new MediaField();
				audioField.setMedia("audio");
				//TODO TRATAMENTO DE PORTA = 0
				int port = mSipuadaAudioManager.getAudioStreamPort();
				SipuadaLog.verbose("Codec: " + codec.rtpmap + ", " + port);
				audioField.setPort(port);
				audioField.setProtocol(SdpConstants.RTP_AVP);
				audioField.setMediaType(Integer.toString(codec.type));

				Vector<String> audioFormats = new Vector<>();
				audioFormats.add(Integer.toString(codec.type));
				audioField.setFormats(audioFormats);

				AttributeField attributeField = new AttributeField();
				attributeField.setName(SdpConstants.RTPMAP);
				attributeField.setValue(Integer.toString(codec.type) + " " + codec.rtpmap);
				audioDescription.addAttribute(attributeField);

				AttributeField sendReceive = new AttributeField();
				sendReceive.setValue("sendrecv");
				audioDescription.addAttribute(sendReceive);

				AttributeField rtcpAttribute = new AttributeField();
				rtcpAttribute.setName("rtcp");
				rtcpAttribute.setValue(Integer.toString(port + 1));
				audioDescription.addAttribute(rtcpAttribute);

				audioDescription.setMediaField(audioField);
				mediaDescriptions.add(audioDescription);
			}

			offer.setOrigin(originField);
			offer.setConnection(connectionField);
			offer.setMediaDescriptions(mediaDescriptions);

			records.put(callId, new Record(offer));
			SipuadaLog.verbose("Generation Offer");

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
	public SessionDescription generateAnswer(String callId, RequestMethod method, SessionDescription offer, String localAddress) {
		roles.put(callId, CallRole.CALLEE);
		try {
			/* This answer start with:
			 * "v" (version) = 0
			 * "s" (session name) = -
			 * "t" (time) = 0
			 */
			SessionDescription answer = SdpFactory.getInstance().createSessionDescription(localAddress);

			SessionNameField sessionNameField = new SessionNameField();
			sessionNameField.setSessionName(offer.getSessionName().getValue());

			// Origin ("o")
			// o=<user name> <sess-id> <sess-version> <net type> <addr type> <unicast-address> 
			OriginField originField = new OriginField();
			originField.setUsername(mUsername);
			originField.setSessionId(offer.getOrigin().getSessionId());
			originField.setSessVersion(offer.getOrigin().getSessionVersion());
			originField.setNetworkType(mNetworkType);
			originField.setAddressType(mAddressType);
			originField.setAddress(localAddress);

			// Connection ("c")
			// c=<net type> <addr type> <connection-address>
			ConnectionField connectionField = new ConnectionField();
			connectionField.setNetworkType(mNetworkType);
			connectionField.setAddressType(mAddressType);
			connectionField.setAddress(localAddress);

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
							for (SipuadaAudioCodec codec : myCodecs) {
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

			for (Pair<SipuadaAudioCodec,Integer> codecAndPort : matchCodecs) {
				SipuadaAudioCodec codec = codecAndPort.first;
				MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();
				AttributeField attributeField = new AttributeField();
				attributeField.setName(SdpConstants.RTPMAP);

				MediaField audioField = new MediaField();
				audioField.setMedia("audio");
				//TODO TRATAMENTO DE PORTA = 0
				int port = mSipuadaAudioManager.getAudioStreamPort();
				SipuadaLog.verbose("Codec: " + codec.rtpmap + ", " + port);
				audioField.setPort(port);
				audioField.setProtocol(SdpConstants.RTP_AVP);
				audioField.setMediaType(Integer.toString(codec.type));

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
				rtcpAttribute.setValue(Integer.toString(port + 1));
				audioDescription.addAttribute(rtcpAttribute);

				audioDescription.setMediaField(audioField);
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
		SipuadaLog.verbose("PerformSessionSetup");
		Record record = records.get(callId);
		SessionDescription offer = record.getOffer();
		SessionDescription answer = record.getAnswer();

		switch (roles.get(callId)) {
			case CALLEE:
//				for (Pair<SipuadaAudioCodec, Integer> codec : matchCodecs) {
//					if ("SPEEX/8000".equals(codec.first.rtpmap)) {
//						audioRtpPort = codec.second;
//						codecType = codec.first.type;
//					}
//				}

				ArrayList<Pair<Integer,Integer>> answerCodecsCallee = new ArrayList<>();

//				try {
//					@SuppressWarnings("unchecked")
//					Vector<MediaDescription> answerMediasDescription = answer.getMediaDescriptions(false);
//					SipuadaLog.verbose("size: " + answerMediasDescription.size());
//					for (MediaDescription item : answerMediasDescription) {
//						answerCodecsCallee.add(new Pair<>(Integer.parseInt(item.getMedia().getMediaType()), item.getMedia().getMediaPort()));
//					}
//					for (Pair<Integer,Integer> a : answerCodecsCallee) {
//						SipuadaLog.verbose("Resposta: Codec: " + a.first + ", porta: " + a.second);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//					SipuadaLog.error("Failed to extract media from answer.", e);
//				}


				try {
					//ArrayList<Pair<Type,Port>>
					Vector offerMediaDescriptions = answer.getMediaDescriptions(false);
					for (Object offerMediaDescription : offerMediaDescriptions) {
						if (offerMediaDescription instanceof MediaField) {
							answerCodecsCallee.add(new Pair<>(Integer.parseInt(((MediaField) offerMediaDescription).getMediaType()),((MediaField) offerMediaDescription).getMediaPort()));
						}
					}
					for (Pair<Integer,Integer> a : answerCodecsCallee) {
						SipuadaLog.verbose("Oferta: Codec: " + a.first + ", porta: " + a.second);
					}
				} catch (Exception e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to extract media from answer.", e);
				}


				Map<String, String> calleeProperties = new HashMap<>();
				calleeProperties.put(AudioManager.RATE, "8000");

//				try {
//					@SuppressWarnings("unchecked")
//					Vector<MediaDescription> answerMediasDescription = answer.getMediaDescriptions(false);
//					for (MediaDescription item : answerMediasDescription) {
//						SipuadaLog.verbose(item.getMedia().getMediaType());
//						if (item.getMedia().getMediaType().contains("SPEEX")) {
//							localAudioPort = item.getMedia().getMediaPort();
//						}
//					}
//				} catch (SdpException e) {
//					e.printStackTrace();
//					SipuadaLog.error("Failed to extract media from answer.", e);
//				}

				try {
					mSipuadaAudioManager.startVOIPStreaming(matchCodecs.get(1).second, offer.getConnection().getAddress(), answerCodecsCallee.get(1).second, answerCodecsCallee.get(1).first, calleeProperties);
				} catch (SdpParseException e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to start audio streaming.", e);
				}
				return true;

			case CALLER:
				//ArrayList<Pair<Type,Port>>
				ArrayList<Pair<Integer,Integer>> offerCodecs = new ArrayList<>();
				ArrayList<Pair<Integer,Integer>> answerCodecs = new ArrayList<>();
				try {
					//ArrayList<Pair<Type,Port>>
					Vector offerMediaDescriptions = offer.getMediaDescriptions(false);
					for (Object offerMediaDescription : offerMediaDescriptions) {
						if (offerMediaDescription instanceof MediaField) {
							offerCodecs.add(new Pair<>(Integer.parseInt(((MediaField) offerMediaDescription).getMediaType()),((MediaField) offerMediaDescription).getMediaPort()));
						}

						if (offerMediaDescription instanceof MediaDescriptionImpl) {
							SipuadaLog.verbose(">>>>>: " + ((MediaDescriptionImpl) offerMediaDescription).getMediaField().getMedia() + ", " + ((MediaDescriptionImpl) offerMediaDescription).getMediaField().getMediaType());
						}
					}
					for (Pair<Integer,Integer> a : offerCodecs) {
						SipuadaLog.verbose("Oferta: Codec: " + a.first + ", porta: " + a.second);
					}
				} catch (Exception e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to extract media from answer.", e);
				}

				try {
					@SuppressWarnings("unchecked")
					Vector<MediaDescription> answerMediasDescription = answer.getMediaDescriptions(false);
					SipuadaLog.verbose("size: " + answerMediasDescription.size());
					for (MediaDescription item : answerMediasDescription) {
						answerCodecs.add(new Pair<>(Integer.parseInt(item.getMedia().getMediaType()), item.getMedia().getMediaPort()));
					}
					for (Pair<Integer,Integer> a : answerCodecs) {
						SipuadaLog.verbose("Resposta: Codec: " + a.first + ", porta: " + a.second);
					}
				} catch (Exception e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to extract media from answer.", e);
				}

				//TODO TEMP MOCK
				Map<String, String> callerProperties = new HashMap<>();
				callerProperties.put(AudioManager.RATE,"8000");

				try {
					//int remoteRtpPort, String remoteIp, int localPort, int codecPayloadType, Map<String, String> properties
					mSipuadaAudioManager.startVOIPStreaming(answerCodecs.get(1).second, answer.getConnection().getAddress(), offerCodecs.get(1).second, offerCodecs.get(1).first, callerProperties);
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

	public int getCodecSampleRate(String rtpmap) {
		String a = rtpmap.split("/")[0];
		return Integer.parseInt(a);
	}
}
