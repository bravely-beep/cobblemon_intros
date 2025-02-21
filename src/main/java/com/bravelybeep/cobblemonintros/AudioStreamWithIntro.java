package com.bravelybeep.cobblemonintros;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;

import net.minecraft.client.sounds.AudioStream;

public class AudioStreamWithIntro implements AudioStream {
    private final AudioStream audioStreamFirst;
    private final AudioStream audioStreamSecond;
    private AudioStream audioStreamActive;

    public AudioStreamWithIntro(AudioStream audioStreamFirst, AudioStream audioStreamSecond) {
        this.audioStreamFirst = audioStreamFirst;
        this.audioStreamSecond = audioStreamSecond;
        this.audioStreamActive = audioStreamFirst;
    }

    @Override
    public AudioFormat getFormat() {
        return this.audioStreamActive.getFormat();
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        ByteBuffer byteBuffer = this.audioStreamActive.read(size);
        if (!byteBuffer.hasRemaining() && this.audioStreamActive == this.audioStreamFirst) {
            this.audioStreamFirst.close();
            this.audioStreamActive = this.audioStreamSecond;
            byteBuffer = this.audioStreamActive.read(size);
        }
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        this.audioStreamFirst.close();
        this.audioStreamSecond.close();
    }
}
