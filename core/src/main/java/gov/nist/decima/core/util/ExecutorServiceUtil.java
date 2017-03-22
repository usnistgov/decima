/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.decima.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceUtil {
  private ExecutorServiceUtil() {
    // disable construction
  }

  public static ExecutorService addShutdownHook(ExecutorService executorService, long timeout, TimeUnit timeoutUnits) {
    Runtime.getRuntime().addShutdownHook(new ShutdownThread(executorService, timeout, timeoutUnits));
    return executorService;
  }

  private static class ShutdownThread extends Thread {
    private static final Logger log = LogManager.getLogger(ShutdownThread.class);
    private final ExecutorService executorService;
    private final long timeout;
    private final TimeUnit timeoutUnits;

    private ShutdownThread(ExecutorService executorService, long timeout, TimeUnit timeoutUnits) {
      this.executorService = executorService;
      this.timeout = timeout;
      this.timeoutUnits = timeoutUnits;
    }

    @Override
    public void run() {
      executorService.shutdown();
      try {
        if (!executorService.awaitTermination(timeout, timeoutUnits)) {
          log.warn("The ExecutorService did not shutdown in the specified time.");
          List<Runnable> waitingTasks = executorService.shutdownNow();
          log.warn("The ExecutorService was abruptly shut down, with {} task(s) left unexecuted.", waitingTasks.size());
        }
      } catch (InterruptedException e) {
        log.error(e);
      }
    }
  }
}
