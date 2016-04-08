package com.letv.mobile.core.time;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @deprecated
 * @author xiaqing
 */
@Deprecated
public class SntpClient {
    private static final int ORIGINATE_TIME_OFFSET = 24;
    private static final int RECEIVE_TIME_OFFSET = 32;
    private static final int TRANSMIT_TIME_OFFSET = 40;
    private static final int NTP_PACKET_SIZE = 48;

    private static final int NTP_PORT = 123;
    private static final int NTP_MODE_CLIENT = 3;
    private static final int NTP_VERSION = 3;

    // Number of seconds between Jan 1, 1900 and Jan 1, 1970
    // 70 years plus 17 leap days
    private static final long OFFSET_1900_TO_1970 = ((365L * 70L) + 17L) * 24L * 60L * 60L;

    // system time computed from NTP server response
    private long mNtpTime = ReferenceTime.INVALID_TIME;

    // value of SystemClock.elapsedRealtime() corresponding to mNtpTime
    private long mNtpTimeReference = ReferenceTime.INVALID_TIME;

    // round trip time in milliseconds
    private long mRoundTripTime = ReferenceTime.INVALID_TIME;

    /**
     * Sends an SNTP request to the given host and processes the response.
     * @param host
     *            host name of the server.
     * @param timeout
     *            network timeout in milliseconds.
     * @return true if the transaction was successful.
     */
    public boolean requestTime(String host, int timeout) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress address = InetAddress.getByName(host);
            byte[] buffer = new byte[SntpClient.NTP_PACKET_SIZE];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length,
                    address, SntpClient.NTP_PORT);

            // set mode = 3 (client) and version = 3
            // mode is in low 3 bits of first byte
            // version is in bits 3-5 of first byte
            buffer[0] = SntpClient.NTP_MODE_CLIENT
                    | (SntpClient.NTP_VERSION << 3);

            // get current time and write it to the request packet
            long requestTime = TimeProvider.getCurrentMillisecondTime();
            long requestTicks = System.nanoTime() / 1000;
            this.writeTimeStamp(buffer, SntpClient.TRANSMIT_TIME_OFFSET,
                    requestTime);

            socket.send(request);

            // read the response
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
            long responseTicks = System.nanoTime() / 1000;
            long responseTime = requestTime + (responseTicks - requestTicks);
            socket.close();

            // extract the results
            long originateTime = this.readTimeStamp(buffer,
                    SntpClient.ORIGINATE_TIME_OFFSET);
            long receiveTime = this.readTimeStamp(buffer,
                    SntpClient.RECEIVE_TIME_OFFSET);
            long transmitTime = this.readTimeStamp(buffer,
                    SntpClient.TRANSMIT_TIME_OFFSET);
            long roundTripTime = responseTicks - requestTicks
                    - (transmitTime - receiveTime);
            long clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime)) / 2;

            // save our results - use the times on this side of the network
            // latency
            // (response rather than request time)
            this.mNtpTime = responseTime + clockOffset;
            this.mNtpTimeReference = responseTicks;
            this.mRoundTripTime = roundTripTime;
        } catch (Exception e) {

            return false;
        }

        return true;
    }

    /**
     * Returns the time computed from the NTP transaction.
     * @return time value computed from NTP server response.
     */
    public long getNtpTime() {
        return this.mNtpTime;
    }

    /**
     * Returns the reference clock value (value of
     * SystemClock.elapsedRealtime()) corresponding to the NTP time.
     * @return reference clock corresponding to the NTP time.
     */
    public long getNtpTimeReference() {
        return this.mNtpTimeReference / 1000;
    }

    /**
     * Returns the round trip time of the NTP transaction
     * @return round trip time in milliseconds.
     */
    public long getRoundTripTime() {
        return this.mRoundTripTime;
    }

    /**
     * If this client have been got sntp time.
     * @return
     */
    public boolean isFetchedTime() {
        return this.getNtpTime() != ReferenceTime.INVALID_TIME;
    }

    /**
     * Get current time.
     * @return
     */
    public long getCurrentTime() {
        return this.isFetchedTime() ? this.getNtpTime()
                : ReferenceTime.INVALID_TIME;
    }

    /**
     * Reads an unsigned 32 bit big endian number from the given offset in the
     * buffer.
     */
    private long read32(byte[] buffer, int offset) {
        byte b0 = buffer[offset];
        byte b1 = buffer[offset + 1];
        byte b2 = buffer[offset + 2];
        byte b3 = buffer[offset + 3];

        // convert signed bytes to unsigned values
        int i0 = ((b0 & 0x80) == 0x80 ? (b0 & 0x7F) + 0x80 : b0);
        int i1 = ((b1 & 0x80) == 0x80 ? (b1 & 0x7F) + 0x80 : b1);
        int i2 = ((b2 & 0x80) == 0x80 ? (b2 & 0x7F) + 0x80 : b2);
        int i3 = ((b3 & 0x80) == 0x80 ? (b3 & 0x7F) + 0x80 : b3);

        return ((long) i0 << 24) + ((long) i1 << 16) + ((long) i2 << 8) + i3;
    }

    /**
     * Reads the NTP time stamp at the given offset in the buffer and returns it
     * as a system time (milliseconds since January 1, 1970).
     */
    private long readTimeStamp(byte[] buffer, int offset) {
        long seconds = this.read32(buffer, offset);
        long fraction = this.read32(buffer, offset + 4);
        return ((seconds - SntpClient.OFFSET_1900_TO_1970) * 1000)
                + ((fraction * 1000L) / 0x100000000L);
    }

    /**
     * Writes system time (milliseconds since January 1, 1970) as an NTP time
     * stamp at the given offset in the buffer.
     */
    private void writeTimeStamp(byte[] buffer, int offset, long time) {
        long seconds = time / 1000L;
        long milliseconds = time - seconds * 1000L;
        seconds += SntpClient.OFFSET_1900_TO_1970;

        // write seconds in big endian format
        buffer[offset++] = (byte) (seconds >> 24);
        buffer[offset++] = (byte) (seconds >> 16);
        buffer[offset++] = (byte) (seconds >> 8);
        buffer[offset++] = (byte) (seconds);

        long fraction = milliseconds * 0x100000000L / 1000L;
        // write fraction in big endian format
        buffer[offset++] = (byte) (fraction >> 24);
        buffer[offset++] = (byte) (fraction >> 16);
        buffer[offset++] = (byte) (fraction >> 8);
        // low order bits should be random data
        buffer[offset++] = (byte) (Math.random() * 255.0);
    }
}
