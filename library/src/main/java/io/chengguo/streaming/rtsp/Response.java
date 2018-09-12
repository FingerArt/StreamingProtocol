package io.chengguo.streaming.rtsp;

import java.util.ArrayList;
import java.util.List;

import io.chengguo.streaming.rtsp.header.ContentLengthHeader;
import io.chengguo.streaming.rtsp.header.Header;

/**
 * Created by fingerart on 2018-07-17.
 */
public class Response implements IMessage {
    private Line line;
    private List<Header> headers = new ArrayList<>();
    private int contentLength;
    private Body body = new Body();

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public List<Header> getHeader() {
        return headers;
    }

    public void setHeader(List<Header> header) {
        this.headers = header;
    }

    public void addHeader(Header header) {
        if (header instanceof ContentLengthHeader) {
            contentLength = ((ContentLengthHeader) header).getRawValue();
        }
        headers.add(header);
    }

    public int getContentLength() {
        return contentLength;
    }

    public void appendBody(String sLine) {
        body.append(sLine);
    }

    public Body getBody() {
        return body;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(line.toString()).append("\r\n");
        for (Header header : headers) {
            buffer.append(header).append("\r\n");
        }
        buffer.append("\r\n").append(body.toString());
        buffer.append("\r\n").append("\r\n");
        return buffer.toString();
    }

    @Override
    public byte[] toRaw() {
        return toString().getBytes();
    }

    public static class Line {
        private Version version;
        private int statusCode;
        private String statusMessage;

        public Version getVersion() {
            return version;
        }

        public void setVersion(Version version) {
            this.version = version;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }

        @Override
        public String toString() {
            return version + " " + statusCode + " " + statusMessage;
        }

        public static Line parse(String sLine) {
            assert sLine != null;

            String[] ls = sLine.split(" ");
            //version
            String sVersion = ls[0];
            String version = sVersion.substring(sVersion.indexOf("/") + 1);
            //status code
            int code = Integer.valueOf(ls[1]);
            //status message
            String message = ls[2];
            Line line = new Line();
            line.setVersion(new Version(version));
            line.setStatusCode(code);
            line.setStatusMessage(message);
            return line;
        }
    }

    public static class Body {
        private StringBuilder content = new StringBuilder();

        public void append(String sLine) {
            content.append(sLine).append("\r\n");
        }

        public int getLength() {
            return toString().getBytes().length;
        }

        @Override
        public String toString() {
            return content.toString();
        }
    }
}