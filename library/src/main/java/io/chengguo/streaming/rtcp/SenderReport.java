package io.chengguo.streaming.rtcp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.chengguo.streaming.exceptions.NotSupportException;
import io.chengguo.streaming.utils.Bits;

import static io.chengguo.streaming.utils.Bits.getLongByInt;

//@formatter:off
//|        0                   1                   2                   3
//|         0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//| header |V=2|P|    RC   |   PT=SR=200   |             length            |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |                         SSRC of sender                        |
//|        +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
//| sender |              NTP timestamp, most significant word             |
//| info   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |             NTP timestamp, least significant word             |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |                         RTP timestamp                         |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |                     sender's packet count                     |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |                      sender's octet count                     |
//|        +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
//| report |                 SSRC_1 (SSRC of first source)                 |
//| block  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|   1    | fraction lost |       cumulative number of packets lost       |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |           extended highest sequence number received           |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |                      interarrival jitter                      |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |                         last SR (LSR)                         |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|        |                   delay since last SR (DLSR)                  |
//|        +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
//| report |                 SSRC_2 (SSRC of second source)                |
//| block  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|   2    :                               ...                             :
//|        +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
//|        |                  profile-specific extensions                  |
//|        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//@formatter:on

/**
 * 发送端报告
 */
public class SenderReport implements IPacket {
    public static final byte PACKET_TYPE = (byte) 200;

    private int version;
    private boolean padding;
    private int counter;
    private int pt;
    private int length;
    private long ssrc;
    private long ntpMsw;
    private long ntpLsw;
    private long rtpTimestamp;
    private long packetCount;//已发送的RTCP包数量
    private long octetCount;//8字节数量
    private List<ReportBlock> reportBlocks;

    @Override
    public String toString() {
        return "SenderReport{" +
                "version=" + version +
                ", padding=" + padding +
                ", counter=" + counter +
                ", pt=" + pt +
                ", length=" + length +
                ", ssrc=" + ssrc +
                ", ntpMsw=" + ntpMsw +
                ", ntpLsw=" + ntpLsw +
                ", rtpTimestamp=" + rtpTimestamp +
                ", packetCount=" + packetCount +
                ", octetCount=" + octetCount +
                ", reportBlocks=" + reportBlocks +
                '}';
    }

    @Override
    public byte[] toRaw() {
        throw new RuntimeException();
    }


    public static SenderReport of(ByteBuffer buffer) {
        SenderReport senderReport = new SenderReport();
        byte vpc = buffer.get();
        senderReport.version = vpc >> 6 & 0x3;
        senderReport.padding = (vpc >> 5 & 0x1) == 1;
        senderReport.counter = vpc & 0x1f;
        senderReport.pt = buffer.get() & 0xff;
        senderReport.length = buffer.getShort();
        senderReport.ssrc = getLongByInt(buffer);
        senderReport.ntpMsw = getLongByInt(buffer);
        senderReport.ntpLsw = getLongByInt(buffer);
        senderReport.rtpTimestamp = getLongByInt(buffer);
        senderReport.packetCount = getLongByInt(buffer);
        senderReport.octetCount = getLongByInt(buffer);

        //report block
        senderReport.reportBlocks = new ArrayList<>();
        for (int i = 0; i < senderReport.counter; i++) {
            ReportBlock reportBlock = new ReportBlock();
            reportBlock.identifier = getLongByInt(buffer);
            reportBlock.fractionLost = buffer.get() & 0xff;
            byte[] numberOfPacketsLostArr = new byte[3];
            buffer.get(numberOfPacketsLostArr);
            reportBlock.numberOfPacketsLost = Bits.byteArrayToInt(numberOfPacketsLostArr);
            reportBlock.exHighestNumber = getLongByInt(buffer);
            reportBlock.interarrivalJitter = getLongByInt(buffer);
            reportBlock.lastSR = getLongByInt(buffer);
            reportBlock.delayLastSR = getLongByInt(buffer);
            senderReport.reportBlocks.add(reportBlock);
        }

        return senderReport;
    }
}