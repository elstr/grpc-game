package nl.toefel.server.state;

import java.util.concurrent.locks.Lock;

public class AutoClosableLocker implements AutoCloseable {

  public Lock state;

  public AutoClosableLocker(Lock lock) {
    this.state = lock;
    this.state.lock();
  }

  @Override
  public void close() {
    this.state.unlock();
  }
}
