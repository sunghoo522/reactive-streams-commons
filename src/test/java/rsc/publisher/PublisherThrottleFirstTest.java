package rsc.publisher;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;
import rsc.processor.DirectProcessor;
import rsc.test.TestSubscriber;
import rsc.util.ConstructorTestBuilder;

public class PublisherThrottleFirstTest {

    @Test
    public void constructors() {
        ConstructorTestBuilder ctb = new ConstructorTestBuilder(PublisherThrottleFirst.class);
        
        ctb.addRef("source", PublisherNever.instance());
        ctb.addRef("throttler", (Function<Object, Publisher<Object>>)v -> PublisherNever.instance());
        
        ctb.test();
    }
    
    @Test
    public void normal() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        DirectProcessor<Integer> sp2 = new DirectProcessor<>();
        DirectProcessor<Integer> sp3 = new DirectProcessor<>();
        
        sp1.throttleFirst(v -> v == 1 ? sp2 : sp3).subscribe(ts);
        
        sp1.onNext(1);
        
        ts.assertValue(1)
        .assertNoError()
        .assertNotComplete();
        
        sp1.onNext(2);
        
        ts.assertValue(1)
        .assertNoError()
        .assertNotComplete();

        sp2.onNext(1);
        
        ts.assertValue(1)
        .assertNoError()
        .assertNotComplete();

        sp1.onNext(3);
        
        ts.assertValues(1, 3)
        .assertNoError()
        .assertNotComplete();
        
        sp1.onComplete();
        
        ts.assertValues(1, 3)
        .assertNoError()
        .assertComplete();
        
        Assert.assertFalse("sp1 has subscribers?", sp1.hasDownstreams());
        Assert.assertFalse("sp1 has subscribers?", sp2.hasDownstreams());
        Assert.assertFalse("sp1 has subscribers?", sp3.hasDownstreams());
    }
    
    @Test
    public void mainError() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        DirectProcessor<Integer> sp2 = new DirectProcessor<>();
        DirectProcessor<Integer> sp3 = new DirectProcessor<>();
        
        sp1.throttleFirst(v -> v == 1 ? sp2 : sp3).subscribe(ts);
        
        sp1.onNext(1);
        sp1.onError(new RuntimeException("forced failure"));
        
        ts.assertValue(1)
        .assertError(RuntimeException.class)
        .assertErrorMessage("forced failure")
        .assertNotComplete();
        
        Assert.assertFalse("sp1 has subscribers?", sp1.hasDownstreams());
        Assert.assertFalse("sp1 has subscribers?", sp2.hasDownstreams());
        Assert.assertFalse("sp1 has subscribers?", sp3.hasDownstreams());
    }

    @Test
    public void throttlerError() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        DirectProcessor<Integer> sp2 = new DirectProcessor<>();
        DirectProcessor<Integer> sp3 = new DirectProcessor<>();
        
        sp1.throttleFirst(v -> v == 1 ? sp2 : sp3).subscribe(ts);
        
        sp1.onNext(1);
        sp2.onError(new RuntimeException("forced failure"));
        
        ts.assertValue(1)
        .assertError(RuntimeException.class)
        .assertErrorMessage("forced failure")
        .assertNotComplete();
        
        Assert.assertFalse("sp1 has subscribers?", sp1.hasDownstreams());
        Assert.assertFalse("sp1 has subscribers?", sp2.hasDownstreams());
        Assert.assertFalse("sp1 has subscribers?", sp3.hasDownstreams());
    }

    @Test
    public void throttlerThrows() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        
        sp1.throttleFirst(v -> { throw new RuntimeException("forced failure"); }).subscribe(ts);
        
        sp1.onNext(1);
        
        ts.assertValue(1)
        .assertError(RuntimeException.class)
        .assertErrorMessage("forced failure")
        .assertNotComplete();
        
        Assert.assertFalse("sp1 has subscribers?", sp1.hasDownstreams());
    }

    @Test
    public void throttlerReturnsNull() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        
        sp1.throttleFirst(v -> null).subscribe(ts);
        
        sp1.onNext(1);
        
        ts.assertValue(1)
        .assertError(NullPointerException.class)
        .assertNotComplete();
        
        Assert.assertFalse("sp1 has subscribers?", sp1.hasDownstreams());
    }

}
