package com.dianping.cat.demo;

import java.io.File;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class Demo extends ComponentTestCase {
	private static boolean s_initialized;

	@Before
	public void before() throws Exception {
		if (!s_initialized) {
			File configFile = getResourceFile("client.xml");

			s_initialized = true;
			Cat.initialize(getContainer(), configFile);
		}

		Cat.setup(null);
	}

	@After
	public void after() throws Exception {
		Cat.reset();
	}

	@Test
	public void testSingleTransaction() throws Exception {
		MessageProducer cat = lookup(MessageProducer.class);
		Transaction t = cat.newTransaction("URL", "FailureReportPage");

		cat.logEvent("Error", OutOfMemoryError.class.getName(), "ERROR", null);
		cat.logEvent("Exception", Exception.class.getName(), "ERROR", null);
		cat.logEvent("RuntimeException", RuntimeException.class.getName(), "ERROR", null);
		cat.logEvent("Exception", Exception.class.getName(), "ERROR", null);
		cat.logEvent("RuntimeException", NullPointerException.class.getName(), "ERROR", null);
		Thread.sleep(new Random().nextInt(10) * new Random().nextInt(3));
		t.setStatus("error");
		t.complete();
	}

	@Test
	public void testNestTransaction() throws Exception {
		DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();
		final Transaction f = cat.newTransaction("demo", "father");
		Thread child = new Thread(new Runnable() {
			@Override
			public void run() {
				DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();
				Transaction t = cat.newTransaction(f, "demo", "child");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				t.setStatus("child");
				t.complete();
			}
		});
		child.start();
		child.join();
		f.setStatus("father");
		f.complete();
	}
}
