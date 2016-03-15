package org.github.sipuada.plugins.android.audio;

import android.content.Context;
import android.gov.nist.gnjvx.sdp.MediaDescriptionImpl;
import android.gov.nist.gnjvx.sdp.fields.*;
import android.javax.sdp.*;
import android.util.Log;
import android.util.Pair;

import org.github.sipuada.Constants.RequestMethod;
import org.github.sipuada.UserAgent;
import org.github.sipuada.plugins.android.audio.utils.SipuadaLog;
import org.github.sipuada.plugins.SipuadaPlugin;

import java.util.*;

public class AndroidAudioSipuadaPlugin implements SipuadaPlugin, AudioManager.OnErrorListener {

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
    private ArrayList<Pair<SipuadaAudioCodec, Integer>> matchCodecs;
    Map<String, String> sessionsIds = new HashMap<>();

    public AndroidAudioSipuadaPlugin(String username, Context context) {

        mSipuadaAudioManager = new AudioManager(context,this);
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

			MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();
			MediaField audioField = new MediaField();
			audioField.setMedia("audio");
			//TODO TRATAMENTO DE PORTA = 0
			int port = mSipuadaAudioManager.getAudioStreamPort();
			audioField.setPort(port);
			audioField.setProtocol(SdpConstants.RTP_AVP);
			audioField.setMediaType("audio");

			Vector<String> audioFormats = new Vector<>();

			for (SipuadaAudioCodec codec : myCodecs) {
				audioFormats.add(Integer.toString(codec.type));
				audioField.setFormats(audioFormats);

				AttributeField attributeField = new AttributeField();
				attributeField.setName(SdpConstants.RTPMAP);
				attributeField.setValue(Integer.toString(codec.type) + " " + codec.rtpmap);
				audioDescription.addAttribute(attributeField);

				AttributeField sendReceive = new AttributeField();
				sendReceive.setValue("sendrecv");
				audioDescription.addAttribute(sendReceive);

				audioDescription.setMediaField(audioField);
			}

			mediaDescriptions.add(audioDescription);

			offer.setOrigin(originField);
			offer.setConnection(connectionField);
			offer.setMediaDescriptions(mediaDescriptions);

			records.put(callId, new Record(offer));
			SipuadaLog.error("offer sent, callId: " + callId + " Porta? " + port);
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
					Vector attributeFields = ((MediaDescription) offerMediaDescription).getAttributes(false);
					for (Object attributeField : attributeFields) {
						if (SdpConstants.RTPMAP.equals(((AttributeField) attributeField).getName())) {
							SipuadaLog.verbose(((AttributeField) attributeField).getValue());
							for (SipuadaAudioCodec codec : myCodecs) {
								if (getCodecRtmap(((AttributeField) attributeField).getValue()).toLowerCase().equals(codec.rtpmap.toLowerCase())) {
									matchCodecs.add(new Pair<>(codec,port));
								}
							}
						}
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
			MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();
			MediaField audioField = new MediaField();
			audioField.setMedia("audio");
			int port = mSipuadaAudioManager.getAudioStreamPort();
			audioField.setPort(port);
			audioField.setProtocol(SdpConstants.RTP_AVP);
			audioField.setMediaType("audio");

			Vector<String> audioFormats = new Vector<>();

			for (Pair<SipuadaAudioCodec,Integer> codecAndPort : matchCodecs) {
				SipuadaAudioCodec codec = codecAndPort.first;

				audioFormats.add(Integer.toString(codec.type));
				audioField.setFormats(audioFormats);

				AttributeField attributeField = new AttributeField();
				attributeField.setName(SdpConstants.RTPMAP);
				attributeField.setValue(Integer.toString(codec.type) + " "+ codec.rtpmap);
				audioDescription.addAttribute(attributeField);

				AttributeField sendReceive = new AttributeField();
				sendReceive.setValue("sendrecv");
				audioDescription.addAttribute(sendReceive);

				audioDescription.setMediaField(audioField);
			}

			mediaDescriptions.add(audioDescription);

			answer.setOrigin(originField);
			answer.setConnection(connectionField);
			answer.setMediaDescriptions(mediaDescriptions);

			records.put(callId, new Record(offer, answer));
			SipuadaLog.error("answer sent, callId: " + callId + " Porta? " + port);
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
		int remoteRtpPort = 0;
		int localPort = 0;
		int codecPayloadType = 0;
		Map<String, String> properties = new HashMap<>();
		//TODO DEFINIR PRIORIDADES DE ESCOLHA DE CODEC
		SipuadaAudioCodec priorityCodec = SipuadaAudioCodec.PCMA;

		switch (roles.get(callId)) {
			case CALLEE:
                try {
                    Vector answerMediaDescriptions = answer.getMediaDescriptions(false);
                    for (Object answerMediaDescription : answerMediaDescriptions) {
                        if (answerMediaDescription instanceof MediaDescription) {
							localPort = ((MediaDescription)answerMediaDescription).getMedia().getMediaPort();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SipuadaLog.error("Failed to extract media from answer.", e);
                }

				for (Pair<SipuadaAudioCodec, Integer> codec : matchCodecs) {
					if (codec.first.rtpmap.toLowerCase().equals(priorityCodec.rtpmap.toLowerCase())) {
						codecPayloadType = codec.first.type;
						remoteRtpPort = codec.second;
						properties.put(AudioManager.RATE,getCodecSampleRate(codec.first.rtpmap));
						break;
					} else {
						codecPayloadType = matchCodecs.get(0).first.type;
						remoteRtpPort = matchCodecs.get(0).second;
						properties.put(AudioManager.RATE,getCodecSampleRate(matchCodecs.get(0).first.rtpmap));
					}
				}

				try {
					mSipuadaAudioManager.startVOIPStreaming(remoteRtpPort, offer.getConnection().getAddress(), localPort, codecPayloadType, properties);
				} catch (SdpParseException e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to start audio streaming.", e);
				}
				return true;

			case CALLER:
				ArrayList<SipuadaAudioCodec> answerCodecs = new ArrayList<>();

				try {
					Vector offerMediaDescriptions = offer.getMediaDescriptions(false);
					for (Object offerMediaDescription : offerMediaDescriptions) {
						if (offerMediaDescription instanceof MediaDescriptionImpl) {
							localPort = ((MediaDescriptionImpl) offerMediaDescription).getMediaField().getMediaPort();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to extract media from answer.", e);
				}

				try {
					Vector answerMediasDescription = answer.getMediaDescriptions(false);
					for (Object answerMediaDescription : answerMediasDescription) {
						if (answerMediaDescription instanceof MediaDescriptionImpl) {
							remoteRtpPort = ((MediaDescription)answerMediaDescription).getMedia().getMediaPort();

							Vector attributeFields = ((MediaDescription) answerMediaDescription).getAttributes(false);
							for (Object attributeField : attributeFields) {
								if (SdpConstants.RTPMAP.equals(((AttributeField) attributeField).getName())) {
									answerCodecs.add(getCodec(((AttributeField) attributeField).getValue()));
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					SipuadaLog.error("Failed to extract media from answer.", e);
				}

				for (SipuadaAudioCodec codec : answerCodecs) {
					for (SipuadaAudioCodec myCodec : myCodecs) {
						if (codec.rtpmap.toLowerCase().equals(myCodec.rtpmap.toLowerCase())) {
							if (myCodec.rtpmap.toLowerCase().equals(priorityCodec.rtpmap.toLowerCase())) {
								codecPayloadType = myCodec.type;
								properties.put(AudioManager.RATE,getCodecSampleRate(myCodec.rtpmap));
								break;
							} else {
								codecPayloadType = myCodecs[0].type;
								properties.put(AudioManager.RATE,getCodecSampleRate(myCodecs[0].rtpmap));
							}
						}
					}
				}

				try {
					//int remoteRtpPort, String remoteIp, int localPort, int codecPayloadType, Map<String, String> properties
					mSipuadaAudioManager.startVOIPStreaming(remoteRtpPort, answer.getConnection().getAddress(), localPort, codecPayloadType, properties);
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

	public String getCodecSampleRate(String rtpmap) {
		return rtpmap.split("/")[1];
	}

	public String getCodecRtmap(String rtpmap) {
		return rtpmap.split(" ")[1];
	}

	public SipuadaAudioCodec getCodec(String rtmap) {
		return new SipuadaAudioCodec(Integer.parseInt(rtmap.split(" ")[0]), rtmap.split(" ")[1], null);
	}

    @Override
    public void onError(String streamerName, String message) {
        Log.wtf("AndroidAudioSipuadaPlugin", streamerName + " : " + message);
    }
}
