package io.chengguo.streaming;

import io.chengguo.streaming.codec.Decoder;
import io.chengguo.streaming.rtp.RtpPacket;

public abstract class MediaStream {

    public abstract void prepare() throws Exception;
    public abstract void feedPacket(RtpPacket packet);
    public abstract Decoder getDecoder();
}
