package pt.hlbk;

import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Benchmarks {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmarks.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureThroughput() {
        final OneToOneRingBuffer buffer = new OneToOneRingBuffer(new UnsafeBuffer(
                ByteBuffer.allocate(32768 + RingBufferDescriptor.TRAILER_LENGTH)));

        IntStream.range(0, 100000)
                .boxed()
                .forEach(i -> {
                    //Adding elements in Agrona RingBuffer
                    String s = Integer.toString(i);
                    UnsafeBuffer unsafeBuffer = new UnsafeBuffer(s.getBytes());
                    unsafeBuffer.wrap(s.getBytes());
                    buffer.write(1, unsafeBuffer, 0, s.length());
                });

        IntStream.range(0, 100000)
                .boxed()
                .forEach(i -> {
                    //Reading elements from an Agrona RingBuffer
                    buffer.read((msgTypeId, srcBuffer, index, length) -> {
                        byte[] message = new byte[length];
                        srcBuffer.getBytes(index, message);
                        //We would get string like so `new String(message)`
                    });
                });
    }
}
