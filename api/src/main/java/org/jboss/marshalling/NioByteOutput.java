/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.marshalling;

import java.io.OutputStream;
import java.io.IOException;
import java.io.Flushable;
import java.nio.ByteBuffer;

/**
 * A {@code ByteOutput} implementation which writes out {@code ByteBuffer}s to a consumer.
 */
public class NioByteOutput extends OutputStream implements ByteOutput {
    private final Object lock = new Object();
    private ByteBuffer buffer;
    private boolean eof;

    private final BufferWriter bufferWriterTask;

    /**
     * Construct a new instance.  The internal buffers will have a capacity of {@code bufferSize}.  The
     * given {@code bufferWriterTask} will be called to send buffers, flush the output stream, and handle the
     * end-of-file condition.
     *
     * @param bufferWriterTask the writer task
     */
    public NioByteOutput(final BufferWriter bufferWriterTask) {
        this.bufferWriterTask = bufferWriterTask;
        synchronized (lock) {
            buffer = bufferWriterTask.getBuffer();
        }
    }

    private static IOException closed() {
        return new IOException("Stream is closed");
    }

    private void checkClosed() throws IOException {
        if (eof) {
            throw closed();
        }
    }

    private ByteBuffer getBuffer() throws IOException {
        final ByteBuffer buffer = this.buffer;
        if (buffer != null && buffer.hasRemaining()) {
            return buffer;
        } else {
            if (buffer != null) send();
            return this.buffer = bufferWriterTask.getBuffer();
        }
    }

    /** {@inheritDoc} */
    public void write(final int b) throws IOException {
        synchronized (this) {
            checkClosed();
            getBuffer().put((byte) b);
        }
    }

    /** {@inheritDoc} */
    public void write(final byte[] b, int off, int len) throws IOException {
        synchronized (this) {
            checkClosed();
            while (len > 0) {
                final ByteBuffer buffer = getBuffer();
                final int cnt = Math.min(len, buffer.remaining());
                buffer.put(b, off, cnt);
                len -= cnt;
                off += cnt;
            }
        }
    }

    // call with lock held
    private void send() throws IOException {
        final ByteBuffer buffer = this.buffer;
        this.buffer =  null;
        final boolean eof = this.eof;
        if (buffer != null && buffer.position() > 0) {
            buffer.flip();
            send(buffer, eof);
        } else if (eof) {
            final ByteBuffer buffer1 = getBuffer();
            buffer1.flip();
            send(buffer1, eof);
        }
    }

    private void send(ByteBuffer buffer, boolean eof) throws IOException {
        try {
            bufferWriterTask.accept(buffer, eof);
        } catch (IOException e) {
            this.eof = true;
            throw e;
        }
    }

    /** {@inheritDoc} */
    public void flush() throws IOException {
        synchronized (this) {
            send();
            try {
                bufferWriterTask.flush();
            } catch (IOException e) {
                eof = true;
                buffer = null;
                throw e;
            }
        }
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
        synchronized (this) {
            if (eof) {
                return;
            }
            eof = true;
            send();
        }
    }

    /**
     * A buffer writer for an {@link NioByteOutput}.
     */
    public interface BufferWriter extends Flushable {

        /**
         * Get a new buffer to be filled.  The new buffer may, for example, include a prepended header.
         *
         * @return the new buffer
         */
        ByteBuffer getBuffer();

        /**
         * Accept a buffer.  If this is the last buffer that will be sent, the {@code eof} flag will be set to {@code true}.
         * This method should block until the entire buffer is consumed, or an error occurs.
         *
         * @param buffer the buffer to send
         * @param eof {@code true} if this is the last buffer which will be sent
         * @throws IOException if an I/O error occurs
         */
        void accept(ByteBuffer buffer, boolean eof) throws IOException;

        /**
         * Flushes this stream by writing any buffered output to the underlying stream.  This method should block until
         * the data is fully flushed.
         *
         * @throws IOException If an I/O error occurs
         */
        void flush() throws IOException;
    }
}
