package com.airbnb.lottie;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import static org.mockito.Mockito.*;

public class LottieTaskTest extends BaseTest {

  @Mock
  public LottieListener<Integer> successListener;
  @Mock
  public LottieListener<Throwable> failureListener;

  @Before
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testListener() {
    LottieTask<Integer> task = new LottieTask<>(() -> new LottieResult<>(5), true)
        .addListener(successListener)
        .addFailureListener(failureListener);
    verify(successListener, times(1)).onResult(5);
    verifyNoInteractions(failureListener);
  }

  @Test
  public void testException() {
    final IllegalStateException exception = new IllegalStateException("foo");
    LottieTask<Integer> task = new LottieTask<>((Callable<LottieResult<Integer>>) () -> {
      throw exception;
    }, true)
        .addListener(successListener)
        .addFailureListener(failureListener);
    verifyNoInteractions(successListener);
    verify(failureListener, times(1)).onResult(exception);
  }

  /**
   * This hangs on CI but not locally.
   */
  @Ignore
  @Test
  public void testRemoveListener() {
    final Semaphore lock = new Semaphore(0);
    LottieTask<Integer> task = new LottieTask<>(() -> new LottieResult<>(5))
        .addListener(successListener)
        .addFailureListener(failureListener)
        .addListener(result -> lock.release());
    task.removeListener(successListener);
    try {
      lock.acquire();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    verifyNoInteractions(successListener);
    verifyNoInteractions(failureListener);
  }

  @Test
  public void testAddListenerAfter() {
    LottieTask<Integer> task = new LottieTask<>(() -> new LottieResult<>(5), true);

    task.addListener(successListener);
    task.addFailureListener(failureListener);
    verify(successListener, times(1)).onResult(5);
    verifyNoInteractions(failureListener);
  }
}
